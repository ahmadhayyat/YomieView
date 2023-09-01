package com.signage.yomie.commons

object AppConstants {
    //API CALLS
    const val API_VERSION = "/api/"
    const val BASE_URL_S = "https://player.cosignage.com${API_VERSION}"
    const val BASE_URL = "http://player.cosignage.com${API_VERSION}"

    //current dateTime
    var tempDate = ""
    var tempTime = ""
    var tempDateTime = ""
    var isDateTimeUpdated = false
    var isDownloading = false
    var isSyncing = false
    var isToCheckPlaylist = false

    //queue array
    var queue = ArrayList<String>()
    fun addToQueue(link: String) {
        queue.add(link)
    }

    fun queueContain(link: String): Boolean {
        return queue.contains(link)
    }

    fun removeFromQueue(link: String): Int {
        queue.remove(link)
        return queue.size
    }

    //CMSPlayer constants
    const val CMSPlayer_table_name = "CMSValues"

    //Logs
    const val Logs_table_name = "Logs"

    //MediaList
    const val DataItem_Table_Name = "DataItem"
    const val RssFeedItem_Table_Name = "RssFeedItem"

    //Scheduling
    const val PlayerSchedule_Table_Name = "PlayerSchedule"
    var scheduleStartTime = ""
    var scheduleEndTime = ""
    const val images_update_time = "07:00:00"
    var isImagesUpdated = true

    //Rss feed
    const val DefaultRss = "Default"
    const val OverlayRss = "Overlay"

    //Commons
    var socketReconnectionTime = 30000
    var playerErrorCounter = 0
    var totalProgress = 0

    //Translation constants
    const val English_table_name = "en"
    const val French_table_name = "fr"
    const val Dutch_table_name = "nl"
    const val preparing_files = "preparing_files"
    const val preparing = "preparing"
    const val checking_files = "checking_files"
    const val syncing = "syncing"
    const val downloading = "downloading"
    const val add_content = "add_content"
    const val box_not_assigned = "box_not_assigned"
    const val box_blocked = "box_blocked"
    const val done = "done"
    const val cancel = "cancel"
    const val player_not_exist = "player_not_exist"
    const val actions = "actions"
    const val send_logs = "send_logs"
    const val close_app = "close_app"
    const val remove_player = "remove_player"
    const val app_version = "app_version"
    const val player_id = "player_id"
    const val claim_id = "claim_id"
    const val browser_not_supported = "browser_not_supported"
    const val register_player = "register_player"
    const val registering_device = "registering_device"
    const val success = "success"
    const val loading = "loading"
    const val player_added_success = "player_added_success"
    const val something_wrong = "something_wrong"
    const val no_internet = "no_internet"
    const val connected = "connected"
    const val devic_not_registered = "devic_not_registered"
    const val alert = "alert"
    const val retry = "retry"
    const val maintenance_time = "maintenance_time"
    const val oops = "oops"
    const val all_set = "all_set"
    const val no_valid_data = "no_valid_data"
    const val update_images = "update_images"


    //Database
    const val DB_Version = 8
    const val DB_Name = "yomie"

    val MIME_TYPE_IMG = "image/jpeg"
    val MIME_TYPE_VIDEO = "video/mp4"
    val MIME_TYPE_WEB = "application/x-msdos-program"
    val TYPE_GEN_RSS = "genRss"
    val ORIENTATION_LANDSCAPE = "Landscape"
    val ORIENTATION_PORTRAIT = "Portrait"
    val ORIENTATION_R_PORTRAIT = "RPortrait"
    val MEDIA_TYPE_IMG = "Image"
    val MEDIA_TYPE_VIDEO = "Video"
    val MEDIA_TYPE_WEBSITE = "Website"
}