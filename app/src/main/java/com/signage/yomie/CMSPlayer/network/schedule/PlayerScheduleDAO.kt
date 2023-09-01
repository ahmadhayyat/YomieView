package com.signage.yomie.CMSPlayer.network.schedule

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signage.yomie.commons.AppConstants

@Dao
interface PlayerScheduleDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSchedule(playerSchedule: ArrayList<PlayerSchedule>)

    @Query("SELECT * FROM ${AppConstants.PlayerSchedule_Table_Name}")
    fun getSchedules(): List<PlayerSchedule>

    @Query("DELETE FROM ${AppConstants.PlayerSchedule_Table_Name}")
    fun deleteAllSchedule()
}