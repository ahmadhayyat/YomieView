package com.signage.yomie.CMSPlayer.network.checkdevice

data class CheckDeviceResponse(
    val Status: String,
    val Type: String,
    val Data: Data,
    val Message: String
)
