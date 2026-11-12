package com.faultcall.wear

import android.app.Application
import com.faultcall.wear.communication.WearDataLayerManager
import com.faultcall.wear.haptics.HapticManager

class FaultCallWearApplication : Application() {

    lateinit var dataLayerManager: WearDataLayerManager
        private set

    lateinit var hapticManager: HapticManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataLayerManager = WearDataLayerManager(this)
        hapticManager = HapticManager(this)
    }

    companion object {
        lateinit var instance: FaultCallWearApplication
            private set
    }
}
