package com.signage.yomie

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.CMSPlayer.network.medialist.RssFeedItem
import com.signage.yomie.commons.AppConstants
import com.signage.yomie.commons.DownloadInterface
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import java.io.File


open class ValidateData(val context: Context) {
    private var playerMediaList: ArrayList<DataItem> = ArrayList()
    private var rssFeedTemp: ArrayList<RssFeedItem> = ArrayList()
    private var storageFiles: ArrayList<String> = ArrayList()
    private var mediaListFiles: ArrayList<String> = ArrayList()
    private var urlsList: ArrayList<String> = ArrayList()
    private var complete = 0

    fun validate(
        dd: ArrayList<DataItem>, rssFeedTemp: ArrayList<RssFeedItem>?
    ): ArrayList<DataItem> {
        try {
            playerMediaList = ArrayList()
            this.rssFeedTemp.addAll(rssFeedTemp!!)
            playerMediaList.addAll(dd)
            if (playerMediaList.isNotEmpty()) {
                val temp =
                    Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/temp"
                val zero =
                    Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0"
                if (!File(zero).exists()) {
                    val dm = YomieApp.getContext()
                        .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val re =
                        DownloadManager.Request(Uri.parse("https://player.cosignage.com/images/original/media_1621940038.png"))
                    re.setDestinationInExternalFilesDir(
                        YomieApp.getContext(),
                        Environment.getExternalStorageDirectory().absolutePath,
                        "/temp/media_1621940038.png"
                    )
                    dm.enqueue(re)
                }
                if (!File(temp).exists()) if (File(temp).mkdirs()) Log.i("DIRR", "created")
                else Log.i("DIRR", " not created")
                //deleteRecursive(File(temp))
                checkFileToDownload()
                if (AppConstants.queue.size == 0) {
                    checkAndDeleteFiles()
                } else {
                    if (!AppConstants.isDownloading) {
                        startReq(0)
                        downloadInterface!!.onDownloadingStarted()
                        downloadInterface!!.onProgressChange(complete, urlsList.size)
                        AppUtils.logError("Download start", ApiInterfaceErrorLog.TYPE_INFO)
                    }
                }
            } else checkAndDeleteFiles()
        } catch (ex: Exception) {
            AppUtils.logError(
                "${this::class.simpleName}\n${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
            ex.printStackTrace()
            Log.i("VALID", ex.toString())
        }
        return playerMediaList

    }

    private fun checkFileToDownload() {
        if (urlsList.isNotEmpty()) urlsList.clear()
        AppUtils.makeLogFile("checkFileToDownload\t[${AppUtils.getCurrentDateTime()}]")
        for (d in playerMediaList) {
            val url: String?
            val ext: String?
            if (d.MediaType.equals("Image") || d.MediaType.equals(
                    "Video"
                )
            ) {
                url = d.Content
                ext = AppUtils.getExt(url!!)
            } else {
                url = d.Thumb
                ext = AppUtils.getExt(url!!)
            }

            val files = File(
                Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/${ext}"
            )
            mediaListFiles.add(ext.substring(ext.lastIndexOf("/") + 1))

            if (!files.exists()) {
                if (!AppConstants.queueContain(url)) {
                    urlsList.add(url)
                    AppConstants.addToQueue(url)
                }
            }
        }
    }

    private fun startReq(index: Int) {
        //AppUtils.makeLogFile("${urlsList[complete]} started\t[${AppUtils.getCurrentDateTime()}]")
        AppConstants.isDownloading = true
        var dId = 0
        dId = PRDownloader.download(
            AppConstants.queue[index],
            dirPath,
            AppUtils.getExt(AppConstants.queue[index])
        )
            .build().setOnStartOrResumeListener {}.setOnProgressListener {

                /*Log.i(
                    "QUEUEE",
                    "progress ${(it.currentBytes * 100) / it.totalBytes}/${it.totalBytes}"
                )*/

            }.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    //AppUtils.makeLogFile("${urlsList[complete]} downloaded\t[${AppUtils.getCurrentDateTime()}]")
                    //val queueSize = AppConstants.removeFromQueue(AppConstants.queue[0])
                    /*Log.i("QUEUEE", "${urlsList[0]} downloaded")
                    Log.i("QUEUEE", "queueSize $queueSize")*/
                    complete++
                    downloadInterface!!.onProgressChange(complete, urlsList.size)
                    if (urlsList.size == complete) {
                        AppConstants.isDownloading = false
                        AppConstants.queue = ArrayList()
                        downloadInterface!!.onDownloadComplete()
                    } else startReq(complete)
                }

                override fun onError(error: com.downloader.Error?) {
                    try {
                        complete = 0
                        deleteRecursive(File(  dirPath))
                        if (!File(dirPath).exists()) File(dirPath).mkdirs()
                        startReq(0)
                    } catch (ex: Exception) {
                        AppUtils.logError(
                            "on downloading error \n${AppUtils.appendExp(ex)}",
                            ApiInterfaceErrorLog.TYPE_ERROR
                        )
                    }
                    if (error != null) {
                        if (error.isServerError && (error.serverErrorMessage != null)) {
                            Log.i("DOWNERR", error.serverErrorMessage)
                            AppUtils.logError(
                                error.serverErrorMessage, ApiInterfaceErrorLog.TYPE_INFO
                            )
                        } else if (error.isConnectionError && error.connectionException.message != null) {
                            Log.i("DOWNERR", error.connectionException.message!!)
                            AppUtils.logError(
                                error.connectionException.message!!, ApiInterfaceErrorLog.TYPE_INFO
                            )
                        } else {
                            Log.i("DOWNERR", error.toString())
                            AppUtils.logError(
                                "error on downloading $error", ApiInterfaceErrorLog.TYPE_INFO
                            )
                        }
                    } else AppUtils.logError(
                        "null error on downloading", ApiInterfaceErrorLog.TYPE_INFO
                    )
                }
            })

    }

    private fun checkAndDeleteFiles() {
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/").listFiles()
        val fileNames = arrayOfNulls<String>(files?.size ?: 0)
        files?.mapIndexed { index, item ->
            fileNames[index] = item?.name
            fileNames[index]?.let {
                storageFiles.add(it)
            }
        }
        AppUtils.makeLogFile("checkAndDeleteFiles\t[${AppUtils.getCurrentDateTime()}]")
        val result: ArrayList<String> = ArrayList()
        for (s: String in storageFiles) {
            if (!mediaListFiles.contains(s)) {
                result.add(s)
            }
        }
        var del = 0
        if (result.size > 0) {
            /*did this to avoid crashing while playing playout and the same media is being deleted
         as same time, now media playing started and the unwanted media will be deleted later*/
            MediaPlayerActivity.launch(context, playerMediaList, rssFeedTemp)
            try {
                for (s: String in result) {
                    val file =
                        File(Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/$s")
                    file.delete()
                    del++
                    if (del == result.size) {
                        /*Handler(Looper.getMainLooper()).postDelayed({
                            MediaPlayerActivity.launch(context, playerMediaList, rssFeedTemp)
                        }, 1000)*/
                        break
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else MediaPlayerActivity.launch(context, playerMediaList, rssFeedTemp)

    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    companion object {
        val dirPath =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + YomieApp.getContext().packageName + "/files/storage/emulated/0/temp/"

        private var downloadInterface: DownloadInterface? = null
        fun setDownloadInterface(dow: DownloadInterface) {
            downloadInterface = dow
        }
    }
}
