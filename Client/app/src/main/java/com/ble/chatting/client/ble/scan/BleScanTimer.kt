package com.ble.chatting.client.ble.scan

import android.os.CountDownTimer
import com.ble.chatting.client.ble.Const

class BleScanTimer {

    lateinit var mBleScanTimerListener: BleScanTimerListener
    var mMillisInFuture = Const.BLE_MILL_IS_IN_FUTURE_DEFAULT
    var mCountDownInterval = Const.BLE_COUNTDOWN_INTERVAL

    var mCountDownTimer = object : CountDownTimer(mMillisInFuture, mCountDownInterval) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            if (::mBleScanTimerListener.isInitialized) {
                mBleScanTimerListener.onFinish()
            }
        }
    }

    fun startTimer() {
        mCountDownTimer.start()
    }

    fun setTimerInit(time: Long, bleScanTimerListener: BleScanTimerListener) {
        this.mMillisInFuture = time * 1000L
        this.mBleScanTimerListener = bleScanTimerListener
    }
}