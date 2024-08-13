package com.ble.chatting.client.ble.gatt

import android.bluetooth.BluetoothGattCharacteristic

data class BleSendData(
    val actName: String,
    val requestId: Int?,
    val characteristic: BluetoothGattCharacteristic?,
    val prepareWrite: Boolean?,
    val responseNeeded: Boolean?,
    val offset: Int?,
    val value: ByteArray?
)
