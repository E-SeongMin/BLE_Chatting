package com.ble.chatting.client.ble.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

data class BleReceiveData(
    val actName: String,
    val gatt: BluetoothGatt?,
    val status: Int?,
    val newState: Int?,
    val mtu: Int?,
    val characteristic: BluetoothGattCharacteristic?,
    val descriptor: BluetoothGattDescriptor?
)
