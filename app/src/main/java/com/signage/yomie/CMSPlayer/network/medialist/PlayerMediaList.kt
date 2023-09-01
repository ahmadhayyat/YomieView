package com.signage.yomie.CMSPlayer.network.medialist

import android.os.Parcelable
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerMediaList(
    val Status: String? = null,
    val ApiStatus: Boolean,
    val Type: String? = null,
    val Data: List<DataItem?>? = null,
    val RssFeed: List<RssFeedItem?>? = null,
    val PlayerStatus: String? = null,
    val PlayerIsActive: String? = null,
    val PlayerInfo: PlayerInfo? = null,
    val PlaybackInfo: PlaybackInfo,
    val PlayerSchedule: List<PlayerSchedule>
) : Parcelable