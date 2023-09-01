package com.signage.yomie.database

import com.signage.yomie.CMSPlayer.CMSValues
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.CMSPlayer.network.medialist.RssFeedItem
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl


class YomieRepository(private val db: YomieDatabase) {
    suspend fun setCMSValues(values: CMSValues) = db.CMSValuesDAO().setCMSValues(values)
    fun getCMSValues() = db.CMSValuesDAO().getCMSPlayerValues()
    suspend fun setTranslation(en: En) =
        db.translationsDAO().setTranslation(en)

    suspend fun setTranslation(fr: Fr) =
        db.translationsDAO().setTranslation(fr)

    suspend fun setTranslation(nl: Nl) =
        db.translationsDAO().setTranslation(nl)

    fun getEnTranslation() = db.translationsDAO().getEnTranslation()
    fun getFrTranslation() = db.translationsDAO().getFrTranslation()
    fun getNlTranslation() = db.translationsDAO().getNlTranslation()

    suspend fun setDataItem(dataItem: ArrayList<DataItem>) = db.mediaListDAO().setDataItem(dataItem)
    fun getDataItemList() = db.mediaListDAO().getDataItemList()
    fun getDataItem() = db.mediaListDAO().getDataItem()
    fun deleteAllDataItems() {
        db.mediaListDAO().deleteAllDataItems()
    }

    suspend fun setRssFeed(rssFeedItem: ArrayList<RssFeedItem>) =
        db.mediaListDAO().setRssFeed(rssFeedItem)

    fun getRssList() = db.mediaListDAO().getRssList()
    fun getRssFeed() = db.mediaListDAO().getRssFeedItem()
    fun deleteAllRss() {
        db.mediaListDAO().deleteAllRss()
    }


    suspend fun setSchedule(playerSchedule: ArrayList<PlayerSchedule>) =
        db.playerScheduleDAO().setSchedule(playerSchedule)

    fun getSchedule() = db.playerScheduleDAO().getSchedules()
    fun deleteAllSchedule() {
        db.playerScheduleDAO().deleteAllSchedule()
    }

    fun deleteTranslation() {
        db.translationsDAO().deleteEnTranslation()
        db.translationsDAO().deleteFrTranslation()
        db.translationsDAO().deleteNlTranslation()

    }
}