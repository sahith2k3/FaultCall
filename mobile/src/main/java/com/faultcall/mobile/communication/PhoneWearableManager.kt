package com.faultcall.mobile.communication

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.faultcall.mobile.FaultCallApplication
import com.faultcall.mobile.presentation.MainActivity
import com.faultcall.mobile.service.MatchSessionForegroundService
import com.faultcall.shared.command.CommandAcknowledgment
import com.faultcall.shared.command.CommandType
import com.faultcall.shared.command.WearableCommand
import com.faultcall.shared.model.AnnouncementCategory
import com.faultcall.shared.model.AnnouncementEvent
import com.faultcall.shared.model.MatchState
import com.faultcall.shared.model.Player
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.LinkedHashSet

/**
 * Handles communication between Phone and Samsung Galaxy Watch8 over Google Wearable Data Layer API.
 */
class PhoneWearableManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MessageClient.OnMessageReceivedListener, DataClient.OnDataChangedListener {

    private val tag = "PhoneWearableManager"
    private val gson = Gson()

    private val messageClient = Wearable.getMessageClient(context)
    private val dataClient = Wearable.getDataClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    // Sliding window of recently processed command IDs for duplicate rejection (up to 100)
    private val recentCommandIds = LinkedHashSet<String>()

    private val _isWatchConnected = MutableStateFlow(false)
    val isWatchConnected: StateFlow<Boolean> = _isWatchConnected.asStateFlow()

    private val _connectedWatchName = MutableStateFlow<String?>("Searching for Galaxy Watch8...")
    val connectedWatchName: StateFlow<String?> = _connectedWatchName.asStateFlow()

    // Callback to notify UI when score is updated from watch
    var onScoreUpdatedListener: ((MatchState) -> Unit)? = null

    init {
        // Register direct runtime listeners for 0-latency message delivery
        try {
            messageClient.addListener(this)
            dataClient.addListener(this)
            Log.i(tag, "Registered direct runtime Wearable listeners on Phone")
        } catch (e: Exception) {
            Log.w(tag, "Failed to register Wearable runtime listeners: ${e.message}")
        }
        refreshConnectedNodes()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.i(tag, "[WATCH_CMD] Direct runtime message on ${messageEvent.path} from ${messageEvent.sourceNodeId}")
        if (messageEvent.path == "/faultcall/command") {
            val commandJson = String(messageEvent.data)
            handleIncomingCommand(commandJson, messageEvent.sourceNodeId)
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Handle data changes if needed
    }

    fun refreshConnectedNodes() {
        scope.launch {
            try {
                // First check by capability (faultcall_watch_app)
                val capabilityInfo = try {
                    capabilityClient.getCapability("faultcall_watch_app", CapabilityClient.FILTER_REACHABLE).await()
                } catch (e: Exception) { null }

                val watchNodes = capabilityInfo?.nodes
                val allNodes = nodeClient.connectedNodes.await()
                val isConnected = !watchNodes.isNullOrEmpty() || allNodes.isNotEmpty()

                _isWatchConnected.value = isConnected
                _connectedWatchName.value = when {
                    !watchNodes.isNullOrEmpty() -> watchNodes.first().displayName
                    allNodes.isNotEmpty() -> allNodes.first().displayName
                    else -> "No watch connected"
                }
                Log.d(tag, "Connected nodes updated: ${_connectedWatchName.value} (connected=$isConnected)")
            } catch (e: Exception) {
                Log.w(tag, "Failed to query connected nodes: ${e.message}")
            }
        }
    }

    /**
     * Entry point for incoming commands from Galaxy Watch8.
     */
    fun handleIncomingCommand(commandJson: String, sourceNodeId: String?) {
        scope.launch {
            try {
                val command = gson.fromJson(commandJson, WearableCommand::class.java)
                processCommand(command, sourceNodeId)
            } catch (e: Exception) {
                Log.e(tag, "Error deserializing incoming command: ${e.message}", e)
            }
        }
    }

    private suspend fun processCommand(command: WearableCommand, sourceNodeId: String?) {
        val app = FaultCallApplication.instance
        val scoringEngine = app.scoringEngine
        val announcementManager = app.announcementManager

        Log.i(tag, ">>> [WATCH_CMD] PROCESSING: ${command.type} (id: ${command.commandId})")

        // 1. Duplicate detection
        val isDuplicate: Boolean
        synchronized(recentCommandIds) {
            if (recentCommandIds.contains(command.commandId)) {
                isDuplicate = true
            } else {
                isDuplicate = false
                recentCommandIds.add(command.commandId)
                if (recentCommandIds.size > 100) {
                    val first = recentCommandIds.iterator().next()
                    recentCommandIds.remove(first)
                }
            }
        }

        if (isDuplicate) {
            Log.w(tag, "Duplicate command ignored: ${command.commandId}")
            sendAcknowledgment(
                nodeId = sourceNodeId,
                ack = CommandAcknowledgment(
                    commandId = command.commandId,
                    type = command.type,
                    success = true,
                    isDuplicate = true,
                    message = "Ignored duplicate command"
                )
            )
            return
        }

        var spokenResult: String? = null

        // Trigger phone screen to wake up and restore app to foreground if closed/sleeping
        bringAppToForeground(context)

        when (command.type) {
            CommandType.ANNOUNCE_FAULT -> {
                spokenResult = "FAULT!"
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.FAULT,
                        spokenText = spokenResult,
                        priority = 1
                    )
                )
            }

            CommandType.DOUBLE_FAULT -> {
                spokenResult = "Double fault."
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.DOUBLE_FAULT,
                        spokenText = spokenResult,
                        priority = 1
                    )
                )
            }

            CommandType.ANNOUNCE_LET -> {
                spokenResult = "Let. First serve."
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.LET,
                        spokenText = spokenResult,
                        priority = 2
                    )
                )
            }

            CommandType.ANNOUNCE_SCORE -> {
                spokenResult = scoringEngine.getCurrentScoreAnnouncement()
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.SCORE,
                        spokenText = spokenResult,
                        priority = 5
                    )
                )
            }

            CommandType.AWARD_POINT_A -> {
                announcementManager.purgeObsoleteScoreAnnouncements()
                val res = scoringEngine.awardPoint(Player.PLAYER_A)
                spokenResult = res.announcementText
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = res.category,
                        spokenText = spokenResult,
                        priority = res.category.priority
                    )
                )
                onScoreUpdated(res.state)
            }

            CommandType.AWARD_POINT_B -> {
                announcementManager.purgeObsoleteScoreAnnouncements()
                val res = scoringEngine.awardPoint(Player.PLAYER_B)
                spokenResult = res.announcementText
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = res.category,
                        spokenText = spokenResult,
                        priority = res.category.priority
                    )
                )
                onScoreUpdated(res.state)
            }

            CommandType.UNDO -> {
                announcementManager.purgeObsoleteScoreAnnouncements()
                val restored = scoringEngine.undo()
                if (restored != null) {
                    spokenResult = "Score restored. ${scoringEngine.getCurrentScoreAnnouncement()}"
                    announcementManager.enqueue(
                        AnnouncementEvent(
                            category = AnnouncementCategory.SCORE,
                            spokenText = spokenResult,
                            priority = 5
                        )
                    )
                    onScoreUpdated(restored)
                } else {
                    spokenResult = "Cannot undo further."
                }
            }

            CommandType.REQUEST_STATE -> {
                broadcastStateToWatch(scoringEngine.getState())
            }

            CommandType.START_NEW_MATCH -> {
                val newState = scoringEngine.reset()
                spokenResult = "New match started."
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.INFO,
                        spokenText = spokenResult,
                        priority = 6
                    )
                )
                onScoreUpdated(newState)
            }

            CommandType.SET_SERVER -> {
                val targetServer = if (command.payload == Player.PLAYER_B.name) Player.PLAYER_B else Player.PLAYER_A
                val newState = scoringEngine.setServer(targetServer)
                spokenResult = "Server changed to ${newState.config.nameOf(targetServer)}."
                onScoreUpdated(newState)
            }

            CommandType.ANNOUNCE_GAME -> {
                spokenResult = "Game announcement."
            }

            CommandType.ANNOUNCE_SET -> {
                spokenResult = "Set announcement."
            }

            CommandType.ANNOUNCE_MATCH -> {
                spokenResult = "Match announcement."
            }

            CommandType.PAUSE_ANNOUNCEMENTS -> {
                announcementManager.clearAll()
            }

            CommandType.RESUME_ANNOUNCEMENTS -> {}

            CommandType.LAUNCH_PHONE_APP -> {
                spokenResult = "FaultCall active."
                val state = scoringEngine.getState()
                onScoreUpdated(state)
            }
        }

        // Send confirmation acknowledgment back to watch
        sendAcknowledgment(
            nodeId = sourceNodeId,
            ack = CommandAcknowledgment(
                commandId = command.commandId,
                type = command.type,
                success = true,
                spokenText = spokenResult
            )
        )
    }

    /**
     * Wakes the phone display and brings FaultCall MainActivity back to the foreground,
     * even if the app was completely closed or the screen was turned off.
     */
    fun bringAppToForeground(context: Context) {
        try {
            // 1. Briefly acquire wake lock to turn on screen if off
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "FaultCall:CourtWakeLock"
            )
            wakeLock?.acquire(3000L)

            // 2. Launch MainActivity with Background Activity Launch permission (Android 14)
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }
                val pi = PendingIntent.getActivity(
                    context,
                    1003,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    options.toBundle()
                )
                pi.send()
            } else {
                val pi = PendingIntent.getActivity(
                    context,
                    1003,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                pi.send()
            }
            Log.i(tag, "Triggered MainActivity back onto phone screen from watch")
        } catch (e: Exception) {
            Log.w(tag, "PendingIntent launch attempt: ${e.message}, falling back to startActivity")
            try {
                val directIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(directIntent)
            } catch (e2: Exception) {
                Log.e(tag, "Failed to start MainActivity directly: ${e2.message}")
            }
        }
    }

    private fun onScoreUpdated(state: MatchState) {
        val app = FaultCallApplication.instance
        app.matchRepository.saveActiveMatchState(state)

        if (state.isFinished) {
            app.matchRepository.saveCompletedMatch(state)
        }

        MatchSessionForegroundService.update(
            context = context,
            scoreText = state.formattedScoreSummary(),
            statusText = "${state.config.playerAName}: ${state.displayPoints(Player.PLAYER_A)} | ${state.config.playerBName}: ${state.displayPoints(Player.PLAYER_B)}"
        )

        scope.launch(Dispatchers.Main) {
            onScoreUpdatedListener?.invoke(state)
        }

        // Sync with watch
        broadcastStateToWatch(state)
    }

    fun broadcastStateToWatch(state: MatchState) {
        scope.launch {
            try {
                val stateJson = gson.toJson(state)

                // 1. DataClient: Persists on the Data Layer
                val putDataMapReq = PutDataMapRequest.create("/faultcall/match_state").apply {
                    dataMap.putString("state_json", stateJson)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent()).await()

                // 2. Direct urgent message to all connected and reachable nodes
                val nodes = nodeClient.connectedNodes.await()
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/faultcall/state_sync", stateJson.toByteArray()).await()
                    Log.i(tag, "[SYNC_SENT] Sent state_sync message to node ${node.displayName} (${node.id})")
                }

                Log.d(tag, "Broadcast state to watch: ${state.formattedScoreSummary()}")
            } catch (e: Exception) {
                Log.w(tag, "Failed to broadcast state to watch: ${e.message}")
            }
        }
    }

    private suspend fun sendAcknowledgment(nodeId: String?, ack: CommandAcknowledgment) {
        try {
            val json = gson.toJson(ack).toByteArray()
            if (nodeId != null) {
                messageClient.sendMessage(nodeId, "/faultcall/ack", json).await()
            } else {
                val nodes = nodeClient.connectedNodes.await()
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/faultcall/ack", json).await()
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to send command acknowledgment: ${e.message}")
        }
    }
}
