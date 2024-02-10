package com.lexus.ISClimate.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyFloatingService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return  null
    }
}