package com.ble.chatting.client

import android.app.Application
import android.content.Context
class MyApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance : MyApplication? = null

        @JvmStatic
        fun applicationContext(): Context = instance!!.applicationContext

        @JvmStatic
        fun application(): MyApplication = instance!!
    }
}