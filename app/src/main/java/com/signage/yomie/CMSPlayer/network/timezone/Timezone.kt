package com.signage.yomie.CMSPlayer.network.timezone

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Timezone(

    @field:SerializedName("Status")
    val status: String? = null,

    @field:SerializedName("Type")
    val type: String? = null,

    @field:SerializedName("Message")
    val message: String? = null,

    @field:SerializedName("Data")
    val data: Data? = null
) : Parcelable

@Parcelize
data class Data(

    @field:SerializedName("dateTime")
    val dateTime: String? = null,

    @field:SerializedName("zipCode")
    val zipCode: String? = null,

    @field:SerializedName("timezone")
    val timezone: String? = null,

    @field:SerializedName("ip")
    val ip: String? = null,

    @field:SerializedName("regionName")
    val regionName: String? = null,

    @field:SerializedName("postalCode")
    val postalCode: Int? = null,

    @field:SerializedName("latitude")
    val latitude: String? = null,

    @field:SerializedName("regionCode")
    val regionCode: String? = null,

    @field:SerializedName("areaCode")
    val areaCode: String? = null,

    @field:SerializedName("cityName")
    val cityName: String? = null,

    @field:SerializedName("driver")
    val driver: String? = null,

    @field:SerializedName("isoCode")
    val isoCode: String? = null,

    @field:SerializedName("countryCode")
    val countryCode: String? = null,

    @field:SerializedName("metroCode")
    val metroCode: String? = null,

    @field:SerializedName("countryName")
    val countryName: String? = null,

    @field:SerializedName("longitude")
    val longitude: String? = null
) : Parcelable
