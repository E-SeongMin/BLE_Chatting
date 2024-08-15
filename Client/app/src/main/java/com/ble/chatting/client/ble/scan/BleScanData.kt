package com.ble.chatting.client.ble.scan

import android.bluetooth.le.ScanResult


data class BleScanData(val actName: String, val callbackType: Int, val result: ScanResult)
