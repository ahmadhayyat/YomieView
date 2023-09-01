package com.signage.yomie.CMSPlayer.network.translation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signage.yomie.commons.AppConstants

@Entity(tableName = AppConstants.English_table_name)
data class En(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = AppConstants.preparing_files)
    val preparing_files: String,

    @ColumnInfo(name = AppConstants.preparing)
    val preparing: String,

    @ColumnInfo(name = AppConstants.checking_files)
    val checking_files: String,

    @ColumnInfo(name = AppConstants.syncing)
    val syncing: String,

    @ColumnInfo(name = AppConstants.downloading)
    val downloading: String,

    @ColumnInfo(name = AppConstants.add_content)
    val add_content: String,

    @ColumnInfo(name = AppConstants.box_not_assigned)
    val box_not_assigned: String,

    @ColumnInfo(name = AppConstants.box_blocked)
    val box_blocked: String,

    @ColumnInfo(name = AppConstants.done)
    val done: String,

    @ColumnInfo(name = AppConstants.cancel)
    val cancel: String,
    @ColumnInfo(name = AppConstants.player_not_exist)
    val player_not_exist: String,
    @ColumnInfo(name = AppConstants.actions)
    val actions: String,

    @ColumnInfo(name = AppConstants.send_logs)
    val send_logs: String,

    @ColumnInfo(name = AppConstants.close_app)
    val close_app: String,

    @ColumnInfo(name = AppConstants.remove_player)
    val remove_player: String,

    @ColumnInfo(name = AppConstants.app_version)
    val app_version: String,

    @ColumnInfo(name = AppConstants.player_id)
    val player_id: String,

    @ColumnInfo(name = AppConstants.claim_id)
    val claim_id: String,

    @ColumnInfo(name = AppConstants.browser_not_supported)
    val browser_not_supported: String,

    @ColumnInfo(name = AppConstants.register_player)
    val register_player: String,

    @ColumnInfo(name = AppConstants.registering_device)
    val registering_device: String,


    @ColumnInfo(name = AppConstants.loading)
    val loading: String,

    @ColumnInfo(name = AppConstants.player_added_success)
    val player_added_success: String,

    @ColumnInfo(name = AppConstants.something_wrong)
    val something_wrong: String,

    @ColumnInfo(name = AppConstants.no_internet)
    val no_internet: String,

    @ColumnInfo(name = AppConstants.connected)
    val connected: String,

    @ColumnInfo(name = AppConstants.devic_not_registered)
    val devic_not_registered: String,

    @ColumnInfo(name = AppConstants.alert)
    val alert: String,


    @ColumnInfo(name = AppConstants.retry)
    val retry: String,

    @ColumnInfo(name = AppConstants.maintenance_time)
    val maintenance_time: String,
    @ColumnInfo(name = AppConstants.oops)
    val oops: String,
    @ColumnInfo(name = AppConstants.success)
    val success: String,
    @ColumnInfo(name = AppConstants.all_set)
    val all_set: String,
    @ColumnInfo(name = AppConstants.no_valid_data)
    val no_valid_data: String,
    @ColumnInfo(name = AppConstants.update_images)
    val update_images: String
)