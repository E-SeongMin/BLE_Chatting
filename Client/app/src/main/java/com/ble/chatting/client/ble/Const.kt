package com.ble.chatting.client.ble

import java.util.UUID

object Const {
    const val BLE_MILL_IS_IN_FUTURE_DEFAULT = 1000 * 5L
    const val BLE_COUNTDOWN_INTERVAL = 1000L

    val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    val DESCRIPTION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}