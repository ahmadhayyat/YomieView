package com.signage.yomie

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.signage.yomie.commons.*
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject


class SocketManager : WebSocketListener() {

    private val CLOSE_STATUS = 1000
    private var playListString = ""
    private var rssFeedString = ""
    private var isNoPlaylist = false
    var isConnected = false
    var isFeedEvent = false

    companion object {
        const val TYPE_PLAYLIST_DATA = "PlaylistData"
        const val TYPE_PLAYLIST_DEL = "PlaylistDel"
        const val TYPE_USER_CHANGE = "UserChange"
        const val TYPE_PLAYLIST_ADD = "PlaylistAdd"
        private const val TYPE_PLAYER = "Player"
        private const val TYPE_PLAYER_DEL = "PlayerDel"
        const val TYPE_PLAYBACK = "Playback"
        const val PLAYER_FIELD_RSS_FEED_SLIDE = "RssFeedSlides"
        const val PLAYER_FIELD_RSS_FEED = "RssFeed"
        const val PLAYER_FIELD_PLAYLIST = "Playlist"
        const val PLAYER_FIELD_ORIENTATION = "Orientation"
        const val PLAYER_FIELD_OVERLAY_URL = "OverlayUrl"
        const val PLAYER_FIELD_RSS_STYLE = "rssTickerStyle"
        const val PLAYER_FIELD_RSS_HEIGHT = "rssTickerHeight"
        const val PLAYER_FIELD_REFRESH_TIME = "PlayerRefreshTime"
        const val PLAYER_FIELD_LANGUAGE = "Language"


        var mWebSocket: WebSocket? = null
        var mClient: OkHttpClient? = null
        private lateinit var socketInterface: SocketInterface
        private var mediaPlayerInterface: MediaPlayerInterface? = null

        fun setMediaInterface(mPlayerInterface: MediaPlayerInterface) {
            mediaPlayerInterface = mPlayerInterface
        }

        fun setSocketInterface(sInterface: SocketInterface) {
            socketInterface = sInterface
        }
    }

    init {
        if (!isConnected) {
            connectSocket()
            isConnected = true
            preferences.setBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED, isConnected)
        }
    }

    fun connectSocket() {
        if (mClient != null) mClient = null
        mClient = OkHttpClient()
        val calls = mClient!!.dispatcher.queuedCalls()
        calls.forEachIndexed { index, call ->
            if (call.request().tag()!! == "socreq") {
                call.cancel()
                if (index == calls.size - 1) {
                    val request =
                        Request.Builder().url(MediaPlayerActivity.WEB_SOCKET_URL).tag("socreq")
                            .build()
                    mClient!!.newWebSocket(request, this)
                }
            }
        }

        if (calls.isEmpty()) {
            val request =
                Request.Builder().url(MediaPlayerActivity.WEB_SOCKET_URL).tag("socreq").build()
            mClient!!.newWebSocket(request, this)
        }

    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        AppConstants.socketReconnectionTime = 30000
        mWebSocket = null
        mWebSocket = webSocket
        Log.i("SOCKET", "on opened")
        AppUtils.logError("socket connected", ApiInterfaceErrorLog.TYPE_INFO)
        isConnected = true
        preferences.setBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED, isConnected)
        socketManager.sendEvent(
            SocketParams().registerDevice(
                preferences.getString(AppPreferences.KEY_DEVICE_ID)!!,
                preferences.getString(AppPreferences.KEY_PLAYER_ID)!!
            )
        )
        if (AppConstants.isToCheckPlaylist) AppUtils.checkForPlaylistChange()
        super.onOpen(webSocket, response)
    }

    fun sendEvent(param: String) {
        try {
            if (mWebSocket != null) {
                mWebSocket!!.send(param)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(
                "${this::class.simpleName}\t${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
    }


    fun closeSocket() = mWebSocket!!.close(CLOSE_STATUS, "Socket close!")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            Log.i("SOCKETMsg", text)
            AppUtils.logError("socket event received", ApiInterfaceErrorLog.TYPE_INFO)
            val jsonObject = JSONObject(text)
            val type = jsonObject.getString("Type")
            if (!type.equals(TYPE_PLAYER_DEL)) mediaPlayerInterface?.onChanges(MediaPlayerActivity.SHOW_SYNC)
            when (type) {
                TYPE_PLAYER_DEL -> {
                    mediaPlayerInterface?.onDeletePlayer()
                }
                TYPE_PLAYLIST_DATA -> socketInterface.onDataCaptured(text, TYPE_PLAYLIST_DATA)
                TYPE_PLAYLIST_ADD -> socketInterface.onDataCaptured(text, TYPE_PLAYLIST_ADD)
                TYPE_PLAYLIST_DEL -> socketInterface.onDataCaptured(text, TYPE_PLAYLIST_DEL)
                TYPE_USER_CHANGE -> {
                    isNoPlaylist = true
                    val intent = Intent(YomieApp.getContext(), MediaActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    YomieApp.getContext().startActivity(intent)
                }
                TYPE_PLAYBACK, TYPE_PLAYER -> {
                    val obj = jsonObject.getJSONObject("Data")
                    if (obj.getString("Type").equals(TYPE_PLAYBACK)) {
                        val data = obj.getJSONObject("Data")
                        socketInterface.onDataCaptured(data.toString(), TYPE_PLAYBACK)
                    } else {
                        val jsonArray = obj.getJSONArray("Data")
                        for (i in 0 until jsonArray.length()) {
                            val zeroObj = jsonArray.getJSONObject(i)
                            val fieldName = zeroObj.getString("FieldName")
                            var fieldValueArr = JSONArray()
                            var fieldValueStr = ""
                            if (fieldName.equals(PLAYER_FIELD_ORIENTATION) || fieldName.equals(
                                    PLAYER_FIELD_OVERLAY_URL
                                ) || fieldName.equals(PLAYER_FIELD_REFRESH_TIME) || fieldName.equals(
                                    (PLAYER_FIELD_LANGUAGE)
                                ) || fieldName.equals((PLAYER_FIELD_RSS_STYLE)) || fieldName.equals(
                                    (PLAYER_FIELD_RSS_HEIGHT)
                                )
                            ) {
                                fieldValueStr = zeroObj.getString("FieldValue")
                            } else {
                                fieldValueArr = zeroObj.getJSONArray("FieldValue")
                            }
                            when (zeroObj.getString("FieldName")) {
                                PLAYER_FIELD_RSS_FEED_SLIDE, PLAYER_FIELD_RSS_FEED -> {
                                    rssFeedString = fieldValueArr.toString()
                                    isFeedEvent = true
                                }
                                PLAYER_FIELD_PLAYLIST -> {
                                    if (fieldValueArr.length() == 0 || fieldValueArr.isNull(0)) {
                                        isNoPlaylist = true
                                        val intent =
                                            Intent(YomieApp.getContext(), MediaActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        YomieApp.getContext().startActivity(intent)
                                    } else {
                                        isNoPlaylist = false
                                        playListString = fieldValueArr.toString()
                                    }
                                }
                                PLAYER_FIELD_ORIENTATION -> {
                                    preferences.setString(
                                        AppPreferences.KEY_PLAYER_ORIENTATION, fieldValueStr
                                    )
                                    mediaPlayerInterface?.onChanges(MediaPlayerActivity.CHANGE_ORIENTATION)
                                }
                                PLAYER_FIELD_OVERLAY_URL -> {
                                    preferences.setString(
                                        AppPreferences.KEY_PLAYER_OVERLAY_URL, fieldValueStr
                                    )
                                }
                                PLAYER_FIELD_REFRESH_TIME -> {
                                    preferences.setString(
                                        AppPreferences.KEY_PLAYER_REFRESH_TIME, fieldValueStr
                                    )
                                    if (!isFeedEvent) rssFeedString = AppConstants.TYPE_GEN_RSS
                                }
                                PLAYER_FIELD_LANGUAGE -> {
                                    preferences.setString(
                                        AppPreferences.KEY_PLAYER_LANGUAGE, fieldValueStr
                                    )
                                }
                                PLAYER_FIELD_RSS_STYLE -> {
                                    preferences.setString(
                                        AppPreferences.KEY_RSS_TICKER_STYLE, fieldValueStr
                                    )
                                    if (!isFeedEvent) rssFeedString = AppConstants.TYPE_GEN_RSS
                                }
                                PLAYER_FIELD_RSS_HEIGHT -> {
                                    preferences.setInt(
                                        AppPreferences.KEY_RSS_TICKER_HEIGHT, fieldValueStr.toInt()
                                    )
                                    if (!isFeedEvent) rssFeedString = AppConstants.TYPE_GEN_RSS
                                }

                            }
                        }
                        //multiple event triggering
                        if (!isNoPlaylist) if (!type.equals(PLAYER_FIELD_ORIENTATION) || !type.equals(
                                PLAYER_FIELD_OVERLAY_URL
                            ) || !type.equals(PLAYER_FIELD_REFRESH_TIME) || !type.equals(
                                PLAYER_FIELD_LANGUAGE
                            )
                        ) socketInterface.onDataCaptured(playListString, rssFeedString)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.i("SOCKETMsg", ex.toString())
            ex.printStackTrace()
            try {
                hideSyncTxt()
            } catch (_: Exception) {

            }
            AppUtils.logError(
                "${this::class.simpleName}\n${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
        super.onMessage(webSocket, text)
    }

    private fun hideSyncTxt() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (mediaPlayerInterface != null) mediaPlayerInterface!!.onChanges(MediaPlayerActivity.HIDE_SYNC)
        }, 3000)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("SOCKET", "on closing")
        super.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        isConnected = false
        AppConstants.isToCheckPlaylist = true
        preferences.setBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED, isConnected)
        Log.i("SOCKETT", "on closed")
        AppUtils.logError(
            "socket closed\t ${AppUtils.getCurrentDateTime()}", ApiInterfaceErrorLog.TYPE_INFO
        )
        super.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnected = false
        AppConstants.isToCheckPlaylist = true
        preferences.setBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED, isConnected)
        AppUtils.scheduleSocketReconnection()
        AppUtils.logError(
            "${this::class.simpleName} on failure\t ${AppUtils.appendExp(t as Exception)} ${AppUtils.getCurrentDateTime()}",
            ApiInterfaceErrorLog.TYPE_INFO
        )
        response?.let { AppUtils.logError(ApiInterfaceErrorLog.TYPE_INFO, it.body.toString()) }
        response?.let { AppUtils.logError(ApiInterfaceErrorLog.TYPE_INFO, it.toString()) }
        super.onFailure(webSocket, t, response)
    }

}