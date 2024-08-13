package com.ble.chatting.client.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.ble.chatting.client.ble.gatt.BleConnectItem
import com.ble.chatting.client.ble.gatt.BleGattReceiveListener
import com.ble.chatting.client.ble.scan.BleScanItem
import com.ble.chatting.client.ble.scan.BleScanStatusListener
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object BleLibrary {

    private lateinit var application: Application
    private lateinit var mBleManager: BluetoothManager
    private lateinit var mBleAdapter: BluetoothAdapter

    private val bleScanItemMap: ConcurrentMap<String, BleScanItem> = ConcurrentHashMap()
    private val bleConnectItemMap: ConcurrentMap<String, BleConnectItem> = ConcurrentHashMap()

    fun initialize(application: Application) {

        if (application == null) {
            throw IllegalArgumentException("Context is Null")
        }

        this.application = application
        this.mBleManager = application.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.mBleAdapter = mBleManager.adapter
    }

    fun startScan(bleScanStatusListener: BleScanStatusListener) {
        val bleFindScanItem = bleScanItemMap[bleScanStatusListener.javaClass.name]

        bleFindScanItem?.let {
            if (it.isScanning()) {
                bleScanStatusListener.onStartStatus()
                return
            }
        }

        val bleScanItem = BleScanItem(context = application.applicationContext)
        bleScanItem.setBleScanStatusListener(bleScanStatusListener)
        bleScanItem.startScan()

        bleScanItemMap[bleScanStatusListener.javaClass.name] = bleScanItem
    }

    fun stopScan(bleScanStatusListener: BleScanStatusListener) {
        val bleFindScanItem = bleScanItemMap[bleScanStatusListener.javaClass.name]

        bleFindScanItem?.let {
            it.stopScan()
        }
    }

    fun connect(bleAddress: String, bleGattConnectListener: BleGattReceiveListener) {
        val bleFindConnectItem = bleConnectItemMap[bleGattConnectListener.javaClass.name]

        bleFindConnectItem?.let {

        }

        val bleConnectItem = BleConnectItem(application.applicationContext)
        bleConnectItem.setBleGattConnectListener(bleGattConnectListener)
        bleConnectItem.connect(bleAddress)

        bleConnectItemMap[bleGattConnectListener.javaClass.name] = bleConnectItem
    }

    fun disconnect(bleGattConnectListener: BleGattReceiveListener) {
        val bleFindConnectMap = bleConnectItemMap[bleGattConnectListener.javaClass.name]

        bleFindConnectMap?.let {
            it.disconnect()
        }
    }
}