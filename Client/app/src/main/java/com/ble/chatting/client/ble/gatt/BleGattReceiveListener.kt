package com.ble.chatting.client.ble.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

interface BleGattReceiveListener {
    fun onConnectionStateChange(bleConnectData: BleReceiveData)
    fun onServicesDiscovered(bleConnectData: BleReceiveData)
    fun onNotifyChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray)
}