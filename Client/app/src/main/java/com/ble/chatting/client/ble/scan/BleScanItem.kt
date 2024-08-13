package com.ble.chatting.client.ble.scan

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.ble.chatting.client.ble.Const

class BleScanItem(private val context: Context) {
    private var bleScanStatusListener: BleScanStatusListener? = null
    private var isScanning: Boolean = false
    private lateinit var scanFilters: List<ScanFilter>
    private lateinit var scanSettings: ScanSettings

    fun setBleScanStatusListener(bleScanStatusListener: BleScanStatusListener) {
        this.bleScanStatusListener = bleScanStatusListener
    }

    fun startScan() {
        try {
            isScanning = true
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            scanFilters = buildScanFilters()
            scanSettings = buildScanSettings()
            manager.adapter.bluetoothLeScanner.startScan(mScanCallback)
        } catch (e: SecurityException) {

        }
    }

    fun stopScan() {
        try {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            manager.adapter.bluetoothLeScanner.stopScan(mScanCallback)
            isScanning = false
        } catch (e: SecurityException) {

        }
    }

    private fun buildScanFilters() : List<ScanFilter> {
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(Const.SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }

    private fun buildScanSettings() : ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }

    private var mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bleScanStatusListener?.onScanResult(BleScanData(this.javaClass.name, callbackType, result))
        }
    }

    fun isScanning() : Boolean {
        return this.isScanning
    }
}