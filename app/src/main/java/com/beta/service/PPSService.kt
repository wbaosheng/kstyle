package com.beta.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.huawei.android.hms.ppskit.IPPSChannelInfoService

class PPSService : Service() {

    override fun onBind(intent: Intent?): IBinder {
        return PPSBinder()
    }

    class PPSBinder : IPPSChannelInfoService.Stub() {
        override fun getChannelInfo(): String {
            return "hello pps"
        }
    }

}