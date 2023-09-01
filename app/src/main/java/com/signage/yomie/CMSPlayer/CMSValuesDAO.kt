package com.signage.yomie.CMSPlayer

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signage.yomie.commons.AppConstants

@Dao
interface CMSValuesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCMSValues(cmsValues: CMSValues)

    @Query("SELECT * FROM ${AppConstants.CMSPlayer_table_name}")
    fun getCMSPlayerValues(): LiveData<CMSValues>
}