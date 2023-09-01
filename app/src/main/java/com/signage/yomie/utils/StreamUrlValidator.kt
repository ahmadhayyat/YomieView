package com.signage.yomie.utils

import android.app.Activity
import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object StreamUrlValidator {

    fun isStreamUrlValid(
        urlString: String, activity: Activity, listener: OnStreamUrlValidationListener
    ) {
        if (urlString.endsWith(".mp3")) {
            object : Thread() {
                override fun run() {
                    val con = URL(urlString).openConnection() as HttpURLConnection
                    try {
                        HttpURLConnection.setFollowRedirects(false)
                        // note : you may also need
                        //HttpURLConnection.setInstanceFollowRedirects(false)
                        con.requestMethod = "GET"
                        if (con.responseCode == HttpURLConnection.HTTP_OK) {
                            activity.runOnUiThread { listener.onValidationResult(true) }
                            con.disconnect()
                        } else {
                            activity.runOnUiThread { listener.onValidationResult(false) }
                            con.disconnect()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        con.disconnect()

                    }
                }
            }.start()
        } else {

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "HEAD"
                    val responseCode = connection.responseCode
                    // HTTP 2xx means the URL is valid and reachable
                    val isValid = responseCode in 200..299
                    activity.runOnUiThread {
                        listener.onValidationResult(isValid)
                    }
                } catch (e: IOException) {
                    // Connection error, URL might be invalid or unreachable
                    activity.runOnUiThread {
                        listener.onValidationResult(false)

                    }
                }

            }
        }

    }


    fun urlValidator(url: String, context: Context, listener: OnStreamUrlValidationListener) {
        val exoPlayer = ExoPlayer.Builder(context).build()


        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, "user-agent")
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))


        exoPlayer.setMediaSource(mediaSource)


        exoPlayer.prepare()



        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                try {
                    if (playbackState == ExoPlayer.STATE_READY) {
                        listener.onValidationResult(true)
                        exoPlayer.release()
                    } else {
                        listener.onValidationResult(false)
                        exoPlayer.release()
                        // There might be an error or the stream is not ready
                    }
                } catch (ex: Exception) {
                    listener.onValidationResult(false)
                }
            }
        })
    }


    interface OnStreamUrlValidationListener {
        fun onValidationResult(isValid: Boolean)
    }
}
