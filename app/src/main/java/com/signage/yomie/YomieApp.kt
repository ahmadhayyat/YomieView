package com.signage.yomie

import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.signage.yomie.commons.AppPreferences


val preferences: AppPreferences by lazy {
    YomieApp.appPreferences!!
}
val socketManager: SocketManager by lazy {
    YomieApp.socketManager!!
}

class YomieApp : Application() {

    companion object {
        fun connectSocket() {
            socketManager = SocketManager()
        }

        fun getContext(): Context {
            return context
        }

        var appPreferences: AppPreferences? = null
        private lateinit var context: Context
        var socketManager: SocketManager? = null
    }


    override fun onCreate() {
        super.onCreate()
        //ANRWatchDog().setReportThreadNamePrefix("ANR:").start()
        appPreferences = AppPreferences(applicationContext)
        appPreferences!!.setBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE, false)
        appPreferences!!.setBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED, false)
        context = applicationContext
        val config = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        //socketManager = SocketManager()
        PRDownloader.initialize(applicationContext, config)
        Bugsnag.start(this)
        appPreferences!!.getString(AppPreferences.KEY_PLAYER_ID)
            ?.let {
                FirebaseApp.initializeApp(this)
                Firebase.crashlytics.setUserId(it)
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
                FirebaseCrashlytics.getInstance().sendUnsentReports()
            }
//changeLog change check
    }
}