package com.signage.yomie.commons

interface DownloadInterface {
    fun onDownloadComplete()
    fun onDownloadingStarted()
    fun onProgressChange(complete: Int, total: Int)
}