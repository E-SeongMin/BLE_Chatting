package com.ble.chatting.client.ble.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log

class BleConnectItem(private val context: Context) {
    private var bleGattConnectListener: BleGattReceiveListener? = null
    private lateinit var mGatt: BluetoothGatt
    private var isConnecting: Boolean = false

    fun setBleGattConnectListener(bleGattConnectListener: BleGattReceiveListener) {
        this.bleGattConnectListener = bleGattConnectListener
    }

    fun connect(address: String) {
        try {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mGatt = manager.adapter.getRemoteDevice(address).connectGatt(context, false, gattCallback)
            isConnecting = true
        } catch (e: SecurityException) {

        }
    }

    fun disconnect() {
        try {
            mGatt.disconnect()
            mGatt.close()
            isConnecting = false
        } catch (e: SecurityException) {

        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("asdf", "BluetoothGattCallback onConnectionStateChange")
            bleGattConnectListener?.onConnectionStateChange(BleReceiveData(this.javaClass.name, gatt, status, newState, null, null, null))
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d("asdf", "BluetoothGattCallback onServicesDiscovered")
            bleGattConnectListener?.onServicesDiscovered(BleReceiveData(this.javaClass.name, gatt, status, null, null, null, null))
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            bleGattConnectListener?.onNotifyChanged(gatt, characteristic, value)
        }
    }
}