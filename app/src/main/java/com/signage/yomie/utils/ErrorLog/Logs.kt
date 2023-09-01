package com.signage.yomie.utils.ErrorLog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.signage.yomie.commons.AppConstants
import java.util.*

@Entity(tableName = AppConstants.Logs_table_name)
data class Logs(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo
    var status: String,
    @ColumnInfo
    var description: String,
    @ColumnInfo
    var type: String,
    @ColumnInfo
    var date: Date
)
