package com.signage.yomie.CMSPlayer.network.medialist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signage.yomie.commons.AppConstants
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = AppConstants.DataItem_Table_Name)
data class DataItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val PlayerPKID: Int? = null,
    val Status: String? = null,
    val MediaType: String? = null,
    val PlaylistContentDetailID: Int? = null,
    val PlaylistID: Int? = null,
    val PlaylistContentID: Int? = null,
    val Content: String? = null,
    val YTVideoID: String? = null,
    val MediaTypeID: Int? = null,
    val Comments: String? = null,
    val DateFrom: String?,
    val DateTo: String?,
    val TimeFrom: String? = null,
    val TimeTo: String? = null,
    val ToTheEnd: Int? = null,
    val Duration: Int? = null,
    val Run: Int? = null,
    val RunTime: String? = null,
    val SlideOrder: Int? = null,
    val Youtube: Int? = null,
    val Thumb: String? = null,
    val GroupedContentID: String? = null,
    val TotalDuration: String? = null,
    val VideoStartFrom: String? = null,
    val VideoEndOn: String? = null,
    val MimeType: String? = null,
    val Mute: String? = null,
    val IsFullScreen: String? = null,
    val Volume: Int? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val deleted_at: String? = null,
    val Days: List<String?>?
) : Parcelable