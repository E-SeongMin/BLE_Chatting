package com.ble.chatting.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.ble.chatting.client.ble.scan.BleScanData
import com.ble.chatting.client.ble.scan.BleScanStatusListener
import com.ble.chatting.client.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var mApplication = MyApplication.application()
    private lateinit var binding: ActivityMainBinding

    var syncObj = Any()
    var scanResults: ArrayList<BluetoothDevice> = ArrayList()
    var listAdapter = RecyclerViewAdapter(this)

    private val bleScanStatusListener = object : BleScanStatusListener {
        override fun onScanResult(bleScanData: BleScanData) {
            addScanReult(bleScanData)
        }

        override fun onStartStatus() {

        }

        override fun onStopStatus() {

        }
    }

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
        initBleLibrary()
        initListener()
    }

    private fun init() {
        binding.apply {
            recyclerView.adapter = listAdapter
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            tvScanning.visibility = View.GONE

            val defaultAnimator = DefaultItemAnimator()
            defaultAnimator.addDuration = 1000

            recyclerView.itemAnimator = defaultAnimator
        }
    }

    private fun refreshAdapter() {
        scanResults.clear()
        binding.recyclerView.visibility = View.GONE
        listAdapter.notifyDataSetChanged()
    }

    private fun initBleLibrary() {
        if (isBluetoothPossible() && checkConnectPermission() && checkScanPermission()) {
            BleLibrary.initialize(mApplication)
        } else {
            getRequestPermission()
        }
    }

    private fun initListener() {
        binding.apply {
            btnStartScan.setOnClickListener {
                Log.d("asdf", "click btnStartScan")
                refreshAdapter()
                progressBar.visibility = View.VISIBLE
                tvScanning.visibility = View.VISIBLE
                BleLibrary.startScan(bleScanStatusListener)
            }

            btnStopScan.setOnClickListener {
                Log.d("asdf", "click btnStopScan")
                progressBar.visibility = View.GONE
                tvScanning.visibility = View.GONE
                BleLibrary.stopScan(bleScanStatusListener)
                refreshAdapter()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshAdapter()
    }

    private fun addScanReult(bleScanData: BleScanData) {
        synchronized(syncObj) {
            try {
                val device: BluetoothDevice = bleScanData.result.device
                val deviceAddress: String = device?.address ?: "not address"

                for (dev in scanResults) {
                    if (dev.address == deviceAddress) {
                        return
                    }
                }

                if (device.name == null) {
                    return
                }

                scanResults.add(device)

                if (scanResults.size > 0 && binding.recyclerView.visibility == View.GONE) {
                    binding.recyclerView.visibility = View.VISIBLE
                }

                listAdapter.notifyItemInserted(scanResults.size - 1)

                Log.d("asdf", "AddMainScanResult name : ${device.name}")
                Log.d("asdf", "MainScanResults size : ${scanResults.size}")
            } catch (e: SecurityException) {

            }
        }
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
            val view = LayoutInflater.from(context).inflate(R.layout.view_item_scan_result, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return scanResults.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(scanResults[position])
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var resultName = itemView.findViewById<TextView>(R.id.tvScanResultName)
        fun bind(item: BluetoothDevice) {
            binding.apply {
                try {
                    resultName.text = item.name

                    resultName.setOnClickListener {
                        BleLibrary.stopScan(bleScanStatusListener)

                        val intent = Intent(this@MainActivity, ChattingActivity::class.java)
                        intent.putExtra("bleDevice", item)
                        startActivity(intent)
                    }
                } catch (_: SecurityException) {

                }
            }
        }
    }
}