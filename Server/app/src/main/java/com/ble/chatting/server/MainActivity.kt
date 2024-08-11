package com.ble.chatting.server

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
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
import com.ble.chatting.server.ble.BleLibrary
import com.ble.chatting.server.ble.Const
import com.ble.chatting.server.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    var mApplication = MyApplication.application()
    private lateinit var binding: ActivityMainBinding

    private lateinit var bluetoothManager: BluetoothManager
    private var mGattSend : BluetoothGattServer? = null
    private var selectedBleDevice: BluetoothDevice? = null
    private var connectedBleDeviceList: ArrayList<BluetoothDevice> = ArrayList()

    private var listAdapter = RecyclerViewAdapter(this)
    private var index = -1
    private var messageArray = ArrayList<ByteArray>()
    private var receiveByteArray = ByteArrayOutputStream()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        initListener()
        initBleLibrary()
        setUpGattServer()
    }

    override fun onDestroy() {
        super.onDestroy()

        BleLibrary.stopAdvertise()
        stopGattServer()
    }

    private fun init() {
        binding.apply {
            val defaultAnimator = DefaultItemAnimator()
            defaultAnimator.addDuration = 1000
            defaultAnimator.removeDuration = 1000
            recyclerView.itemAnimator = defaultAnimator
            recyclerView.adapter = listAdapter
        }
    }

    private fun initListener() {
        binding.apply {
            etxtSendText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (selectedBleDevice == null) {
                        Toast.makeText(this, "선택된 디바이스가 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedBleDevice?.let {
                            try {
                                val characteristic = mGattSend!!.getService(Const.SERVICE_UUID).getCharacteristic(Const.MESSAGE_UUID)
                                characteristic.setValue(etxtSendText.text.toString())
                                mGattSend!!.notifyCharacteristicChanged(it, characteristic, false)
                                etxtSendText.setText("")
                            } catch (e: SecurityException) {

                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }

            btnSendText.setOnClickListener {
                if (selectedBleDevice == null) {
                    Toast.makeText(this@MainActivity, "선택된 디바이스가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    selectedBleDevice?.let {
                        try {
                            val characteristic = mGattSend!!.getService(Const.SERVICE_UUID).getCharacteristic(Const.MESSAGE_UUID)
                            characteristic.setValue(etxtSendText.text.toString())
                            mGattSend!!.notifyCharacteristicChanged(it, characteristic, false)
                            etxtSendText.setText("")
                        } catch (e: SecurityException) {

                        }
                    }
                }
            }
        }
    }

    private fun initBleLibrary() {
        if (isBluetoothPossible() && checkConnectPermission() && checkScanPermission()) {
            BleLibrary.initialize(mApplication)
            BleLibrary.startAdvertise()
        } else {
            getRequestPermission()
        }
    }

    private fun setUpGattServer() {
        
    }

    private fun stopGattServer() {
        try {
            mGattSend?.close()
        } catch (e: SecurityException) {

        }
    }

    private fun getMessage(device: BluetoothDevice, message: Message) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                binding.tvChattingText.text = "[${device}] 에서 보낸 메세지\n\n${message}"
                delay(3000)
                binding.tvChattingText.text = ""
            }
        } catch (e: SecurityException) {

        }
    }

    private fun getImage(imageByteArray: ByteArray) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("asdf", "imageByteArray Size : ${imageByteArray.size}")
                val image = byteArrayToBitmap(imageByteArray)
                binding.imgView.setImageBitmap(image)
            }
        } catch (_: SecurityException) {

        }
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return bitmap
    }

    private fun isBluetoothPossible(): Boolean {
        return if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE 미지원", Toast.LENGTH_SHORT).show()
            return false
        } else {
            true
        }
    }

    private fun getRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT), 1)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        init()
    }

    private fun checkConnectPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkScanPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    inner class RecyclerViewAdapter(private val context: Context): RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.view_item_connected, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return connectedBleDeviceList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(connectedBleDeviceList[position], position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvConnectedItemName = itemView.findViewById<TextView>(R.id.tvConnectedItemName)
        fun bind(item: BluetoothDevice, position: Int) {
            binding.apply {
                try {
                    tvConnectedItemName.text = item.toString()
                } catch (_: SecurityException) {

                }

                tvConnectedItemName.isPressed = index == position

                tvConnectedItemName.setOnClickListener {
                    index = position
                    selectedBleDevice = connectedBleDeviceList[index]
                }
            }
        }
    }
}