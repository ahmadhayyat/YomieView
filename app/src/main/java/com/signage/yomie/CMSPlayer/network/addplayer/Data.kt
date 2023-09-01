package com.signage.yomie.CMSPlayer.network.addplayer


data class Data(
    val PlayerID: String,
    val ClaimID: String,
    val DeviceID: String,
    val UserID: Any,
    val InstallationId: String,
    val Description: String,
    val playerTypeID: String,
    val Version: Any? = null,
    val Orientation: String,
    val Language: String,
    val CreatedAt: String,
    val ModifiedAt: String
)