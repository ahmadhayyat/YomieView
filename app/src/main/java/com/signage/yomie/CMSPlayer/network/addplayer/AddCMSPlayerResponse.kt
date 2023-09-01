package com.signage.yomie.CMSPlayer.network.addplayer

data class AddCMSPlayerResponse(
    val Status: String,
    val ApiStatus: Boolean,
    val Type: String,
    val Data: Data,
    val Message: String
)