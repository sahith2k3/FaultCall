package com.faultcall.wear.communication

import android.content.Context
import android.util.Log
import com.faultcall.shared.command.CommandAcknowledgment
import com.faultcall.shared.command.CommandType
import com.faultcall.shared.command.WearableCommand
import com.faultcall.shared.model.MatchState
import com.faultcall.shared.model.Player
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages communication from Samsung Galaxy Watch8 to the Android Phone.
 * Implements runtime message listeners, DataClient listeners, capability discovery,
 * and optimistic score updates for instant courtside responsiveness.
 */
class WearDataLayerManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MessageClient.OnMessageReceivedListener, DataClient.OnDataChangedListener {

    private val tag = "WearDataLayer"
    private val gson = Gson()

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val dataClient = Wearable.getDataClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    private val _isPhoneConnected = MutableStateFlow(false)
    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()

    private val _matchState = MutableStateFlow(MatchState())
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    private val _lastAckFeedback = MutableStateFlow<String?>(null)
    val lastAckFeedback: StateFlow<String?> = _lastAckFeedback.asStateFlow()

    private var lastCommandTimestamp = 0L

    init {
        // Register direct runtime listeners for 0-latency message delivery
        try {
            messageClient.addListener(this)
            dataClient.addListener(this)
            Log.i(tag, "Registered direct runtime Wearable listeners on Watch")
        } catch (e: Exception) {
            Log.w(tag, "Failed to register Wearable runtime listeners: ${e.message}")
        }

        checkConnectedNodes()
        fetchLatestDataState()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.i(tag, "[PHONE_MSG] Direct runtime message on ${messageEvent.path}")
        when (messageEvent.path) {
            "/faultcall/state_sync" -> {
                onStateSyncReceived(String(messageEvent.data))
            }
            "/faultcall/ack" -> {
                onAcknowledgmentReceived(String(messageEvent.data))
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(tag, "[DATA_CHANGED] Data item updated on Watch")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/faultcall/match_state") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val stateJson = dataMap.getString("state_json")
                if (!stateJson.isNullOrBlank()) {
                    onStateSyncReceived(stateJson)
                }
            }
        }
    }

    fun checkConnectedNodes() {
        scope.launch {
            try {
                // Check capability first
                val capabilityInfo = try {
                    capabilityClient.getCapability("faultcall_phone_app", CapabilityClient.FILTER_REACHABLE).await()
                } catch (e: Exception) { null }

                val phoneCapNodes = capabilityInfo?.nodes
                val allNodes = nodeClient.connectedNodes.await()
                val isConnected = !phoneCapNodes.isNullOrEmpty() || allNodes.isNotEmpty()

                _isPhoneConnected.value = isConnected
                Log.d(tag, "Phone connection status: $isConnected (capability nodes: ${phoneCapNodes?.size ?: 0}, total nodes: ${allNodes.size})")

                // Request state sync from phone immediately on connect
                if (isConnected) {
                    sendCommand(CommandType.REQUEST_STATE)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to query connected nodes: ${e.message}")
            }
        }
    }

    fun fetchLatestDataState() {
        scope.launch {
            try {
                val buffer = dataClient.dataItems.await()
                for (item in buffer) {
                    if (item.uri.path == "/faultcall/match_state") {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        val json = dataMap.getString("state_json")
                        if (!json.isNullOrBlank()) {
                            val state = gson.fromJson(json, MatchState::class.java)
                            _matchState.value = state
                            Log.d(tag, "Loaded cached match state from DataClient: ${state.formattedScoreSummary()}")
                        }
                    }
                }
                buffer.release()
            } catch (e: Exception) {
                Log.w(tag, "Failed to read cached data items: ${e.message}")
            }
        }
    }

    /**
     * Sends command to the phone and applies optimistic UI update if scoring.
     */
    fun sendCommand(type: CommandType, payload: String? = null, onSent: (() -> Unit)? = null) {
        val now = System.currentTimeMillis()
        if (now - lastCommandTimestamp < 250L && type != CommandType.REQUEST_STATE) {
            Log.d(tag, "Command debounced (tap too rapid)")
            return
        }
        lastCommandTimestamp = now

        // Optimistic UI updates on watch for instantaneous visual feedback
        applyOptimisticUpdate(type)

        scope.launch {
            try {
                val command = WearableCommand(
                    type = type,
                    matchId = _matchState.value.matchId,
                    payload = payload
                )
                val json = gson.toJson(command).toByteArray()

                // Find phone node (favor capability node, fallback to connected nodes)
                val capabilityInfo = try {
                    capabilityClient.getCapability("faultcall_phone_app", CapabilityClient.FILTER_REACHABLE).await()
                } catch (e: Exception) { null }

                val targetNodes = if (!capabilityInfo?.nodes.isNullOrEmpty()) {
                    capabilityInfo!!.nodes
                } else {
                    nodeClient.connectedNodes.await()
                }

                if (targetNodes.isEmpty()) {
                    Log.w(tag, "Cannot send command: no connected phone found")
                    _isPhoneConnected.value = false
                    _lastAckFeedback.value = "Phone disconnected"
                    return@launch
                }

                _isPhoneConnected.value = true
                for (node in targetNodes) {
                    messageClient.sendMessage(node.id, "/faultcall/command", json).await()
                    Log.i(tag, ">>> [WATCH_SEND] Sent $type to ${node.displayName} (${node.id})")
                }

                withContext(Dispatchers.Main) {
                    onSent?.invoke()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to send command $type: ${e.message}")
                _lastAckFeedback.value = "Send failed"
            }
        }
    }

    private fun applyOptimisticUpdate(type: CommandType) {
        val current = _matchState.value
        when (type) {
            CommandType.AWARD_POINT_A -> {
                if (!current.isTiebreak && current.playerAPoints < 3) {
                    _matchState.value = current.copy(playerAPoints = current.playerAPoints + 1)
                }
            }
            CommandType.AWARD_POINT_B -> {
                if (!current.isTiebreak && current.playerBPoints < 3) {
                    _matchState.value = current.copy(playerBPoints = current.playerBPoints + 1)
                }
            }
            CommandType.ANNOUNCE_FAULT -> {
                _lastAckFeedback.value = "FAULT! sent"
            }
            CommandType.ANNOUNCE_SCORE -> {
                _lastAckFeedback.value = "Requesting score..."
            }
            else -> {}
        }
    }

    fun onAcknowledgmentReceived(ackJson: String) {
        try {
            val ack = gson.fromJson(ackJson, CommandAcknowledgment::class.java)
            _lastAckFeedback.value = ack.spokenText ?: "Call delivered"
            Log.i(tag, "<<< [WATCH_ACK] Received phone ACK for ${ack.type}: ${ack.spokenText}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse ACK: ${e.message}")
        }
    }

    fun onStateSyncReceived(stateJson: String) {
        try {
            val state = gson.fromJson(stateJson, MatchState::class.java)
            _matchState.value = state
            _isPhoneConnected.value = true
            Log.i(tag, "<<< [WATCH_SYNC] Reconciled match state from phone: ${state.formattedScoreSummary()} (Points: ${state.displayPoints(Player.PLAYER_A)} - ${state.displayPoints(Player.PLAYER_B)})")
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse synced state: ${e.message}")
        }
    }
}
