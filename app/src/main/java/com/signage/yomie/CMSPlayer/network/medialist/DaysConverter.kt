package com.signage.yomie.CMSPlayer.network.medialist

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class DaysConverter {
    @TypeConverter
    fun fromString(value: String?): List<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}