package com.faultcall.wear.communication

import android.util.Log
import com.faultcall.wear.FaultCallWearApplication
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService

class WearStateListenerService : WearableListenerService() {

    private val tag = "WearStateService"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val app = FaultCallWearApplication.instance
        when (messageEvent.path) {
            "/faultcall/ack" -> {
                val ackJson = String(messageEvent.data)
                app.dataLayerManager.onAcknowledgmentReceived(ackJson)
            }
            "/faultcall/state_sync" -> {
                val stateJson = String(messageEvent.data)
                app.dataLayerManager.onStateSyncReceived(stateJson)
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val app = FaultCallWearApplication.instance
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/faultcall/match_state") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val stateJson = dataMap.getString("state_json")
                if (!stateJson.isNullOrBlank()) {
                    app.dataLayerManager.onStateSyncReceived(stateJson)
                }
            }
        }
    }

    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        Log.i(tag, "Phone connected: ${peer.displayName}")
        FaultCallWearApplication.instance.dataLayerManager.checkConnectedNodes()
    }

    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        Log.w(tag, "Phone disconnected: ${peer.displayName}")
        FaultCallWearApplication.instance.dataLayerManager.checkConnectedNodes()
    }
}
