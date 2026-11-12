package com.faultcall.mobile.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioRouteInfo(
    val isBluetoothConnected: Boolean,
    val deviceName: String,
    val routeType: String,
    val isExternalSpeaker: Boolean
)

/**
 * Diagnostics helper for monitoring Android audio routing to external Bluetooth speakers.
 */
class BluetoothAudioHelper(
    private val context: Context
) {
    private val tag = "BluetoothAudioHelper"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _currentRoute = MutableStateFlow(inspectCurrentAudioRoute())
    val currentRoute: StateFlow<AudioRouteInfo> = _currentRoute.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            Log.d(tag, "Audio devices added: ${addedDevices?.joinToString { it.productName.toString() }}")
            updateRoute()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            Log.d(tag, "Audio devices removed: ${removedDevices?.joinToString { it.productName.toString() }}")
            updateRoute()
        }
    }

    init {
        try {
            audioManager.registerAudioDeviceCallback(deviceCallback, null)
        } catch (e: Exception) {
            Log.w(tag, "Failed to register audio device callback: ${e.message}")
        }
    }

    fun updateRoute() {
        _currentRoute.value = inspectCurrentAudioRoute()
    }

    private fun inspectCurrentAudioRoute(): AudioRouteInfo {
        try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

            // Look for Bluetooth A2DP, BLE Headset, or SCO devices
            val btDevice = devices.firstOrNull { device ->
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET,
                    AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
                    else -> false
                }
            }

            if (btDevice != null) {
                val name = btDevice.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Connected Bluetooth Device"
                val type = when (btDevice.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                    AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE Speaker"
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE Headset"
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
                    else -> "Bluetooth"
                }
                return AudioRouteInfo(
                    isBluetoothConnected = true,
                    deviceName = name,
                    routeType = type,
                    isExternalSpeaker = true
                )
            }

            // Look for wired headphones / aux
            val wiredDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE
            }

            if (wiredDevice != null) {
                return AudioRouteInfo(
                    isBluetoothConnected = false,
                    deviceName = wiredDevice.productName?.toString() ?: "Wired / USB Output",
                    routeType = "Wired Connection",
                    isExternalSpeaker = true
                )
            }

            // Fallback to built-in phone speaker
            return AudioRouteInfo(
                isBluetoothConnected = false,
                deviceName = "Phone Speaker",
                routeType = "Internal Speaker",
                isExternalSpeaker = false
            )
        } catch (e: Exception) {
            Log.w(tag, "Error inspecting audio route: ${e.message}")
            return AudioRouteInfo(
                isBluetoothConnected = false,
                deviceName = "Audio Output",
                routeType = "Default",
                isExternalSpeaker = false
            )
        }
    }

    fun unregister() {
        try {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
        } catch (e: Exception) {
            Log.w(tag, "Error unregistering callback: ${e.message}")
        }
    }
}
