package com.signage.yomie.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.signage.yomie.CMSPlayer.CMSValues
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.CMSPlayer.network.medialist.RssFeedItem
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class YomieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YomieRepository(YomieDatabase(application))
    fun setCMSValues(values: CMSValues) = GlobalScope.launch {
        repository.setCMSValues(values)
    }

    fun getCMSValues() = repository.getCMSValues()

    fun setTranslation(en: En) = GlobalScope.launch {
        repository.setTranslation(en)
    }

    fun setTranslation(fr: Fr) = GlobalScope.launch {
        repository.setTranslation(fr)
    }

    fun setTranslation(nl: Nl) = GlobalScope.launch {
        repository.setTranslation(nl)
    }

    fun getEnTranslation() = repository.getEnTranslation()
    fun getFrTranslation() = repository.getFrTranslation()
    fun getNlTranslation() = repository.getNlTranslation()

    fun setDataItem(dataItem: ArrayList<DataItem>) = GlobalScope.launch {
        repository.setDataItem(dataItem)
    }

    fun getDataItemList() = repository.getDataItemList()
    fun getDataItem() = repository.getDataItem()
    fun deleteAllDataItems() = GlobalScope.launch {
        repository.deleteAllDataItems()
    }

    fun setRssFeed(rssFeedItem: ArrayList<RssFeedItem>) = GlobalScope.launch {
        repository.setRssFeed(rssFeedItem)
    }

    fun getRssFeedList() = repository.getRssList()
    fun getRssFeed() = repository.getRssFeed()

    fun deleteAllRssFeed() = GlobalScope.launch {
        repository.deleteAllRss()
    }


    fun setSchedule(playerSchedule: ArrayList<PlayerSchedule>) = GlobalScope.launch {
        repository.setSchedule(playerSchedule)
    }

    fun getSchedule() = repository.getSchedule()
    fun deleteAllSchedule() = GlobalScope.launch {
        repository.deleteAllSchedule()
    }

    fun deleteAllTranslations() = GlobalScope.launch {
        repository.deleteTranslation()
    }


}