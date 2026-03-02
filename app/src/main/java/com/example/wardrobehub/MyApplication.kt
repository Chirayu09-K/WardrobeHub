package com.example.wardrobehub

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "dzclxipb2",
            "api_key" to "693744846597828",
            "api_secret" to "LCLytgUmppwZr1e4FeaqvvCsX10"
        )
        MediaManager.init(this, config)
    }
}