package com.signage.yomie.CMSPlayer.network.medialist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerInfo(
    val PlayerPKID: Int? = null,
    val UserID: Int? = null,
    val PlayerID: String? = null,
    val BoxNumber: String? = null,
    val ClaimID: String? = null,
    val DeviceID: String? = null,
    val InstallationId: String? = null,
    val RssFeed: String? = null,
    val Status: String? = null,
    val Description: String? = null,
    val GroupID: String? = null,
    val PlaylistID: Int? = null,
    val IsActive: String? = null,
    val PlayerTypeID: Int? = null,
    val OverlayUrl: String? = null,
    val Version: String? = null,
    val Orientation: String? = null,
    val InternalRemarks: String? = null,
    val Language: String? = null,
    val IsPlaybackAllow: Int? = null,
    val PlaybackUrl: String? = null,
    val AnyDeskID: String? = null,
    val AnyDeskPassword: String? = null,
    val LastActive: String? = null,
    val PlayerRefreshTime: String? = null,
    val CreatedAt: String? = null,
    val ModifiedAt: String? = null,
    val deletedAt: String? = null,
    val rssTickerStyle: String? = null,
    val rssTickerHeight: Int? = null
) : Parcelable