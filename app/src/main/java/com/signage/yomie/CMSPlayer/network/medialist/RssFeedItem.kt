package com.signage.yomie.CMSPlayer.network.medialist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signage.yomie.commons.AppConstants
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = AppConstants.RssFeedItem_Table_Name)
data class RssFeedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val RssFeedSiteID: Int? = null,
    val SiteName: String? = null,
    val SiteLink: String? = null,
    val SiteImage: String? = null,
    val Title: String? = null,
    val Description: String? = null
) : Parcelable