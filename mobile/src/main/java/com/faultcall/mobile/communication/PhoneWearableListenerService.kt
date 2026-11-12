package com.faultcall.mobile.communication

import android.util.Log
import com.faultcall.mobile.FaultCallApplication
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService

class PhoneWearableListenerService : WearableListenerService() {

    private val tag = "PhoneWearableService"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(tag, "Message received on path: ${messageEvent.path} from node: ${messageEvent.sourceNodeId}")
        if (messageEvent.path == "/faultcall/command") {
            val commandJson = String(messageEvent.data)
            FaultCallApplication.instance.wearableManager.handleIncomingCommand(
                commandJson = commandJson,
                sourceNodeId = messageEvent.sourceNodeId
            )
        }
    }

    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        Log.i(tag, "Galaxy Watch connected: ${peer.displayName}")
        FaultCallApplication.instance.wearableManager.refreshConnectedNodes()
        // Broadcast current state to newly connected watch
        val state = FaultCallApplication.instance.scoringEngine.getState()
        FaultCallApplication.instance.wearableManager.broadcastStateToWatch(state)
    }

    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        Log.w(tag, "Galaxy Watch disconnected: ${peer.displayName}")
        FaultCallApplication.instance.wearableManager.refreshConnectedNodes()
    }
}
