package com.faultcall.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.faultcall.shared.command.CommandType
import com.faultcall.shared.model.MatchState
import com.faultcall.shared.model.Player
import com.faultcall.wear.FaultCallWearApplication
import kotlinx.coroutines.flow.StateFlow

class WearMainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FaultCallWearApplication
    private val dataLayerManager = app.dataLayerManager
    private val haptic = app.hapticManager

    val matchState: StateFlow<MatchState> = dataLayerManager.matchState
    val isPhoneConnected: StateFlow<Boolean> = dataLayerManager.isPhoneConnected
    val lastAck: StateFlow<String?> = dataLayerManager.lastAckFeedback

    fun onFaultTap() {
        haptic.heavyFault()
        dataLayerManager.sendCommand(CommandType.ANNOUNCE_FAULT)
    }

    fun onScoreTap() {
        haptic.click()
        dataLayerManager.sendCommand(CommandType.ANNOUNCE_SCORE)
    }

    fun onPointATap() {
        haptic.click()
        dataLayerManager.sendCommand(CommandType.AWARD_POINT_A)
    }

    fun onPointBTap() {
        haptic.click()
        dataLayerManager.sendCommand(CommandType.AWARD_POINT_B)
    }

    fun onUndoTap() {
        haptic.doublePulse()
        dataLayerManager.sendCommand(CommandType.UNDO)
    }

    fun onLetTap() {
        haptic.click()
        dataLayerManager.sendCommand(CommandType.ANNOUNCE_LET)
    }

    fun onDoubleFaultTap() {
        haptic.heavyFault()
        dataLayerManager.sendCommand(CommandType.DOUBLE_FAULT)
    }

    fun onStartNewMatchTap() {
        haptic.doublePulse()
        dataLayerManager.sendCommand(CommandType.START_NEW_MATCH)
    }

    fun onToggleServerTap() {
        haptic.click()
        val nextServer = matchState.value.currentServer.opponent()
        dataLayerManager.sendCommand(CommandType.SET_SERVER, payload = nextServer.name)
    }

    fun refreshState() {
        dataLayerManager.sendCommand(CommandType.REQUEST_STATE)
    }

    fun onLaunchPhoneAppTap() {
        haptic.doublePulse()
        dataLayerManager.sendCommand(CommandType.LAUNCH_PHONE_APP)
    }
}
