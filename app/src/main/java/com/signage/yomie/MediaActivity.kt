package com.signage.yomie

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.novoda.merlin.Merlin
import com.signage.yomie.CMSPlayer.CMSPlayerVMFactory
import com.signage.yomie.CMSPlayer.network.ApiInterfaceCMSPlayer
import com.signage.yomie.CMSPlayer.network.medialist.*
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import com.signage.yomie.CMSPlayer.network.timezone.ApiInterfaceTimeZone
import com.signage.yomie.CMSPlayer.network.timezone.Timezone
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl
import com.signage.yomie.CMSPlayer.network.translation.TranslationResponse
import com.signage.yomie.commons.*
import com.signage.yomie.database.YomieViewModel
import com.signage.yomie.databinding.ActivityMediaBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.AppUtils.Companion.toString
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess


class MediaActivity : BaseActivity(), DownloadInterface, SocketInterface, MediaPlayerInterface {
    private var binding: ActivityMediaBinding? = null
    private lateinit var playerIdTv: TextView
    private lateinit var claimIdTv: TextView
    private lateinit var mediaMsg: TextView
    private lateinit var prgMsg: TextView
    private lateinit var mediaPb: ProgressBar
    private var en: En? = null
    private var fr: Fr? = null
    private var nl: Nl? = null
    private lateinit var syncTv: TextView
    private lateinit var syncIv: ImageView
    private lateinit var syncRl: LinearLayout
    private lateinit var mainView: ConstraintLayout
    var context: Context? = null
    private var merlin: Merlin? = null
    private var isConnected = false
    private var dateHandler: Handler? = null
    private var dateRunnable: Runnable? = null
    private var dateCount = 0

    companion object {
        var playerMediaListTemp: PlayerMediaList? = null
        var playerMediaList: ArrayList<DataItem>? = null
        var rssFeedList: ArrayList<RssFeedItem>? = null
        var playerSchedule: ArrayList<PlayerSchedule>? = null
        var playBackInfo: PlaybackInfo? = null
        var storageFiles: ArrayList<String>? = null
        var mediaListFiles: ArrayList<String>? = null
        var viewModel: YomieViewModel? = null
        var vmFactory: CMSPlayerVMFactory? = null
        var installationId: String? = null
        var deviceId: String? = null
        var preparingFile: PreparingFile? = null
        fun setPreparingFileInterface(pr: PreparingFile) {
            preparingFile = pr
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeOrientation()
        binding = ActivityMediaBinding.inflate(layoutInflater)
        val view = binding?.root
        setContentView(view)
        initViews()
        initVariables()
        setUpClicks()
    }

    private fun getTranslation() {
        try {
            mediaPb.visibility = View.GONE
            mediaMsg.visibility = View.VISIBLE
            if (en != null && fr != null && nl != null) {
                when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                    AppConstants.English_table_name -> mediaMsg.text = en!!.checking_files
                    AppConstants.French_table_name -> mediaMsg.text = fr!!.checking_files
                    AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.checking_files
                }
            } else mediaMsg.text = getString(R.string.checking_files)
            val apiTranslation = ApiInterfaceCMSPlayer.create().getTranslations()
            apiTranslation.enqueue(object : Callback<TranslationResponse> {
                override fun onResponse(
                    call: Call<TranslationResponse>, response: Response<TranslationResponse>
                ) {
                    if (response.body()?.ApiStatus == true) {
                        en = response.body()!!.Translations.en
                        fr = response.body()!!.Translations.fr
                        nl = response.body()!!.Translations.nl
                        viewModel?.deleteAllTranslations()
                        viewModel?.setTranslation(en!!)
                        viewModel?.setTranslation(fr!!)
                        viewModel?.setTranslation(nl!!)
                        getMediaList()
                    } else {
                        val text =
                            "\n${this::class.simpleName} [${ApiInterfaceErrorLog.TYPE_ERROR.uppercase()}]\t" + "get translation call returned false\t[${AppUtils.getCurrentDateTime()}]"
                        AppUtils.makeLogFile(text)

                        mediaPb.visibility = View.GONE
                        mediaMsg.visibility = View.VISIBLE
                        if (en != null && fr != null && nl != null) {
                            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                                AppConstants.English_table_name -> mediaMsg.text =
                                    en!!.something_wrong
                                AppConstants.French_table_name -> mediaMsg.text =
                                    fr!!.something_wrong
                                AppConstants.Dutch_table_name -> mediaMsg.text =
                                    nl!!.something_wrong
                            }
                        } else mediaMsg.text = getString(R.string.something_wrong)
                    }
                }

                override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getTranslation()
                    }, 30000)
                    AppUtils.logError(
                        "${this::class.simpleName}\n${t.message}", ApiInterfaceErrorLog.TYPE_ERROR
                    )
                    if (en != null && fr != null && nl != null) {
                        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                            AppConstants.English_table_name -> mediaMsg.text = en!!.something_wrong
                            AppConstants.French_table_name -> mediaMsg.text = fr!!.something_wrong
                            AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.something_wrong
                        }
                    } else mediaMsg.text = getString(R.string.something_wrong)
                }

            })

        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(
                "${this::class.simpleName}\n${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
    }

    private fun getMediaList() {
        mediaPb.visibility = View.GONE
        mediaMsg.visibility = View.VISIBLE
        if (en != null && fr != null && nl != null) {
            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                AppConstants.English_table_name -> mediaMsg.text = en!!.checking_files
                AppConstants.French_table_name -> mediaMsg.text = fr!!.checking_files
                AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.checking_files
            }
        } else mediaMsg.text = getString(R.string.checking_files)
        val apiInterface = ApiInterfaceCMSPlayer.create().getDeviceMedia(
            deviceId!!, installationId!!, BuildConfig.VERSION_NAME
        )
        apiInterface.enqueue(object : Callback<PlayerMediaList> {
            override fun onResponse(
                call: Call<PlayerMediaList>, response: Response<PlayerMediaList>
            ) {
                if (response.body()?.ApiStatus == true) {
                    playerMediaListTemp = response.body()!!
                    val dataTemp: ArrayList<DataItem> =
                        playerMediaListTemp!!.Data as ArrayList<DataItem>
                    rssFeedList = playerMediaListTemp!!.RssFeed as ArrayList<RssFeedItem>
                    playBackInfo = playerMediaListTemp!!.PlaybackInfo
                    playerSchedule =
                        playerMediaListTemp!!.PlayerSchedule as ArrayList<PlayerSchedule>
                    val playerInfo: PlayerInfo = playerMediaListTemp!!.PlayerInfo!!
                    preferences.setInt(AppPreferences.KEY_PLAYER_PKID, playerInfo.PlayerPKID!!)
                    preferences.setString(
                        AppPreferences.KEY_PLAYER_ORIENTATION, playerInfo.Orientation!!
                    )
                    preferences.setString(
                        AppPreferences.KEY_PLAYER_REFRESH_TIME, playerInfo.PlayerRefreshTime!!
                    )
                    if (playBackInfo != null) {
                        preferences.setString(AppPreferences.KEY_PLAYBACK_URL, playBackInfo!!.Url!!)
                        preferences.setInt(AppPreferences.KEY_PLAYBACK_VOL, playBackInfo!!.Volume!!)
                    } else {
                        preferences.setString(AppPreferences.KEY_PLAYBACK_URL, "")
                        preferences.setInt(AppPreferences.KEY_PLAYBACK_VOL, 0)
                    }
                    preferences.setString(
                        AppPreferences.KEY_USER_LANG, playerInfo.Language!!
                    )
                    preferences.setString(
                        AppPreferences.KEY_RSS_TICKER_STYLE, playerInfo.rssTickerStyle!!
                    )
                    preferences.setInt(
                        AppPreferences.KEY_RSS_TICKER_HEIGHT, playerInfo.rssTickerHeight!!
                    )
                    if (!dataTemp.isNullOrEmpty()) {
                        playerMediaList = ValidateData(context!!).validate(
                            dataTemp,
                            rssFeedList,
                        )

                        if (viewModel!!.getDataItemList().isNotEmpty()) {
                            viewModel!!.deleteAllDataItems()
                        }
                        if (viewModel!!.getRssFeedList().isNotEmpty()) {
                            viewModel!!.deleteAllRssFeed()
                        }
                        viewModel!!.setDataItem(playerMediaList!!)
                        viewModel!!.setRssFeed(rssFeedList!!)
                        if (playerSchedule!!.isNotEmpty()) {
                            viewModel!!.deleteAllSchedule()
                            viewModel!!.setSchedule(playerSchedule!!)
                        } else {
                            viewModel!!.deleteAllSchedule()
                            AppConstants.scheduleStartTime = ""
                            AppConstants.scheduleEndTime = ""
                        }
                    } else {
                        mediaPb.visibility = View.GONE
                        mediaMsg.visibility = View.VISIBLE
                        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                            AppConstants.English_table_name -> mediaMsg.text = en!!.add_content
                            AppConstants.French_table_name -> mediaMsg.text = fr!!.add_content
                            AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.add_content
                        }

                    }
                } else {
                    val text =
                        "\n${this::class.simpleName} [${ApiInterfaceErrorLog.TYPE_ERROR.uppercase()}]\t" + "get media call returned false\t[${AppUtils.getCurrentDateTime()}]"
                    AppUtils.makeLogFile(text)
                    mediaPb.visibility = View.GONE
                    mediaMsg.visibility = View.VISIBLE
                    when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                        AppConstants.English_table_name -> mediaMsg.text = en!!.player_not_exist
                        AppConstants.French_table_name -> mediaMsg.text = fr!!.player_not_exist
                        AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.player_not_exist
                    }
                    AppUtils.deleteDB(this@MediaActivity)
                    finishAffinity()
                }
            }

            override fun onFailure(call: Call<PlayerMediaList>, t: Throwable) {
                Handler(Looper.getMainLooper()).postDelayed({
                    getMediaList()
                }, 30000)
                t.printStackTrace()
                AppUtils.logError(
                    "${this::class.simpleName}\n${AppUtils.appendExp(t as Exception)}",
                    ApiInterfaceErrorLog.TYPE_ERROR
                )
                when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                    AppConstants.English_table_name -> mediaMsg.text = en!!.something_wrong
                    AppConstants.French_table_name -> mediaMsg.text = fr!!.something_wrong
                    AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.something_wrong
                }
            }

        })
    }

    override fun initViews() {
        playerIdTv = binding!!.mediaPlayerId
        claimIdTv = binding!!.mediaClaimId
        mediaMsg = binding!!.mediaMsg
        prgMsg = binding!!.prgMsg
        mediaPb = binding!!.mediaPb
        mainView = binding!!.mainView
        syncTv = binding!!.syncTv
        syncIv = binding!!.syncIv
        syncRl = binding!!.syncRl
    }

    override fun initVariables() {
        mediaMsg.visibility = View.GONE
        mediaPb.visibility = View.GONE
        SocketManager.setSocketInterface(this)
        ValidateData.setDownloadInterface(this)
        SocketManager.setMediaInterface(this)
        syncRl.visibility = View.GONE
        context = this@MediaActivity
        storageFiles = ArrayList()
        mediaListFiles = ArrayList()
        playerMediaList = ArrayList()
        playBackInfo = PlaybackInfo(0, 0, "", 0, "", "")
        vmFactory = CMSPlayerVMFactory(application)
        viewModel = ViewModelProvider(this, vmFactory!!)[YomieViewModel::class.java]
        val enList: ArrayList<En?> = viewModel!!.getEnTranslation() as ArrayList<En?>
        val frList: ArrayList<Fr?> = viewModel!!.getFrTranslation() as ArrayList<Fr?>
        val nlList: ArrayList<Nl?> = viewModel!!.getNlTranslation() as ArrayList<Nl?>
        if (enList.isNotEmpty() && frList.isNotEmpty() && nlList.isNotEmpty()) {
            en = enList[0]
            fr = frList[0]
            nl = nlList[0]
        }
        installationId = preferences.getString(AppPreferences.KEY_INSTALLATION_ID).toString()
        deviceId = preferences.getString(AppPreferences.KEY_DEVICE_ID).toString()
        playerIdTv.text =
            getString(R.string.playerId, "${preferences.getString(AppPreferences.KEY_PLAYER_ID)}")
        claimIdTv.text =
            getString(R.string.claimId, "${preferences.getString(AppPreferences.KEY_CLAIM_ID)}")
        merlin =
            Merlin.Builder().withConnectableCallbacks().withDisconnectableCallbacks().build(this)
        if (AppUtils.isOnline(this)) {
            merlin!!.registerConnectable {
                if (!isConnected) {
                    checkCurrentDate()
                }
                isConnected = true
            }
            merlin!!.registerDisconnectable {
                val year = AppUtils.getCurrentDateTime().toString("yyyy")
                if (year.toInt() < 2022) {
                    checkCurrentDate()
                } else {
                    runOnUiThread {
                        if (en != null && fr != null && nl != null) {
                            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                                AppConstants.English_table_name -> mediaMsg.text = en!!.no_internet
                                AppConstants.French_table_name -> mediaMsg.text = fr!!.no_internet
                                AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.no_internet
                            }
                        } else mediaMsg.text = getString(R.string.no_internet)
                    }
                    playOutLocally()
                    isConnected = false
                }
            }
        } else {
            playOutLocally()
            isConnected = false
        }
    }

    private fun checkCurrentDate() {
        val apiTimeZone = ApiInterfaceTimeZone.create().getTimeDate()
        apiTimeZone.enqueue(object : Callback<Timezone> {
            override fun onResponse(
                call: Call<Timezone>, response: Response<Timezone>
            ) {
                if (response.body()?.status.equals("SUCCESS")) {
                    val d: List<String> = response.body()!!.data!!.dateTime!!.split(" ")
                    val date = Calendar.getInstance().time.toString("yyyy-MM-dd")
                    if (date == d[0]) {
                        preferences.setBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE, true)
                        //AppConstants.isDateTimeUpdated = true
                        YomieApp.connectSocket()
                        getTranslation()
                    } else {
                        AppConstants.tempDate = d[0]
                        AppConstants.tempTime = d[1]
                        AppConstants.tempDateTime = response.body()!!.data!!.dateTime!!
                        playOutLocally()
                        //AppUtils.checkCurrentDate()
                    }
                } else {
                    if (dateHandler != null && dateRunnable != null) dateHandler!!.removeCallbacks(
                        dateRunnable!!
                    )
                    mediaMsg.visibility = View.VISIBLE
                    mediaMsg.text = getString(R.string.date_incorrect)
                    dateHandler = Handler(mainLooper)
                    dateRunnable = Runnable {
                        checkCurrentDate()
                    }
                    dateHandler!!.postDelayed(dateRunnable!!, 30000)
                }
            }

            override fun onFailure(call: Call<Timezone>, t: Throwable) {
                AppUtils.logError(t.stackTraceToString(), "TIMEZONE_CAll_FAILURE")
                t.printStackTrace()
                if (dateHandler != null && dateRunnable != null) dateHandler!!.removeCallbacks(
                    dateRunnable!!
                )
                mediaMsg.visibility = View.VISIBLE
                mediaMsg.text = getString(R.string.date_incorrect)
                dateHandler = Handler(mainLooper)
                dateRunnable = Runnable {
                    checkCurrentDate()
                }
                dateHandler!!.postDelayed(dateRunnable!!, 30000)
            }
        })
    }

    override fun setUpClicks() {
        var lastClick: Long = 0
        mainView.setOnTouchListener { p0, p1 ->
            val currentClick: Long = System.currentTimeMillis()
            if (currentClick - lastClick < MediaPlayerActivity.TIME_INTERVAL) {
                openBottomSheet()
            }
            lastClick = currentClick
            false
        }
    }

    private fun playOutLocally() {
        val text =
            "\n${this::class.simpleName} [${ApiInterfaceErrorLog.TYPE_ERROR.uppercase()}]\t" + "not connected to internet play out locally\t[${AppUtils.getCurrentDateTime()}]"
        AppUtils.makeLogFile(text)
        playerMediaList = viewModel!!.getDataItemList() as ArrayList<DataItem>
        rssFeedList = viewModel!!.getRssFeed() as ArrayList<RssFeedItem>
        val i = Intent(context, MediaPlayerActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        i.putExtra(
            MediaPlayerActivity.KEY_MEDIA_LIST, playerMediaList
        )
        i.putExtra(MediaPlayerActivity.KEY_RSS_FEED_LIST, rssFeedList)
        startActivity(i)
        finish()

    }

    override fun onDownloadComplete() {
        try {
            AppUtils.logError(
                "download complete\t[${AppUtils.getCurrentDateTime()}]",
                ApiInterfaceErrorLog.TYPE_INFO
            )
            storageFiles = ArrayList()
            moveFiles()

        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(AppUtils.appendExp(ex), ApiInterfaceErrorLog.TYPE_ERROR)
        }
    }

    override fun onDownloadingStarted() {
        runOnUiThread {
            mediaPb.visibility = View.VISIBLE
            mediaMsg.visibility = View.VISIBLE
            if (en != null && fr != null && nl != null) {
                when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                    AppConstants.English_table_name -> mediaMsg.text = en!!.downloading
                    AppConstants.French_table_name -> mediaMsg.text = fr!!.downloading
                    AppConstants.Dutch_table_name -> mediaMsg.text = nl!!.downloading
                }
            } else mediaMsg.text = getString(R.string.downloading_playlist)
        }
    }

    override fun onProgressChange(complete: Int, total: Int) {
        runOnUiThread {
            prgMsg.text = getString(R.string.progress, complete, total)
        }
    }


    private fun moveFiles() {
        AppUtils.makeLogFile("file moving started\t[${AppUtils.getCurrentDateTime()}]")
        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
            AppConstants.English_table_name -> mediaMsg.text = en?.preparing_files
            AppConstants.French_table_name -> mediaMsg.text = fr?.preparing_files
            AppConstants.Dutch_table_name -> mediaMsg.text = nl?.preparing_files
        }

        /*if (preparingFile != null) {
            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                AppConstants.English_table_name -> preparingFile!!.onPreparing(en!!.preparing_files)
                AppConstants.French_table_name -> preparingFile!!.onPreparing(fr!!.preparing_files)
                AppConstants.Dutch_table_name -> preparingFile!!.onPreparing(nl!!.preparing_files)
            }
        }*/
        var inFile: InputStream?
        var out: OutputStream?
        val outputPath =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/"
        val inputPath =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/temp/"
        try {
            val files =
                File(Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/temp/").listFiles()
            val fileNames = arrayOfNulls<String>(files?.size ?: 0)
            files?.mapIndexed { index, item ->
                fileNames[index] = item?.name
                fileNames[index]?.let {
                    storageFiles?.add(it)
                }
            }
            //create output directory if it doesn't exist
            var moved = 0
            for (inputFile in storageFiles!!) {
                AppUtils.makeLogFile("moving $inputFile\t[${AppUtils.getCurrentDateTime()}]")
                val dir = File(outputPath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                inFile = FileInputStream(inputPath + inputFile)
                out = FileOutputStream(outputPath + inputFile)
                val buffer = ByteArray(1024)
                var read: Int
                while (inFile.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                inFile.close()

                // write the output file
                out.flush()
                out.close()

                // delete the original file
                File(inputPath + inputFile).delete()
                moved++

                if (moved == storageFiles?.size) {
                    AppUtils.makeLogFile("file moving completed\t[${AppUtils.getCurrentDateTime()}]")
                    checkAndDeleteFiles()
                    break
                }
            }
        } catch (fnfe1: FileNotFoundException) {
            Log.e("tag", fnfe1.message!!)
        } catch (e: java.lang.Exception) {
            Log.e("tag", e.message!!)
        }
    }

    private fun openBottomSheet() {
        val dialog = BottomSheetDialog(this@MediaActivity)
        val v = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        val closeApp = v.findViewById<TextView>(R.id.bsClose)
        val removePlayer = v.findViewById<TextView>(R.id.bsRemove)
        val appVer = v.findViewById<TextView>(R.id.bsAppVer)
        val playerId = v.findViewById<TextView>(R.id.bsPlayerId)
        val restart = v.findViewById<TextView>(R.id.bsRestart)
        val cancel = v.findViewById<TextView>(R.id.bsCancel)
        val pb = v.findViewById<ProgressBar>(R.id.bsRemovePB)

        cancel.setOnClickListener {
            dialog.cancel()
        }
        closeApp.setOnClickListener {
            AppUtils.logError("App closed", ApiInterfaceErrorLog.TYPE_INFO)
            finishAffinity()
        }
        removePlayer.setOnClickListener {
            pb.visibility = View.VISIBLE
            AppUtils.deleteDB(this@MediaActivity)
            Timer().schedule(timerTask {
                AppUtils.logError("Player removed", ApiInterfaceErrorLog.TYPE_INFO)
                finishAffinity()
            }, 3000)
            //File(this@MediaActivity.cacheDir.path).deleteRecursively()
        }
        restart.setOnClickListener {
            AppUtils.logError("Player restarted", ApiInterfaceErrorLog.TYPE_INFO)
            val intent = Intent(applicationContext, RegistrationActivity::class.java)
            val mPendingIntent = PendingIntent.getActivity(
                applicationContext,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            exitProcess(0)
        }
        dialog.setOnShowListener {
            val bs =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val bsb = BottomSheetBehavior.from(bs)
            bsb.state = BottomSheetBehavior.STATE_EXPANDED
            appVer.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
            playerId.text = getString(
                R.string.playerId, preferences.getString(AppPreferences.KEY_PLAYER_ID)
            )
        }
        dialog.setContentView(v)
        dialog.show()
    }

    private fun checkAndDeleteFiles() {
        for (d in playerMediaList!!) {
            val ext: String? = if (d.MediaType.equals("Image") || d.MediaType.equals("Video")) {
                d.Content?.substring(d.Content.lastIndexOf("/"))
            } else {
                d.Thumb?.substring(d.Thumb.lastIndexOf("/"))
            }
            mediaListFiles!!.add(ext?.substring(ext.lastIndexOf("/") + 1)!!)
        }
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/").listFiles()
        val fileNames = arrayOfNulls<String>(files?.size ?: 0)
        files?.mapIndexed { index, item ->
            fileNames[index] = item?.name
            fileNames[index]?.let {
                storageFiles!!.add(it)
            }
        }
        //storageFiles!!.remove("temp")
        val result: ArrayList<String> = ArrayList()
        for (s: String in storageFiles!!) {
            if (!mediaListFiles!!.contains(s)) {
                result.add(s)
            }
        }
        var deleted = 0
        if (result.size > 0) {
            for (s: String in result) {
                val file =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/$s")
                file.delete()
                deleted++
                if (deleted == result.size) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        MediaPlayerActivity.launch(this, playerMediaList!!, rssFeedList!!)
                    }, 2000)
                    break
                }
            }
        }
    }

    override fun onDataCaptured(data: String, type: String) {
        try {
            var dataList: ArrayList<DataItem> = ArrayList()
            when (type) {
                SocketManager.TYPE_PLAYLIST_DATA, SocketManager.TYPE_PLAYLIST_DEL, SocketManager.TYPE_PLAYLIST_ADD -> {
                    val jsonObject = JSONObject(data)
                    val dt = jsonObject.getJSONObject("Data")
                    val dataArray = dt.get("Data") as JSONArray
                    if (type == SocketManager.TYPE_PLAYLIST_DATA) {
                        for (index in 0 until dataArray.length()) {
                            val obj = dataArray[index].toString()
                            val di = Gson().fromJson(obj, DataItem::class.java)
                            dataList.add(di)
                        }
                    } else {
                        dataList = viewModel!!.getDataItemList() as ArrayList
                        val obj = dataArray[0].toString()
                        val di = Gson().fromJson(obj, DataItem::class.java)
                        if (type == SocketManager.TYPE_PLAYLIST_ADD) dataList.add(di)
                        else {
                            for (index in 0 until dataList.size) {
                                val d = dataList[index]
                                if (di.PlaylistContentID == d.PlaylistContentID) {
                                    dataList.removeAt(index)
                                    break
                                }
                            }
                        }
                    }
                }
                SocketManager.PLAYER_FIELD_RSS_FEED_SLIDE -> {
                    dataList = viewModel!!.getDataItemList() as ArrayList
                    rssFeedList = ArrayList()
                    if (data.isNotEmpty()) {
                        generateRssFeedList(data)
                    }
                }
                SocketManager.PLAYER_FIELD_PLAYLIST -> {
                    dataList = generateDataList(data)
                }
                SocketManager.TYPE_PLAYBACK -> {
                    playBackInfo = Gson().fromJson(data, PlaybackInfo::class.java)
                    if (playBackInfo?.Url!!.isNotEmpty() ) {
                        preferences.setString(AppPreferences.KEY_PLAYBACK_URL, playBackInfo?.Url!!)
                        preferences.setInt(AppPreferences.KEY_PLAYBACK_VOL, playBackInfo?.Volume!!)
                    } else {
                        preferences.setString(AppPreferences.KEY_PLAYBACK_URL, "")
                        preferences.setInt(AppPreferences.KEY_PLAYBACK_VOL, 0)
                    }
                    dataList = viewModel!!.getDataItemList() as ArrayList<DataItem>
                    if (dataList.isEmpty()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            synTxt(MediaPlayerActivity.HIDE_SYNC)
                        }, 2000)
                    }
                }

                else -> {
                    //multiple event handling
                    dataList = generateDataList(data)
                    rssFeedList = ArrayList()
                    if (type.isNotEmpty()) {
                        if (type == AppConstants.TYPE_GEN_RSS) rssFeedList =
                            viewModel!!.getRssFeedList() as ArrayList<RssFeedItem>
                        else generateRssFeedList(type)
                    }
                }
            }
            playerMediaList = ArrayList()
            playerMediaList?.addAll(dataList)
            viewModel!!.deleteAllDataItems()
            viewModel!!.setDataItem(dataList)
            ValidateData(this).validate(dataList, rssFeedList)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun generateRssFeedList(data: String) {
        val rssArray = JSONArray(data)
        for (i in 0 until rssArray.length()) {
            val obj = rssArray[i].toString()
            val rss = Gson().fromJson(obj, RssFeedItem::class.java)
            rssFeedList?.add(rss)
        }
    }

    private fun generateDataList(data: String): ArrayList<DataItem> {
        var dataList: ArrayList<DataItem> = ArrayList()
        if (data.isNotEmpty()) {
            val dataArray = JSONArray(data)
            for (index in 0 until dataArray.length()) {
                val obj = dataArray[index].toString()
                val di = Gson().fromJson(obj, DataItem::class.java)
                dataList.add(di)
            }
        } else {
            dataList = viewModel!!.getDataItemList() as ArrayList<DataItem>
        }
        return dataList
    }

    private fun synTxt(i: Int) {
        when (i) {
            MediaPlayerActivity.SHOW_SYNC -> {
                when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                    AppConstants.English_table_name -> {
                        syncTv.text = en?.syncing
                        mediaMsg.text = en?.downloading
                    }
                    AppConstants.French_table_name -> {
                        syncTv.text = fr?.syncing
                        mediaMsg.text = fr?.downloading
                    }
                    AppConstants.Dutch_table_name -> {
                        syncTv.text = nl?.syncing
                        mediaMsg.text = nl?.downloading
                    }
                }
                mediaPb.visibility = View.VISIBLE
                syncIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sync))
                syncIv.scaleY = -1F
                syncRl.visibility = View.VISIBLE
                syncRl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
                val anim = RotateAnimation(
                    0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                )
                anim.duration = 1000
                anim.repeatCount = Animation.INFINITE
                syncIv.animation = anim
            }
            MediaPlayerActivity.HIDE_SYNC -> {
                syncRl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right))
                syncRl.visibility = View.GONE
                syncIv.clearAnimation()
                mediaPb.visibility = View.GONE
            }
            MediaPlayerActivity.HIDE_SYNC_SHOW_ALL_SET -> {
                allSet()
            }
            MediaPlayerActivity.CHANGE_ORIENTATION -> changeOrientation()
        }
    }

    private fun changeOrientation() {
        when (preferences.getString(AppPreferences.KEY_PLAYER_ORIENTATION)) {
            AppConstants.ORIENTATION_LANDSCAPE -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            AppConstants.ORIENTATION_PORTRAIT -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            AppConstants.ORIENTATION_R_PORTRAIT -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        }

    }

    private fun allSet() {
        mediaPb.visibility = View.GONE
        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
            AppConstants.English_table_name -> syncTv.text = en?.all_set
            AppConstants.French_table_name -> syncTv.text = fr?.all_set
            AppConstants.Dutch_table_name -> syncTv.text = nl?.all_set
        }
        syncIv.clearAnimation()
        syncIv.scaleY = 1F
        syncRl.visibility = View.VISIBLE
        syncRl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
        syncIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_thumbs_up_solid))
        Handler(Looper.getMainLooper()).postDelayed({
            syncRl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right))
            syncRl.visibility = View.GONE
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        if (merlin != null) merlin!!.bind()
    }

    override fun onDestroy() {
        if (merlin != null) merlin!!.unbind()
        merlin = null
        if (dateHandler != null && dateRunnable != null) dateHandler!!.removeCallbacks(dateRunnable!!)
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
        super.onDestroy()
    }

    interface PreparingFile {
        fun onPreparing(text: String)
    }

    override fun onChanges(i: Int) {
        runOnUiThread {
            synTxt(i)
        }

    }

    override fun onDeletePlayer() {

    }
}