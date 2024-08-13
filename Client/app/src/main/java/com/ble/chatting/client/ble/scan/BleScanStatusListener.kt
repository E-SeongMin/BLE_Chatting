package com.ble.chatting.client.ble.scan

interface BleScanStatusListener {
    fun onScanResult(bleScanData: BleScanData)
    fun onStartStatus()
    fun onStopStatus()
}