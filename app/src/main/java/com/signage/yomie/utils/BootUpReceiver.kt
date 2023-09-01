package com.signage.yomie.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.signage.yomie.RegistrationActivity
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1!!.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val intent = Intent(p0, RegistrationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    p0?.startActivity(intent)
                } catch (ex: Exception) {
                    AppUtils.logError(ApiInterfaceErrorLog.TYPE_ERROR, AppUtils.appendExp(ex))
                }

            }, 40000)
        }
    }
}


