package com.ble.chatting.client

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.ble.chatting.client.ble.BleLibrary
import com.ble.chatting.client.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var mApplication = MyApplication.application()
    private lateinit var binding: ActivityMainBinding

    var syncObj = Any()
    var scanResults: ArrayList<BluetoothDevice> = ArrayList()
    var listAdapter = RecyclerView

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