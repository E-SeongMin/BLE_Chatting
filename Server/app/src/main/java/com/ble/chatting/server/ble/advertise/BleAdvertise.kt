package com.ble.chatting.server.ble.advertise

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import com.ble.chatting.server.ble.Const

class BleAdvertise {
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings? = buildAdvertiseSettings()
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    fun startAdvertisement() {
        advertiser = adapter.bluetoothLeAdvertiser

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()

            try {
                advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
            } catch (e: SecurityException) {

            }
        }
    }

    fun stopAdvertising() {
        Log.d("asdf", "Stop Advertising with advertiser $advertiser")
        if (advertiseCallback != null) {
            try {
                advertiser?.stopAdvertising(advertiseCallback)
            } catch (e: SecurityException) {

            }
        }
    }

    private fun buildAdvertiseData(): AdvertiseData {
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(Const.SERVICE_UUID))
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)

        return dataBuilder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    private class DeviceAdvertiseCallback: AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("asdf", "Advertise Successfully started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMsg = "Advertise failed with error : $errorCode"
            Log.d("asdf", "${errorMsg}")
        }
    }
}