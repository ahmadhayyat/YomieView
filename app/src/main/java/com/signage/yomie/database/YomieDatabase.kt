package com.signage.yomie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.signage.yomie.CMSPlayer.CMSValues
import com.signage.yomie.CMSPlayer.CMSValuesDAO
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.CMSPlayer.network.medialist.DaysConverter
import com.signage.yomie.CMSPlayer.network.medialist.MediaListDAO
import com.signage.yomie.CMSPlayer.network.medialist.RssFeedItem
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import com.signage.yomie.CMSPlayer.network.schedule.PlayerScheduleDAO
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl
import com.signage.yomie.CMSPlayer.network.translation.TranslationDAO
import com.signage.yomie.commons.AppConstants

@Database(
    entities = [CMSValues::class, En::class, Fr::class, Nl::class, DataItem::class,
        RssFeedItem::class, PlayerSchedule::class],
    version = AppConstants.DB_Version,
    exportSchema = false
)
@TypeConverters(DaysConverter::class)
abstract class YomieDatabase : RoomDatabase() {

    abstract fun CMSValuesDAO(): CMSValuesDAO
    abstract fun translationsDAO(): TranslationDAO
    abstract fun mediaListDAO(): MediaListDAO
    abstract fun playerScheduleDAO(): PlayerScheduleDAO

    companion object {
        @Volatile
        private var instance: YomieDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                YomieDatabase::class.java,
                "${AppConstants.DB_Name}.db"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}
