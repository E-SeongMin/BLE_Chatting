package com.ble.chatting.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ble.chatting.client.ble.BleLibrary
import com.ble.chatting.client.ble.Const
import com.ble.chatting.client.ble.gatt.BleGattReceiveListener
import com.ble.chatting.client.ble.gatt.BleReceiveData
import com.ble.chatting.client.ble.scan.BleScanData
import com.ble.chatting.client.ble.scan.BleScanStatusListener
import com.ble.chatting.client.databinding.ActivityChattingBinding
import com.ble.chatting.client.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.CRC32

class ChattingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChattingBinding

    lateinit var bleDevice: BluetoothDevice

    private var mGattReceive: BluetoothGatt? = null
    private var messageSendCharacteristic: BluetoothGattCharacteristic? = null
    private var messageReceiveCharacteristic: BluetoothGattCharacteristic? = null

    val DEFAULT_MAX_BYTE_SIZE = 20

    private val mBleGattClientListener = object : BleGattReceiveListener {
        override fun onConnectionStateChange(bleConnectData: BleReceiveData) {
            try {
                mGattReceive = bleConnectData.gatt
                val mNewState = bleConnectData.newState
                Log.d("asdf", "mBleGattClientListener onConnectionStateChange")
                if (mNewState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("asdf", "STATE_CONNECTED")
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.tvConnectDeviceName.text = "연결 : ${mGattReceive?.device?.name}"
                        mGattReceive?.discoverServices()
                    }
                } else if (mNewState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("asdf", "STATE_DISCONNECTED")
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.tvConnectDeviceName.text = "미연결"
                    }
                    BleLibrary.disconnect(this)
                }
            } catch (e: SecurityException) {

            }
        }

        override fun onServicesDiscovered(bleConnectData: BleReceiveData) {
            try {
                val mStatus = bleConnectData.status
                Log.d("asdf", "mBleGattClientListener onServicesDiscovered")
                if (mStatus == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("asdf", "GATT_SUCCESS")
                    val service = mGattReceive?.getService(Const.SERVICE_UUID)

                    if (service != null) {
                        messageSendCharacteristic = service.getCharacteristic(Const.MESSAGE_UUID)
                    } else {
                        Log.d("asdf", "Service not found")
                    }
                }

                enableNotifications()
            } catch (e: SecurityException) {

            }
        }

        override fun onNotifyChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            Log.d("asdf", "onNotifyChanged")
            val message = value.toString(Charsets.UTF_8)
            getMessage(message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        initListener()
        connectGatt()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            mGattReceive!!.setCharacteristicNotification(messageReceiveCharacteristic, false)
            BleLibrary.disconnect(mBleGattClientListener)
        } catch (e: SecurityException) {

        }
    }

    private fun init() {
        binding.apply {
            tvConnectDeviceName.text = "연결 중..."
        }
    }

    private fun initListener() {
        binding.apply {
            etxtSendText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendBigSizeMessage(etxtSendText.text.toString(), "Text")
                    etxtSendText.setText("")
                    true
                } else {
                    false
                }
            }

            btnSendText.setOnClickListener {
                Log.d("asdf", "click btnSendText")
                sendBigSizeMessage(etxtSendText.text.toString(), "Text")
            }

            btnSendImage.setOnClickListener {
                Log.d("asdf", "click btnSendImage")
                sendBigSizeMessage(etxtSendText.text.toString(), "Image")
            }
        }
    }

    private fun connectGatt() {
        bleDevice = intent.extras!!.getParcelable("bleDevice")!!
        BleLibrary.connect(bleDevice!!.address, mBleGattClientListener)
    }

    private fun enableNotifications() {
        try {
            messageReceiveCharacteristic = mGattReceive!!.getService(Const.SERVICE_UUID).getCharacteristic(Const.MESSAGE_UUID)
            messageReceiveCharacteristic?.getDescriptor(Const.DESCRIPTION_UUID).let {
                mGattReceive!!.setCharacteristicNotification(messageReceiveCharacteristic, true)
                mGattReceive!!.writeDescriptor(it)
            }
        } catch (e: SecurityException) {

        }
    }

    private fun getMessage(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                tvChattingText.text = "[서버] 에서 보낸 메시지\n\n${message}"
                delay(3000)
                tvChattingText.text = ""
            }
        }
    }

    private fun sendBigSizeMessage(message: String, type: String) {
        when (type) {
            "Text" -> {
                try {
                    binding.etxtSendText.setText("")
                    val byteArray = getTextByteArray(message)

                    for (i in byteArray.indices) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            messageSendCharacteristic?.let { characteristic ->
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                                characteristic.value = byteArray[i]
                                mGattReceive?.let {
                                    Log.d("asdf", "${i}번째 보내는 중")
                                    it.writeCharacteristic(messageSendCharacteristic)
                                }
                            }
                        }, (500*i).toLong())
                    }
                } catch (e: SecurityException) {

                }
            }
            "Image" -> {
                try {
                    val outputStream = assets.open("dot_arrive.png")
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    val output = ByteArrayOutputStream()
                    while (outputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    val file = output.toByteArray()
                    val imageByteArray = file

                    var crcCheck = CRC32()
                    crcCheck.update(imageByteArray)
                    Log.d("asdf", "CRC32 : ${crcCheck.value}")

                    val resultImageArray = getImageByteArray(imageByteArray)
                    Log.d("asdf", "resultImageByteArray : ${resultImageArray.size}")

                    for (i in resultImageArray.indices) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            messageSendCharacteristic?.let { characteristic ->
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                                characteristic.value = resultImageArray[i]
                                val splitByteArray = resultImageArray[i]

                                val crcCheck = CRC32()
                                crcCheck.update(splitByteArray)
                                Log.d("asdf", "Split CRC32 Index : ${i}, CRC32: ${crcCheck.value}")

                                mGattReceive?.let {
                                    Log.d("asdf", "${i}번째 보내는 중")
                                    it.writeCharacteristic(messageSendCharacteristic)
                                }
                            }
                        }, (500*i).toLong())
                    }

                } catch (e: SecurityException) {

                }
            }
        }
    }

    private fun toHexString(byteArray: ByteArray): String {
        val sbx = StringBuilder()
        for (i in byteArray.indices) {
            sbx.append(String.format("%02X", byteArray[i]))
        }
        return sbx.toString()
    }

    private fun getImageByteArray(imageByteArray: ByteArray): ArrayList<ByteArray> {
        val imageByteLength = imageByteArray.size
        var addSize = 0
        val maxIndexSize = imageByteLength / (DEFAULT_MAX_BYTE_SIZE - 2)
        val resultArray = ArrayList<ByteArray>()
        for (i in 0 .. maxIndexSize) {
            val outputStream = ByteArrayOutputStream()
            outputStream.write(0x04)
            if (i == maxIndexSize) {
                outputStream.write(0x02)
            } else {
                outputStream.write(0x01)
            }

            Log.d("asdf", "Write ImageByteArray size : ${imageByteArray.size}, addSize: ${addSize}")

            if (imageByteArray.size - addSize < 18) {
                outputStream.write(imageByteArray, i * 18, imageByteArray.size - addSize)
                Log.d("asdf", "Write addSize : ${imageByteArray.size - addSize}")
            } else {
                outputStream.write(imageByteArray, i * 18, 18)
                Log.d("asdf", "Write offset i * 18 : ${i * 18}")
                Log.d("asdf", "Write addSize : ${addSize}")
            }

            resultArray.add(outputStream.toByteArray())
            Log.d("asdf", "Send Byte to String : ${toHexString(outputStream.toByteArray())}")
            outputStream.close()

            addSize += 18
        }

        return resultArray
    }

    private fun getTextByteArray(message: String): Array<ByteArray> {
        val messageByteArray = message.toByteArray(Charset.forName("UTF-8"))
        val messageByteLength = messageByteArray.size
        val maxIndexSize = messageByteLength / (DEFAULT_MAX_BYTE_SIZE - 2)
        Log.d("asdf", "messageByteLength : ${messageByteLength}")
        Log.d("asdf", "maxIndexSize : ${maxIndexSize}")

        val sendingArray = Array(maxIndexSize + 1) {
            ByteArray(20)
        }

        for (i in 0 .. maxIndexSize) {
            sendingArray[i][0] = 0x03
            if (i == maxIndexSize) {
                sendingArray[i][1] = 0x02
            } else {
                sendingArray[i][1] = 0x01
            }

            for (j in 2 until DEFAULT_MAX_BYTE_SIZE) {
                val index = i * (DEFAULT_MAX_BYTE_SIZE - 2) + j - 2
                if (index >= messageByteLength) {
                    return sendingArray
                }
                sendingArray[i][j] = messageByteArray[i * (DEFAULT_MAX_BYTE_SIZE - 2) + j - 2]
            }
        }

        return sendingArray
    }
}