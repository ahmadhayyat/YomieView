package com.signage.yomie.CMSPlayer.network.medialist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signage.yomie.commons.AppConstants

@Dao
interface MediaListDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDataItem(dataItem: ArrayList<DataItem>)

    @Query("SELECT * FROM ${AppConstants.DataItem_Table_Name}")
    fun getDataItemList(): List<DataItem>

    @Query("SELECT * FROM ${AppConstants.DataItem_Table_Name}")
    fun getDataItem(): LiveData<List<DataItem>>

    @Query("DELETE FROM ${AppConstants.DataItem_Table_Name}")
    fun deleteAllDataItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setRssFeed(rssFeedItem: ArrayList<RssFeedItem>)

    @Query("DELETE FROM ${AppConstants.RssFeedItem_Table_Name}")
    fun deleteAllRss()

    @Query("SELECT * FROM ${AppConstants.RssFeedItem_Table_Name}")
    fun getRssList(): List<RssFeedItem>

    @Query("SELECT * FROM ${AppConstants.RssFeedItem_Table_Name}")
    fun getRssFeedItem(): List<RssFeedItem>

}