package com.signage.yomie.CMSPlayer.network.medialist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaybackInfo(
    val PlaybackID: Int?,
    val UserID: Int?,
    val Url: String?,
    val Volume: Int?,
    val Created_At: String?,
    val Updated_At: String?
) : Parcelable
