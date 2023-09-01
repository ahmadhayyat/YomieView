package com.signage.yomie

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.*
import android.text.Spanned
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.toSpanned
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.novoda.merlin.Merlin
import com.signage.yomie.CMSPlayer.CMSPlayerVMFactory
import com.signage.yomie.CMSPlayer.network.ApiInterfaceCMSPlayer
import com.signage.yomie.CMSPlayer.network.EmptyFragment
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.CMSPlayer.network.medialist.PlayerStatusResponse
import com.signage.yomie.CMSPlayer.network.medialist.RssFeedItem
import com.signage.yomie.CMSPlayer.network.schedule.PlayerSchedule
import com.signage.yomie.CMSPlayer.network.timezone.ApiInterfaceTimeZone
import com.signage.yomie.CMSPlayer.network.timezone.Timezone
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl
import com.signage.yomie.commons.*
import com.signage.yomie.database.YomieViewModel
import com.signage.yomie.databinding.ActivityMediaplayerBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.AppUtils.Companion.containsAnyOfIgnoreCase
import com.signage.yomie.utils.AppUtils.Companion.dpToPx
import com.signage.yomie.utils.AppUtils.Companion.toString
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import com.signage.yomie.utils.StreamUrlValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlResImageGetter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess


class MediaPlayerActivity : BaseActivity(), MediaPlayerInterface, MediaActivity.PreparingFile,
    Player.Listener, RestartInterface, PlayerScheduleInterface, SkipMediaInterface,
    PlayBackInterface {
    private var mediaList: ArrayList<DataItem>? = null
    private var rssFeedList: ArrayList<RssFeedItem>? = null
    private var playerSchedule: ArrayList<PlayerSchedule>? = null
    private var binding: ActivityMediaplayerBinding? = null
    private lateinit var dTap: RelativeLayout
    private lateinit var altMsgRl: RelativeLayout
    private lateinit var noPlayOutRl: RelativeLayout
    private lateinit var playOutRl: RelativeLayout
    private lateinit var altMsgTv: TextView
    private lateinit var syncTv: TextView
    private lateinit var playerId: TextView
    private lateinit var claimId: TextView
    private lateinit var mediaMsg: TextView
    private lateinit var syncIv: ImageView
    private lateinit var syncRl: LinearLayout
    private var timer: CountDownTimer? = null
    private var playBackTimer: CountDownTimer? = null
    private var socketTimer: CountDownTimer? = null

    /*private var heapTimer: CountDownTimer? = null*/
    lateinit var deviceId: String
    lateinit var installationId: String
    private var tempRss: String? = null
    private var tempRssHttp: String? = null
    private var gotoIndex: Int = 0
    var fontSize: Int = 0
    var imgSize: Int = 0
    private var en: En? = null
    private var fr: Fr? = null
    private var nl: Nl? = null
    var viewModel: YomieViewModel? = null
    lateinit var vmFactory: CMSPlayerVMFactory
    private var rssTickerStyle = ""
    var isPlayOutPlaying = false
    private var isPlaybackPlaying = false
    private var player: ExoPlayer? = null
    private var validMedia = false
    lateinit var merlin: Merlin

    companion object {
        var TOTAL_TIME: Long = 0
        const val TIME_INTERVAL: Long = 1000
        const val KEY_MEDIA_LIST = "keyMediaList"
        const val KEY_RSS_FEED_LIST = "keyRssFeedList"
        const val PLAYBACK_RESUME = 0
        const val PLAYBACK_PAUSE = 1
        lateinit var mClient: OkHttpClient
        const val WEB_SOCKET_URL = "wss://player.cosignage.com:9990"
        var bundle: Bundle? = null
        const val SHOW_SYNC = 1
        const val HIDE_SYNC = 2
        const val HIDE_SYNC_SHOW_ALL_SET = 3
        const val CHANGE_ORIENTATION = 4
        fun launch(
            context: Context?,
            playerMediaList: ArrayList<DataItem>,
            rssFeedList: ArrayList<RssFeedItem>
        ) {
            val intent = Intent(context, MediaPlayerActivity::class.java)
            intent.putExtra(KEY_MEDIA_LIST, playerMediaList)
            intent.putExtra(KEY_RSS_FEED_LIST, rssFeedList)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)
        }

    }

/*
    private var mCallback = object : ANRSpyListener {
        override fun onWait(ms: Long) {
            Log.e("ANRSp waited", "$ms")
            //Total blocking time of main thread.
            //Can be used for doing any action e.g. if blocked time is more than 5 seconds then
            //restart the app to avoid raising ANR message because it will lead to down rank your app.
        }

        override fun onAnrStackTrace(stackstrace: Array<StackTraceElement>) {
            //To  investigate ANR via stackstrace if occured.
            //This method is deprecated and will  be removed in future
        }

        override fun onReportAvailable(methodList: List<MethodModel>) {
            //Get instant report about annotated methods if touches main thread more than target time
        }

        override fun onAnrDetected(
            details: String,
            stackTrace: Array<StackTraceElement>,
            packageMethods: List<String>?
        ) {
            //details: Short description about the detected anr
            //stacktrace: Stacktrace of the anr
            //packageMethod: methods hierarchy(bottom up) that causes anr (only if method is inside current app package name)
        }
    }
*/
    /*private val firebaseInstance = FirebaseAnalytics.getInstance(this)
    private val anrSpyAgent = ANRSpyAgent.Builder(this)
        .setTimeOut(5000)
        .setSpyListener(mCallback)
        .setThrowException(true)
        .enableReportAnnotatedMethods(true)
        .setFirebaseInstance(firebaseInstance)
        .build()*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //anrSpyAgent.start()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        changeOrientation()
        bundle = savedInstanceState
        binding = ActivityMediaplayerBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        initViews()
        initVariables()
        setUpClicks()
    }


    private fun schedulePlayer() {
        val day = AppUtils.getDayInt()
        if (playerSchedule!!.isNotEmpty()) {
            var dayFound = false
            for (schedule in playerSchedule!!) {
                if (schedule.day == day) {
                    AppConstants.scheduleStartTime = schedule.startTime
                    AppConstants.scheduleEndTime = schedule.endTime
                    if (schedule.playback == 1 && schedule.playout == 1) {
                        if (AppUtils.isWithinRange(
                                "t",
                                AppUtils.getCurrentDateTime().toString("HH:mm:ss"),
                                schedule.startTime,
                                schedule.endTime
                            )
                        ) {
                            if (checkPlayBack()) playPlayOut()
                            Log.i(
                                "SCHEDULE",
                                "week day found and playback, play out are 1 play in specific time"
                            )
                        } else {
                            playOutRl.visibility = View.GONE
                            noPlayOutRl.visibility = View.VISIBLE
                            mediaMsg.visibility = View.GONE
                            if (timer != null) {
                                timer!!.cancel()
                                timer = null
                                mediaList!!.clear()
                                addFragmentToActivity(EmptyFragment())
                                isPlayOutPlaying = false
                            }
                            if (player != null) {
                                player = null
                                isPlaybackPlaying = false
                            }
                            Log.i(
                                "SCHEDULE",
                                "week day found and playback, play out are 1 play in specific time"
                            )

                        }
                    } else if (schedule.playback == 1) {
                        if (AppUtils.isWithinRange(
                                "t",
                                AppUtils.getCurrentDateTime().toString("HH:mm:ss"),
                                schedule.startTime,
                                schedule.endTime
                            )
                        ) {
                            checkPlayBack()
                            Log.i(
                                "SCHEDULE",
                                "week day found and playback is 1 play in specific time | play out 24hrs"
                            )
                        } else {
                            if (player != null) {
                                player!!.stop()
                                player = null
                            }
                            Log.i(
                                "SCHEDULE",
                                "week day found and playback is 1 play in specific time | play out 24hrs"
                            )

                        }
                        if (!isPlayOutPlaying) playPlayOut()
                        //play out is 0 play 24hrs
                    } else if (schedule.playout == 1) {
                        if (AppUtils.isWithinRange(
                                "t",
                                AppUtils.getCurrentDateTime().toString("HH:mm:ss"),
                                schedule.startTime,
                                schedule.endTime
                            )
                        ) {
                            playPlayOut()
                            Log.i(
                                "SCHEDULE",
                                "week day found and play out is 1 play in specific time | playback 24hrs"
                            )
                        } else {
                            if (timer != null) {
                                timer!!.cancel()
                                timer = null
                                mediaList!!.clear()
                                addFragmentToActivity(EmptyFragment())
                            }
                            noPlayOutRl.visibility = View.VISIBLE
                            playOutRl.visibility = View.GONE
                            mediaMsg.visibility = View.GONE
                            isPlaybackPlaying = false
                            Log.i(
                                "SCHEDULE",
                                "week day found and play out is 1 play in specific time | playback 24hrs"
                            )

                        }

                        if (!isPlaybackPlaying) checkPlayBack()
                        //playback is 0 play 24hrs
                    }
                    dayFound = true
                    break
                }
                dayFound = false
            }
            if (!dayFound) {
                AppConstants.scheduleStartTime = ""
                AppConstants.scheduleEndTime = ""
                if (checkPlayBack()) playPlayOut()
                Log.i("SCHEDULE", "no week day found play playback and play out 24hrs")
            }
        } else {
            if (checkPlayBack()) playPlayOut()
            Log.i("SCHEDULE", "no week day found play playback,play out 24hrs")
        }
    }

    private fun playPlayOut() {
        playOutRl.visibility = View.VISIBLE
        noPlayOutRl.visibility = View.GONE
        if (intent.hasExtra(KEY_MEDIA_LIST)) {
            if (mediaList!!.size == 0) mediaList =
                intent.getParcelableArrayListExtra(KEY_MEDIA_LIST)!!
            validate(true)
            checkValidMedia(false)
        }


        if (intent.hasExtra(KEY_RSS_FEED_LIST) && validMedia) {
            try {
                rssFeedList = intent.getParcelableArrayListExtra(KEY_RSS_FEED_LIST)!!
                when (preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT)) {
                    72 -> {
                        fontSize = 50
                        imgSize = 72
                    }
                    62 -> {
                        fontSize = 32
                        imgSize = 62
                    }
                    52 -> {
                        fontSize = 28
                        imgSize = 52
                    }
                    42 -> {
                        fontSize = 22
                        imgSize = 42
                    }
                    32 -> {
                        fontSize = 18
                        imgSize = 32
                    }
                    else -> {
                        fontSize = 18
                        imgSize = 32
                    }
                }
                tempRss = ""
                tempRssHttp = ""
                var img: String
                for (r: RssFeedItem in rssFeedList!!) {
                    var b = false
                    when ("${r.SiteImage}") {
                        "ds.jpeg" -> img = "ds$imgSize"
                        "De-Tijd-Logo.png" -> img = "dt$imgSize"
                        "gazet-van-antwerpen.png" -> img = "gva$imgSize"
                        "het-nieuwsblad-logo.png" -> img = "hn$imgSize"
                        "HetBelangvanLimburg-v2.jpg" -> img = "hbvl$imgSize"
                        "media_1651209531.png" -> img = "bi$imgSize"
                        "yomie.png" -> img = ""
                        else -> {
                            img = "${r.SiteImage}"
                            b = img.isNotEmpty()
                        }
                    }
                    if (b) {
                        val sl = img.split(".")
                        if (AppUtils.isOnline(this)) tempRssHttp =
                            StringBuilder().append(tempRssHttp).append(
                                "<p><span><img src=\"https://player.cosignage.com/assets/logo/${sl[0]}_$imgSize.${sl[1]}\"></span><span> ${r.Description}</span></p>"
                            ).toString()
                        else tempRss =
                            StringBuilder().append(tempRss).append("&bull; <p>${r.Description}</p>")
                                .toString()
                    } else {
                        tempRss = if (img.isEmpty()) {
                            StringBuilder().append(tempRss).append("&bull; <p>${r.Description}</p>")
                                .toString()
                        } else {
                            StringBuilder().append(tempRss)
                                .append("<img src=\"$img\"> <p>${r.Description}</p>").toString()
                        }
                    }
                }
                loadRss()
            } catch (ex: Exception) {
                ex.printStackTrace()

            }
        }

    }

    private fun displayHtml(html: String, httpHtml: String, view: TextView) {
        var httpSpan: Spanned = "".toSpanned()
        var htmlSpan: Spanned
        CoroutineScope(Dispatchers.Main).launch {
            if (httpHtml.isNotEmpty()) {
                val httpText = checkHtmlCount(httpHtml)
                httpSpan = HtmlFormatter.formatHtml(
                    HtmlFormatterBuilder().setHtml(httpText)
                        .setImageGetter(HtmlHttpImageGetter(view))
                )

            }
            val text = checkHtmlCount(html)
            htmlSpan = HtmlFormatter.formatHtml(
                HtmlFormatterBuilder().setHtml(text)
                    .setImageGetter(HtmlResImageGetter(this@MediaPlayerActivity))
            )
            val styledText = TextUtils.concat(httpSpan, "", htmlSpan).toSpanned()
            binding?.dClock?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding?.dClock?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    var dClockWidth = binding?.dClock?.measuredWidth
                    dClockWidth = dClockWidth?.div(3)
                    val scale = resources.displayMetrics.density
                    view.setPadding(
                        (32 * scale + 0.5f).toInt(), 0, (dClockWidth!! * scale + 0.5f).toInt(), 0
                    )
                }
            })
            view.text = styledText
            view.isSelected = true
        }

    }

    private fun checkHtmlCount(html: String): String {
        var text = html
        return if (text.length > 250) {
            text
        } else {
            text = "$html $html $html $html $html $html"
            text
        }
    }

    private fun loadRss() {
        if (rssFeedList?.size!! > 0) {
            if (rssTickerStyle == AppConstants.DefaultRss) {
                binding?.rssParent?.visibility = View.VISIBLE

                binding?.rssParent?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) + 6)
                val param = binding?.htmlViewer?.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 4)
                binding?.htmlViewer?.layoutParams = param
                binding?.htmlViewer?.setBackgroundColor(
                    resources.getColor(
                        R.color.black, resources.newTheme()
                    )
                )
                binding?.rssParent?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bottom_ticker_bg, resources.newTheme()
                )
                binding?.htmlViewer?.textSize = fontSize.toFloat()
                binding?.htmlViewer?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT))
                val dParam = binding?.dClock?.layoutParams as ViewGroup.MarginLayoutParams
                dParam.setMargins(0, 0, 0, 4)
                binding?.dClock?.visibility = View.VISIBLE
                binding?.dClock?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT))
                val logoParam = binding?.tickerLogo?.layoutParams as ViewGroup.MarginLayoutParams
                logoParam.setMargins(0, 5, 0, 4)

                binding?.tickerLogo?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_right_curved, resources.newTheme()
                )
                binding?.dClock?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_left_curved, resources.newTheme()
                )
                binding?.dClock?.setTextColor(
                    resources.getColor(
                        R.color.black, resources.newTheme()
                    )
                )

                if (preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) >= 62) binding?.dClock?.textSize =
                    16f

                val playOutRlParam =
                    binding?.playOutRl?.layoutParams as ViewGroup.MarginLayoutParams
                when (preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT)) {
                    32 -> {
                        playOutRlParam.setMargins(
                            0,
                            0,
                            0,
                            preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) * 2 - 8
                        )
                    }
                    42 -> {
                        playOutRlParam.setMargins(
                            0,
                            0,
                            0,
                            preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) * 2 - 14
                        )
                    }
                    52 -> {
                        playOutRlParam.setMargins(
                            0,
                            0,
                            0,
                            preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) * 2 - 18
                        )
                    }
                    62 -> {
                        playOutRlParam.setMargins(
                            0,
                            0,
                            0,
                            preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) * 2 - 22
                        )
                    }
                    72 -> {
                        playOutRlParam.setMargins(
                            0,
                            0,
                            0,
                            preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) * 2 - 27
                        )
                    }

                }
                displayHtml(tempRss!!, tempRssHttp!!, binding?.htmlViewer!!)

            } else if (rssTickerStyle == AppConstants.OverlayRss) {
                binding?.rssParent?.visibility = View.VISIBLE
                binding?.rssParent?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT))
                val param = binding?.htmlViewer?.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 0)
                binding?.htmlViewer?.layoutParams = param
                binding?.htmlViewer?.setBackgroundColor(
                    resources.getColor(
                        R.color.black_50, resources.newTheme()
                    )
                )
                binding?.rssParent?.setBackgroundColor(
                    resources.getColor(
                        android.R.color.transparent, resources.newTheme()
                    )
                )
                binding?.htmlViewer?.textSize = fontSize.toFloat()
                binding?.htmlViewer?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT))
                val dParam = binding?.dClock?.layoutParams as ViewGroup.MarginLayoutParams
                dParam.setMargins(0, 0, 0, 0)
                binding?.dClock?.visibility = View.VISIBLE
                binding?.dClock?.layoutParams?.height =
                    this.dpToPx(preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT))

                val logoParam = binding?.tickerLogo?.layoutParams as ViewGroup.MarginLayoutParams
                logoParam.setMargins(0, 0, 0, 0)

                binding?.tickerLogo?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_right_curved_black, resources.newTheme()
                )
                binding?.dClock?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_left_curved_black, resources.newTheme()
                )
                binding?.dClock?.setTextColor(
                    resources.getColor(
                        R.color.white, resources.newTheme()
                    )
                )
                if (preferences.getInt(AppPreferences.KEY_RSS_TICKER_HEIGHT) >= 62) binding?.dClock?.textSize =
                    16f
                val playOutRlParam =
                    binding?.playOutRl?.layoutParams as ViewGroup.MarginLayoutParams
                playOutRlParam.setMargins(
                    0, 0, 0, 0
                )
            }
            displayHtml(tempRss!!, tempRssHttp!!, binding?.htmlViewer!!)
            rssFeedList = ArrayList()
            tempRss = ""
        } else {
            binding?.rssParent?.visibility = View.GONE
            binding?.dClock?.visibility = View.GONE
        }
    }

    private fun setClockFormat() {
        binding?.dClock?.format24Hour = "HH:mm"
        binding?.dClock?.format12Hour = null
    }

    private fun checkPlayBack(): Boolean {
        return if (!preferences.getString(AppPreferences.KEY_PLAYBACK_URL)
                .isNullOrEmpty() && AppUtils.isOnline(this)
        ) {
            preparePlayBack(
                preferences.getString(AppPreferences.KEY_PLAYBACK_URL)!!,
                preferences.getInt(AppPreferences.KEY_PLAYBACK_VOL)
            )
        } else true
    }

    private fun preparePlayBack(url: String, vol: Int): Boolean {
        if (player != null) player = null
        StreamUrlValidator.urlValidator(url,
            this,
            object : StreamUrlValidator.OnStreamUrlValidationListener {
                override fun onValidationResult(isValid: Boolean) {
                    if (isValid) {
                        player = ExoPlayer.Builder(this@MediaPlayerActivity).build()
                        val mediaItem = MediaItem.fromUri(url)
                        player!!.setMediaItem(mediaItem)
                        player!!.prepare()
                        player!!.playWhenReady = true
                        val volume = if (vol < 100) "0.$vol"
                        else "1.0"
                        player!!.volume = volume.toFloat()
                        player!!.addListener(this@MediaPlayerActivity)
                        isPlaybackPlaying = true
                    } else {
                        AppUtils.logError("invalid playback url", ApiInterfaceErrorLog.TYPE_INFO)
                        checkPlaybackLoop()
                    }
                }
            })
        return true
    }

    override fun onPlayerError(error: PlaybackException) {
        AppUtils.logError(
            "on playback music \n${AppUtils.appendExp(error)}", ApiInterfaceErrorLog.TYPE_ERROR
        )
        checkPlaybackLoop()
    }

    private fun checkPlaybackLoop() {
        playBackTimer = object : CountDownTimer(30000, TIME_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                try {
                    if (player != null) {
                        player!!.stop()
                        player = null
                    }
                    checkPlayBack()
                    playBackTimer?.cancel()
                    playBackTimer = null
                } catch (e: Exception) {
                    Log.e("Error", "Error: $e")
                }
            }
        }.start()

    }

    private fun startLoop() {
        if (timer != null) timer!!.cancel()
        timer = object : CountDownTimer(TOTAL_TIME, TIME_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                //val sec = millisUntilFinished / TIME_INTERVAL
            }

            override fun onFinish() {
                try {
                    if (gotoIndex == (mediaList!!.size - 1)) {
                        validate(true)
                    } else validate(false)
                    checkValidMedia(false)
                } catch (e: Exception) {
                    Log.e("Error", "Error: $e")
                }
            }
        }.start()
    }

    fun validate(isZeroCheck: Boolean) {
        if (isZeroCheck) {
            for (i in 0 until mediaList!!.size) {
                if (validate(mediaList!![i])) {
                    TOTAL_TIME = mediaList!![i].Duration?.toLong()!! * 1000
                    gotoIndex = i
                    prepareFrags(i)
                    if (timer != null) timer!!.cancel()
                    startLoop()
                    break
                }
            }

        } else {
            for (i in gotoIndex + 1 until mediaList!!.size) {
                if (validate(mediaList!![i])) {
                    TOTAL_TIME = mediaList!![i].Duration?.toLong()!! * 1000
                    gotoIndex = i
                    prepareFrags(gotoIndex)
                    if (timer != null) timer!!.cancel()
                    startLoop()
                    break
                } else {
                    if (timer != null) timer!!.cancel()
                    validate(true)
                }
            }
        }
    }

    private fun checkValidMedia(isPreparing: Boolean) {
        if (!validMedia) {
            playOutRl.visibility = View.GONE
            noPlayOutRl.visibility = View.VISIBLE
            mediaMsg.visibility = View.VISIBLE
            binding?.rssParent?.visibility = View.GONE
            binding?.dClock?.visibility = View.GONE
            if (!isPreparing) {
                if (en != null && fr != null && nl != null) {
                    when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                        AppConstants.English_table_name -> mediaMsg.text = en?.no_valid_data
                        AppConstants.French_table_name -> mediaMsg.text = fr?.no_valid_data
                        AppConstants.Dutch_table_name -> mediaMsg.text = nl?.no_valid_data
                    }
                } else mediaMsg.text = getString(R.string.no_valid_data)
            } else {
                if (en != null && fr != null && nl != null) {
                    when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                        AppConstants.English_table_name -> mediaMsg.text = en?.preparing_files
                        AppConstants.French_table_name -> mediaMsg.text = fr?.preparing_files
                        AppConstants.Dutch_table_name -> mediaMsg.text = nl?.preparing_files
                    }
                } else mediaMsg.text = getString(R.string.preparing_files)
            }
            if (timer != null) {
                timer!!.cancel()
                timer = null
                mediaList!!.clear()
                addFragmentToActivity(EmptyFragment())
                isPlayOutPlaying = false
            }
            if (player != null) {
                player?.release()
                player = null
                isPlaybackPlaying = false
            }
            if (!isPreparing) checkValidMediaLoop()
        }
    }

    private fun checkValidMediaLoop() {
        if (timer != null) timer!!.cancel()
        timer = object : CountDownTimer(60000, TIME_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                try {
                    schedulePlayer()
                } catch (_: Exception) {
                }
            }
        }.start()
    }


    private fun validate(d: DataItem): Boolean {
        val date = AppUtils.getCurrentDateTime()
        if (!d.DateFrom.isNullOrEmpty() && !d.DateTo.isNullOrEmpty()) {
            if (AppUtils.isWithinRange(
                    "d", date.toString("yyyy-MM-dd"), d.DateFrom, d.DateTo
                )
            ) {
                val day = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    Calendar.SATURDAY -> "Sat"
                    Calendar.SUNDAY -> "Sun"
                    else -> {
                        ""
                    }
                }

                // val s = date.toString("EEE")
                if (!d.Days.isNullOrEmpty()) {
                    if (day.containsAnyOfIgnoreCase(d.Days)) {
                        return if (!d.TimeFrom.isNullOrEmpty() && !d.TimeTo.isNullOrEmpty()) {
                            if (AppUtils.isWithinRange(
                                    "t", date.toString("HH:mm:ss"), d.TimeFrom, d.TimeTo
                                )
                            ) {
                                validMedia = true
                                true
                            } else {
                                validMedia = false
                                false
                            }
                        } else {
                            validMedia = false
                            false
                        }
                    } else {
                        validMedia = false
                        return false
                    }
                } else {
                    validMedia = false
                    return false
                }
            } else {
                validMedia = false
                return false
            }
        } else {
            validMedia = false
            return false
        }

    }

    private fun prepareFrags(i: Int) {
        var uri =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0"

        val dataItem = mediaList!![i]
        when (dataItem.MediaType) {
            AppConstants.MEDIA_TYPE_VIDEO -> {
                addFragmentToActivity(
                    VideoFragment.newInstance(
                        dataItem
                    )
                )

            }
            AppConstants.MEDIA_TYPE_IMG -> {
                val name = dataItem.Content?.substring(dataItem.Content.lastIndexOf("/")).toString()
                uri += name
                addFragmentToActivity(ImageFragment.newInstance(uri, ""))

            }
            AppConstants.MEDIA_TYPE_WEBSITE -> {
                val url = dataItem.Content
                val name = dataItem.Thumb?.substring(dataItem.Thumb.lastIndexOf("/")).toString()
                uri += name
                addFragmentToActivity(WebsiteFragment.newInstance(url!!, uri))

            }
        }

        isPlayOutPlaying = true
    }

    private fun addFragmentToActivity(fragment: Fragment?) {
        if (fragment == null) return
        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        tr.replace(R.id.mediaPlayerFL, fragment)
        tr.commit()
    }

    fun startSocketLoop() {
        socketTimer = object : CountDownTimer(120000, TIME_INTERVAL) {
            override fun onTick(p0: Long) {
                val currentTime = DateFormat.format("kk:mm:ss", Calendar.getInstance())
                val currentDateTime =
                    DateFormat.format("yyyy-MM-dd HH:mm:ss", Calendar.getInstance())
                preferences.setString(AppPreferences.KEY_PLAYER_CACHE_TIME, "$currentDateTime")
                if ("$currentTime" == preferences.getString(AppPreferences.KEY_PLAYER_REFRESH_TIME)) onTimeMatch()
                else if ("$currentTime" == AppConstants.scheduleStartTime || "$currentTime" == AppConstants.scheduleEndTime) onScheduleTimeOccur()
                else if ("$currentTime" == AppConstants.images_update_time) {
                    if (AppUtils.isOnline(this@MediaPlayerActivity))
                        getFileToDelete()
                    else AppConstants.isImagesUpdated = false
                }

                val r = 120000 / p0.toInt()
                if (r == 2) {
                    var long = AppUtils.timeToMillis(AppConstants.tempTime)
                    long += 60000
                    AppConstants.tempTime = AppUtils.millisToTime(long)
                    AppConstants.tempDateTime = "${AppConstants.tempDate} ${AppConstants.tempTime}"
                }
            }

            override fun onFinish() {
                sendSocketEvent()
                startSocketLoop()
            }
        }.start()
    }

    fun sendSocketEvent() {
        try {
            if (preferences.getBoolean(AppPreferences.KEY_IS_SOCKET_CONNECTED)) {
                val params = SocketParams().sendOnlineStatus(deviceId, installationId)
                socketManager.sendEvent(params)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(
                "${this::class.simpleName}\t${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun openBottomSheet() {
        val dialog = BottomSheetDialog(this@MediaPlayerActivity)
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
            AppUtils.logError("Player closed", ApiInterfaceErrorLog.TYPE_INFO)
            finishAndRemoveTask()
        }
        removePlayer.setOnClickListener {
            pb.visibility = View.VISIBLE
            AppUtils.deleteDB(this@MediaPlayerActivity)
            Timer().schedule(timerTask {
                AppUtils.logError("Player removed", ApiInterfaceErrorLog.TYPE_INFO)
                exitProcess(0)
            }, 3000)
        }
        restart.setOnClickListener {
            restartApp()
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

    private fun synTxt(i: Int) {
        when (i) {
            SHOW_SYNC -> {
                if (!AppConstants.isSyncing) {
                    AppConstants.isSyncing = true
                    if (en != null && fr != null && nl != null) {
                        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                            AppConstants.English_table_name -> syncTv.text = en?.syncing
                            AppConstants.French_table_name -> syncTv.text = fr?.syncing
                            AppConstants.Dutch_table_name -> syncTv.text = nl?.syncing
                        }
                    } else syncTv.text = getString(R.string.syncing)
                    syncIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sync))
                    syncIv.scaleY = -1F
                    syncRl.visibility = View.VISIBLE
                    syncRl.startAnimation(
                        AnimationUtils.loadAnimation(
                            this, R.anim.slide_in_left
                        )
                    )
                    val anim = RotateAnimation(
                        0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    anim.duration = 1000
                    anim.repeatCount = Animation.INFINITE
                    syncIv.animation = anim
                }
            }
            HIDE_SYNC -> {
                syncRl.startAnimation(
                    AnimationUtils.loadAnimation(
                        this, R.anim.slide_out_right
                    )
                )
                syncRl.visibility = View.GONE
                syncIv.clearAnimation()
            }
            HIDE_SYNC_SHOW_ALL_SET -> {
                allSet()
            }
            CHANGE_ORIENTATION -> changeOrientation()
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
        AppConstants.isSyncing = false
        if (en != null && fr != null && nl != null) {
            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                AppConstants.English_table_name -> syncTv.text = en?.all_set
                AppConstants.French_table_name -> syncTv.text = fr?.all_set
                AppConstants.Dutch_table_name -> syncTv.text = nl?.all_set
            }
        } else syncTv.text = getString(R.string.all_set)
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

    private fun alertDialog() {
        altMsgRl.visibility = View.VISIBLE
        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
            AppConstants.English_table_name -> altMsgTv.text = en?.player_not_exist
            AppConstants.French_table_name -> altMsgTv.text = fr?.player_not_exist
            AppConstants.Dutch_table_name -> altMsgTv.text = nl?.player_not_exist
        }
        Timer().schedule(timerTask {
            AppUtils.deleteDB(this@MediaPlayerActivity)
            AppUtils.logError("Player removed", ApiInterfaceErrorLog.TYPE_INFO)
            exitProcess(0)
        }, 10000)

    }

    private fun restartApp() {
        AppUtils.logError("App restarted", ApiInterfaceErrorLog.TYPE_INFO)
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

    override fun initViews() {
        playOutRl = binding!!.playOutRl
        noPlayOutRl = binding!!.noPlayOutRl
        dTap = binding!!.dTapView
        altMsgRl = binding!!.altMsgRl
        altMsgTv = binding!!.altMsgTv
        syncTv = binding!!.syncTv
        syncIv = binding!!.syncIv
        syncRl = binding!!.syncRl
        playerId = binding!!.mediaPlayerId
        claimId = binding!!.mediaClaimId
        mediaMsg = binding!!.mediaMsg
    }

    override fun initVariables() {
        merlin =
            Merlin.Builder().withConnectableCallbacks().withDisconnectableCallbacks().build(this)
        merlin.registerConnectable {
            if (!AppConstants.isImagesUpdated) {
                getFileToDelete()
            }
        }
        if (!preferences.getBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE)) checkCurrentDate()
        SocketManager.setMediaInterface(this)
        MediaActivity.setPreparingFileInterface(this)
        VideoFragment.setRestartInterface(this)
        VideoFragment.setSkipMediaInterface(this)
        VideoFragment.setPlayBackInterface(this)
        playerSchedule = ArrayList()
        dTap.keepScreenOn = true
        mediaList = ArrayList()
        rssFeedList = ArrayList()
        rssTickerStyle = preferences.getString(AppPreferences.KEY_RSS_TICKER_STYLE).toString()
        try {
            vmFactory = CMSPlayerVMFactory(application)
            viewModel = ViewModelProvider(
                this@MediaPlayerActivity, vmFactory
            )[YomieViewModel::class.java]
            if (viewModel != null) {
                val enList: ArrayList<En?> =
                    MediaActivity.viewModel?.getEnTranslation() as ArrayList<En?>
                val frList: ArrayList<Fr?> =
                    MediaActivity.viewModel?.getFrTranslation() as ArrayList<Fr?>
                val nlList: ArrayList<Nl?> =
                    MediaActivity.viewModel?.getNlTranslation() as ArrayList<Nl?>
                if (enList.isNotEmpty() && frList.isNotEmpty() && nlList.isNotEmpty()) {
                    en = enList[0]
                    fr = frList[0]
                    nl = nlList[0]
                }
            }
        } catch (ex: Exception) {
            AppUtils.logError(ApiInterfaceErrorLog.TYPE_ERROR, AppUtils.appendExp(ex))
        }
        allSet()
        mClient = OkHttpClient()
        deviceId = preferences.getString(AppPreferences.KEY_DEVICE_ID).toString()
        installationId = preferences.getString(AppPreferences.KEY_INSTALLATION_ID).toString()
        sendSocketEvent()
        val apiPlayerStatus = ApiInterfaceCMSPlayer.create()
            .setPlayerStatus(preferences.getInt(AppPreferences.KEY_PLAYER_PKID))
        apiPlayerStatus.enqueue(object : Callback<PlayerStatusResponse> {
            override fun onResponse(
                call: Call<PlayerStatusResponse>, response: Response<PlayerStatusResponse>
            ) {
                if (response.isSuccessful) AppConstants.isToCheckPlaylist = false
            }

            override fun onFailure(call: Call<PlayerStatusResponse>, t: Throwable) {
                AppUtils.logError(
                    "${this::class.simpleName}\n${t.message}", ApiInterfaceErrorLog.TYPE_ERROR
                )
            }

        })
        playerId.text =
            getString(R.string.playerId, preferences.getString(AppPreferences.KEY_PLAYER_ID))
        claimId.text =
            getString(R.string.claimId, preferences.getString(AppPreferences.KEY_CLAIM_ID))

        /////PLAYER SCHEDULING/////
        playerSchedule = viewModel!!.getSchedule() as ArrayList<PlayerSchedule>
        schedulePlayer()
        Firebase.crashlytics.setUserId(preferences.getString(AppPreferences.KEY_PLAYER_ID)!!)
        AppUtils.logError(
            "Media playing started", ApiInterfaceErrorLog.TYPE_INFO
        )
    }

    override fun setUpClicks() {
        var lastClick: Long = 0
        dTap.setOnTouchListener { p0, p1 ->
            val currentClick: Long = System.currentTimeMillis()
            if (currentClick - lastClick < TIME_INTERVAL) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    openBottomSheet()
                }
            }
            lastClick = currentClick
            false
        }
    }

    override fun onResume() {
        startSocketLoop()
        //heapLoop()
        setClockFormat()
        if (timer != null) timer!!.start()

        if (player != null) {
            player!!.play()
        }
        if (::merlin.isInitialized)
            merlin.bind()
        //if (merlin != null) merlin!!.bind()
        super.onResume()
    }

    override fun onStop() {
        AppUtils.logError("Player closed/minimized by user", ApiInterfaceErrorLog.TYPE_INFO)
        socketTimer!!.cancel()
        //heapTimer!!.cancel()

        if (player != null) {
            player!!.pause()
        }
        if (timer != null) timer!!.cancel()
        if (playBackTimer != null) playBackTimer!!.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        onDestroyPlayerView()
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        mediaList!!.clear()
        mediaList = null
        rssFeedList!!.clear()
        rssFeedList = null
        playerSchedule!!.clear()
        playerSchedule = null
        viewModel = null
        en = null
        fr = null
        nl = null
        if (socketTimer != null) {
            socketTimer!!.cancel()
            socketTimer = null
        }
        if (playBackTimer != null) {
            playBackTimer!!.cancel()
            playBackTimer = null
        }
        binding = null
        /*if (merlin != null) merlin!!.unbind()
        merlin = null*/
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
        if (::merlin.isInitialized)
            merlin.unbind()
        super.onDestroy()
    }

    private fun onDestroyPlayerView() {
        if (player != null) {
            player!!.pause()
            if (player!!.mediaItemCount > 0) {
                player!!.clearMediaItems()
            }
            player!!.stop()
            player!!.clearVideoSurface()
            player!!.removeListener(this)
            player!!.release()
            player = null
        }
    }

    override fun onChanges(i: Int) {
        runOnUiThread {
            synTxt(i)
        }
    }

    override fun onDeletePlayer() {
        runOnUiThread {
            alertDialog()
        }
    }

    override fun onPreparing(text: String) {
        runOnUiThread {
            syncTv.text = text
            validMedia = false
            checkValidMedia(true)
        }
    }

    override fun onTimeMatch() {
        AppUtils.logError(
            "App is going to restart to maintenance", ApiInterfaceErrorLog.TYPE_INFO
        )
        Handler(Looper.getMainLooper()).postDelayed({
            restartApp()
        }, 3000)
    }

    override fun onScheduleTimeOccur() {
        schedulePlayer()
    }

    override fun onSkip() {
        validate(false)
    }

    override fun onPlayBackEvent(eventType: Int) {
        if (eventType == PLAYBACK_PAUSE) {
            if (player != null) player!!.pause()
        } else if (eventType == PLAYBACK_RESUME) {
            if (player != null) player!!.play()
        }
    }

    fun checkCurrentDate() {
        val apiTimeZone = ApiInterfaceTimeZone.create().getTimeDate()
        apiTimeZone.enqueue(object : Callback<Timezone> {
            override fun onResponse(
                call: Call<Timezone>, response: Response<Timezone>
            ) {
                if (response.body()?.status.equals("SUCCESS")) {
                    val d: List<String> = response.body()!!.data!!.dateTime!!.split(" ")
                    val date = Calendar.getInstance().time.toString("yyyy-MM-dd")
                    if (date == d[0]) {
                        if (!preferences.getBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE) && !preferences.getBoolean(
                                AppPreferences.KEY_IS_SOCKET_CONNECTED
                            )
                        ) YomieApp.connectSocket()
                        preferences.setBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE, true)
                        preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, false)
                    } else {
                        preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, false)
                        AppConstants.tempDateTime = response.body()!!.data!!.dateTime!!
                        preferences.setBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE, false)
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkCurrentDate()
                        }, 30000)
                    }
                } else {
                    preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        checkCurrentDate()
                    }, 30000)
                }
            }

            override fun onFailure(call: Call<Timezone>, t: Throwable) {
                preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, true)
                AppUtils.logError(t.stackTraceToString(), "TIMEZONE_CAll_FAILURE")
                t.printStackTrace()
                Handler(Looper.getMainLooper()).postDelayed({
                    checkCurrentDate()
                }, 30000)
            }
        })
    }

    fun getFileToDelete() {
        val files: ArrayList<String> = ArrayList()
        mediaList?.forEach {
            if (it.MediaType!! == "Website") {
                files.add(AppUtils.getExt(it.Thumb!!))
            }
        }
        if (files.size > 0) {
            stopPlayout()
            var del = 0
            for (it in files) {
                val file = File(
                    Environment.getExternalStorageDirectory().absolutePath +
                            "/Android/data/${YomieApp.getContext().packageName}" +
                            "/files/storage/emulated/0$it"
                )
                if (file.delete())
                    del++
                if (del == files.size) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, MediaActivity::class.java))
                        finish()
                        AppConstants.isImagesUpdated = true
                    }, 1500)

                }

            }
        }
    }

    private fun stopPlayout() {
        playOutRl.visibility = View.GONE
        noPlayOutRl.visibility = View.VISIBLE
        mediaMsg.visibility = View.VISIBLE
        if (en != null && fr != null && nl != null) {
            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                AppConstants.English_table_name -> mediaMsg.text = en?.update_images
                AppConstants.French_table_name -> mediaMsg.text = fr?.update_images
                AppConstants.Dutch_table_name -> mediaMsg.text = nl?.update_images
            }
        } else mediaMsg.text = getString(R.string.update_images)
        if (timer != null) {
            timer!!.cancel()
            timer = null
            mediaList!!.clear()
            addFragmentToActivity(EmptyFragment())
            isPlayOutPlaying = false
        }
        if (player != null) {
            player = null
            isPlaybackPlaying = false
        }
    }
}