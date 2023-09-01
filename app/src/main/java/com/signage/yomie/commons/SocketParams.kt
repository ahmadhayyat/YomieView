package com.signage.yomie.commons

import org.json.JSONObject

class SocketParams {

    fun sendOnlineStatus(dId: String, iId: String): String {
        val params = JSONObject()
        params.put("From", "YomieView")
        params.put("Type", "updatePlayerSession")
        params.put("DeviceID", dId)
        params.put("InstallationId", iId)
        return "$params"
    }

    fun registerDevice(dId: String, pId: String): String {
        val params = JSONObject()
        params.put("From", "YomieView")
        params.put("Type", "DeviceRegistration")
        params.put("DeviceID", dId)
        params.put("PlayerID", pId)
        return "$params"
    }
}