package com.signage.yomie.CMSPlayer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signage.yomie.commons.AppConstants

@Entity(tableName = AppConstants.CMSPlayer_table_name)
data class CMSValues(

    @PrimaryKey
    var deviceId: String,
    @ColumnInfo(name = "installationId")
    var installationId: String,
    @ColumnInfo(name = "playerId")
    var playerId: String,
    @ColumnInfo(name = "claimId")
    var claimId: String

)
