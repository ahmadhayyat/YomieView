package com.signage.yomie.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.Context.ACTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.signage.yomie.*
import com.signage.yomie.CMSPlayer.network.ApiInterfaceCMSPlayer
import com.signage.yomie.CMSPlayer.network.RequestParameter
import com.signage.yomie.CMSPlayer.network.medialist.PlayerMediaList
import com.signage.yomie.commons.AppConstants
import com.signage.yomie.commons.AppPreferences
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import com.signage.yomie.utils.ErrorLog.ErrorLogResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AppUtils {
    companion object {
        private var handler: Handler? = null
        private var runnable: Runnable? = null
        fun generateIds(length: Int): String {
            val allowedChars = ('A'..'Z') + ('0'..'9')
            return (1..length).map { allowedChars.random() }.joinToString("")
        }

        fun copyToClipboard(context: Context?, text: String) {
            val clipboard: ClipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Yomie", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        }

        @SuppressLint("HardwareIds")
        fun getDeviceId(context: Context?): String {
            return Settings.Secure.getString(
                context?.contentResolver, Settings.Secure.ANDROID_ID
            )
        }

        fun getFileExt(uri: Uri, context: Context): String? {
            val contentResolver: ContentResolver = context.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
        }


        fun Date.toString(format: String): String {
            val locale: Locale = Locale.getDefault()
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        fun getCurrentDateTime(): Date {
            return if (preferences.getBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE))
                Calendar.getInstance().time
            else if (preferences.getBoolean(AppPreferences.KEY_IS_CACHE_TIME))
                preferences.getString(AppPreferences.KEY_PLAYER_CACHE_TIME)!!.toDate()
            else AppConstants.tempDateTime.toDate()
        }

        fun isWithinRange(type: String, cDate: String, sDate: String, eDate: String): Boolean {
            val formatter: SimpleDateFormat = if (type.equals("d", true)) {
                SimpleDateFormat("yyyy-MM-dd")
            } else {
                SimpleDateFormat("HH:mm:ss")
            }

            val cD = formatter.parse(cDate)
            val sD = formatter.parse(sDate)
            val eD = formatter.parse(eDate)

            //if ((cD == sD || cD.after(sD)) && (cD.before(eD))) return true
            if ((cD == sD || cD!!.after(sD)) && (cD == eD || cD!!.before(eD))) return true

            return false
        }

        fun String.containsAnyOfIgnoreCase(keywords: List<String?>?): Boolean {
            for (keyword in keywords!!) {
                if (this.contains(keyword!!, true)) return true
            }
            return false
        }


        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return true
            }
            return false
        }

        fun logError(
            errorInfo: String, type: String
        ) {
            val playerId = preferences.getString(AppPreferences.KEY_PLAYER_ID)
            val claimId = preferences.getString(AppPreferences.KEY_CLAIM_ID)
            val apiInterface = ApiInterfaceErrorLog.create()
                .errorLog(RequestParameter.ErrorLog(errorInfo, playerId!!, claimId!!, type))
            apiInterface.enqueue(object : Callback<ErrorLogResponse> {
                override fun onResponse(
                    call: Call<ErrorLogResponse>, response: Response<ErrorLogResponse>
                ) {

                }

                override fun onFailure(call: Call<ErrorLogResponse>, t: Throwable) {

                }

            })
            val text = "\n[${type.uppercase()}]\t$errorInfo\t[${getCurrentDateTime()}]"
            makeLogFile(text)
        }

        fun makeLogFile(text: String) {
            val logFile = File(
                Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/" + "${
                    getCurrentDateTime().toString(
                        "YYYY-MM-dd"
                    )
                }-log/"
            )
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(text)
                buf.newLine()
                buf.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun deleteDB(context: Context) {
            preferences.storeIds(
                "", "", "", ""
            )
            //preferences.setBoolean(AppPreferences.KEY_IS_REG, false)
            preferences.deletePref()
            val sharedPref = File(context.applicationInfo?.dataDir.toString() + "/shared_prefs")
            deleteRecursive(sharedPref)
            val databases = File(context.applicationInfo?.dataDir.toString() + "/databases")
            deleteRecursive(databases)
            val files = File(
                Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0/"
            )
            deleteRecursive(files)

        }

         fun deleteRecursive(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
                child
            )
            if (fileOrDirectory.delete()) Log.i("DBDelete", "$fileOrDirectory")

        }

        fun appendExp(ex: Exception): String {
            var exp = ""
            for (i in 0 until ex.stackTrace.size) {
                exp += "${ex.stackTrace[i]}\n"
            }
            return exp
        }


        fun getHeapSize(context: Context): String {
            val mi = ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            activityManager.getMemoryInfo(mi)
            val availableMegs: Double = mi.availMem.toDouble() / 1000000000.toDouble()
            return "${
                String.format(
                    "%.2f", availableMegs
                )
            }GB/${
                String.format(
                    "%.2f", (mi.totalMem.toDouble() / 1000000000.toDouble())
                )
            }GB"
        }

        fun getExt(url: String): String {
            return url.substring(url.lastIndexOf("/"))
        }

        fun Context.dpToPx(dp: Int): Int {
            return (dp * resources.displayMetrics.density).toInt()
        }

        fun Context.pxToDp(px: Int): Int {
            return (px / resources.displayMetrics.density).toInt()
        }


        fun getDayInt(): Int {
            return when (getCurrentDateTime().toString("EEE")) {
                "Mon" -> 1
                "Tue" -> 2
                "Wed" -> 3
                "Thu" -> 4
                "Fri" -> 5
                "Sat" -> 6
                "Sun" -> 7
                else -> 0
            }
        }

        fun scheduleSocketReconnection() {
            if (handler != null) {
                handler!!.removeCallbacks(runnable!!)
                handler!!.removeCallbacksAndMessages(null)
            }
            handler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                if (!socketManager.isConnected) {
                    socketManager.connectSocket()
                }

                Log.i("SOCKETT", "handler ran")
            }
            handler!!.postDelayed(runnable!!, 30000)
        }

        fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
            if (v.layoutParams is MarginLayoutParams) {
                val p = v.layoutParams as MarginLayoutParams
                p.setMargins(l, t, r, b)
                v.requestLayout()
            }
        }

        fun timeToMillis(time: String): Long {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            try {
                val mDate = sdf.parse(time)
                if (mDate != null) {
                    return mDate.time
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return 0
        }

        private fun String.toDate(): Date {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.parse(this)!!
            } catch (_: Exception) {
                getCurrentDateTime()
            }

        }

        fun millisToTime(millis: Long): String {
            return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
            )
        }

        fun checkForPlaylistChange() {
            val apiInterface = ApiInterfaceCMSPlayer.create().getDeviceMedia(
                preferences.getString(AppPreferences.KEY_DEVICE_ID).toString(),
                preferences.getString(AppPreferences.KEY_INSTALLATION_ID).toString(),
                BuildConfig.VERSION_NAME
            )
            apiInterface.enqueue(object : Callback<PlayerMediaList> {
                override fun onResponse(
                    call: Call<PlayerMediaList>, response: Response<PlayerMediaList>
                ) {
                    if (response.body()?.PlayerStatus.equals("notupdated")) {
                        val intent = Intent(YomieApp.getContext(), MediaActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        YomieApp.getContext().startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<PlayerMediaList>, t: Throwable) {
                }

            })
        }

    }
}