package com.ble.chatting.server.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.ble.chatting.server.ble.advertise.BleAdvertise

object BleLibrary {
    private lateinit var mBleManager: BluetoothManager
    private lateinit var mBleAdapter: BluetoothAdapter
    private lateinit var mBleAdvertise: BleAdvertise

    fun initialize(application: Application) {
        this.mBleManager = application.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.mBleAdapter = mBleManager.adapter
        this.mBleAdvertise = BleAdvertise()
    }

    fun startAdvertise() {
        mBleAdvertise.startAdvertisement()
    }

    fun stopAdvertise() {
        mBleAdvertise.stopAdvertising()
    }
}