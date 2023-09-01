package com.signage.yomie.CMSPlayer.network.schedule

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.signage.yomie.commons.AppConstants
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = AppConstants.PlayerSchedule_Table_Name)
data class PlayerSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo
    @field:SerializedName("ModifiedAt")
    val modifiedAt: String,
    @ColumnInfo
    @field:SerializedName("PlayerID")
    val playerID: Int,
    @ColumnInfo
    @field:SerializedName("EndTime")
    val endTime: String,
    @ColumnInfo
    @field:SerializedName("Playback")
    val playback: Int,
    @ColumnInfo
    @field:SerializedName("Playout")
    val playout: Int,
    @ColumnInfo
    @field:SerializedName("CreatedAt")
    val createdAt: String,
    @ColumnInfo
    @field:SerializedName("StartTime")
    val startTime: String,
    @ColumnInfo
    @field:SerializedName("ScheduleID")
    val scheduleID: Int,
    @ColumnInfo
    @field:SerializedName("Day")
    val day: Int
) : Parcelable
