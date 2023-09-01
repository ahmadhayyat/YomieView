package com.signage.yomie.commons

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.signage.yomie.CMSPlayer.network.medialist.PlayerMediaList
import com.signage.yomie.SocketManager


class AppPreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setString(key: String, value: String) = preferences.edit().putString(key, value).apply()
    fun getString(key: String) = preferences.getString(key, "")
    fun setInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()
    fun getInt(key: String) = preferences.getInt(key, 0)
    fun setBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String) = preferences.getBoolean(key, false)
    fun deletePref() = preferences.edit().clear().apply()
    fun setList(list: PlayerMediaList) {
        val gson = Gson()
        val json = gson.toJson(list)
        preferences.edit().putString(KEY_PLAYER_LIST, json).apply()
    }

    fun storeIds(
        deviceId: String,
        installationId: String,
        playerId: String,
        claimId: String
    ) {
        preferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        preferences.edit().putString(KEY_INSTALLATION_ID, installationId).apply()
        preferences.edit().putString(KEY_PLAYER_ID, playerId).apply()
        preferences.edit().putString(KEY_CLAIM_ID, claimId).apply()
    }

    companion object {
        const val KEY_PLAYER_ID = "playerId"
        const val KEY_PLAYER_PKID = "playerPKID"
        const val KEY_RSS_TICKER_STYLE = "rssTickerStyle"
        const val KEY_RSS_TICKER_HEIGHT = "rssTickerHeight"
        const val KEY_DEVICE_ID = "deviceId"
        const val KEY_INSTALLATION_ID = "installationId"
        const val KEY_CLAIM_ID = "claimId"
        const val KEY_TOTAL_DOWNLOAD = "tDownload"
        const val KEY_IS_REG = "isReg"
        const val KEY_PLAYER_LIST = "playerList"
        const val KEY_USER_LANG = "lang"
        const val KEY_PLAYBACK_URL = "playbackUrl"
        const val KEY_PLAYBACK_VOL = "playbackVol"
        const val KEY_PLAYER_ORIENTATION = SocketManager.PLAYER_FIELD_ORIENTATION
        const val KEY_PLAYER_OVERLAY_URL = SocketManager.PLAYER_FIELD_OVERLAY_URL
        const val KEY_PLAYER_REFRESH_TIME = SocketManager.PLAYER_FIELD_REFRESH_TIME
        const val KEY_PLAYER_CACHE_TIME = "cacheTime"
        const val KEY_IS_CACHE_TIME = "isCacheTime"
        const val KEY_IS_DATE_TIME_UPDATE = "isDateTimeUpdated"
        const val KEY_IS_SOCKET_CONNECTED = "isSocketConnected"
        const val KEY_PLAYER_LANGUAGE = KEY_USER_LANG
        private const val PREF_NAME = "yomie_preferences"
    }
}