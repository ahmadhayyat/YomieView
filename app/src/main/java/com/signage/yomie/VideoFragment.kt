package com.signage.yomie

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.signage.yomie.CMSPlayer.network.medialist.DataItem
import com.signage.yomie.commons.*
import com.signage.yomie.databinding.FragmentVideoBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog


private const val ARG_PARAM1 = "param1"

class VideoFragment : Fragment(), Player.Listener {
    private var dataItem: DataItem? = null
    private var player: ExoPlayer? = null
    private var exoPlayer: StyledPlayerView? = null
    private var binding: FragmentVideoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataItem = it.getParcelable(ARG_PARAM1)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(layoutInflater)
        val view = binding!!.root
        exoPlayer = binding!!.exoP
        return view
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        AppUtils.logError(AppUtils.appendExp(error!!), ApiInterfaceErrorLog.TYPE_ERROR)
        smi!!.onSkip()
        super.onPlayerErrorChanged(error)
    }

    override fun onPlayerError(error: PlaybackException) {
        error.printStackTrace()
        AppUtils.logError(AppUtils.appendExp(error), ApiInterfaceErrorLog.TYPE_ERROR)
        AppUtils.logError(error.stackTrace.toString(), ApiInterfaceErrorLog.TYPE_ERROR)
        AppUtils.logError(error.stackTraceToString(), ApiInterfaceErrorLog.TYPE_ERROR)
        error.localizedMessage?.let { AppUtils.logError(it, ApiInterfaceErrorLog.TYPE_ERROR) }
        error.message?.let { AppUtils.logError(it, ApiInterfaceErrorLog.TYPE_ERROR) }
        smi!!.onSkip()
        AppUtils.logError(
            "error on-> ${dataItem?.Content?.substring(dataItem!!.Content!!.lastIndexOf("/"))}",
            ApiInterfaceErrorLog.TYPE_INFO
        )
        /*val errorCounter = AppConstants.playerErrorCounter
        AppConstants.playerErrorCounter += 1
        if (errorCounter == 3) {
            AppUtils.logError(
                "App restarted due to continuously player error",
                ApiInterfaceErrorLog.TYPE_INFO
            )
            AppConstants.playerErrorCounter = 0
            ri.onTimeMatch()
        }*/
        super.onPlayerError(error)
    }

    private fun createMediaItem(): MediaItem {
        var uri =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/${YomieApp.getContext().packageName}/files/storage/emulated/0"
        val name = dataItem?.Content?.substring(dataItem!!.Content!!.lastIndexOf("/")).toString()
        uri += name
        val mediaUri = Uri.parse(uri)
        AppUtils.logError("$uri video loaded", ApiInterfaceErrorLog.TYPE_INFO)
        return MediaItem.fromUri(mediaUri)
    }

    companion object {
        private var timeInterval: Long = 1000
        var handler: Handler? = null
        var runnable: Runnable? = null
        var smi: SkipMediaInterface? = null
        var pbi: PlayBackInterface? = null
        fun setSkipMediaInterface(skipMediaInterface: SkipMediaInterface) {
            smi = skipMediaInterface
        }

        fun setPlayBackInterface(playBackInterface: PlayBackInterface) {
            pbi = playBackInterface
        }

        @JvmStatic
        fun newInstance(dataItem: DataItem) = VideoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM1, dataItem)
            }
        }

        private lateinit var ri: RestartInterface
        fun setRestartInterface(restartInterface: RestartInterface) {
            ri = restartInterface
        }
    }

    override fun onResume() {
        player = ExoPlayer.Builder(requireContext()).build()
        exoPlayer!!.player = player
        if (preferences.getString(AppPreferences.KEY_PLAYER_ORIENTATION)
                .equals(AppConstants.ORIENTATION_LANDSCAPE)
        ) {
            exoPlayer!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
        player!!.addListener(this)
        player!!.addMediaItem(createMediaItem())
        player!!.playWhenReady = true
        player!!.prepare()
        if (dataItem!!.Mute!!.toInt() == 0) {
            val vol = if (dataItem!!.Volume!! < 100) "0.${dataItem!!.Volume}"
            else "1.0"
            player!!.volume = vol.toFloat()
            pbi!!.onPlayBackEvent(MediaPlayerActivity.PLAYBACK_PAUSE)
        } else {
            player!!.volume = 0.0F
        }
        prepareSeek()
        super.onResume()
    }

    private fun prepareSeek() {
        if (!dataItem!!.VideoStartFrom.isNullOrEmpty()) {
            player!!.seekTo(dataItem!!.VideoStartFrom!!.toLong() * timeInterval)
        }
        startHandler()
    }

    private fun startHandler() {
        if (!dataItem!!.VideoStartFrom.isNullOrEmpty() or !dataItem!!.VideoEndOn.isNullOrEmpty()) {
            player!!.repeatMode = Player.REPEAT_MODE_OFF
            runnable = Runnable {
                if ((player!!.currentPosition / timeInterval) == dataItem!!.VideoEndOn!!.toLong()) {
                    if (!dataItem?.VideoStartFrom.isNullOrEmpty())
                        player!!.seekTo(dataItem!!.VideoStartFrom!!.toLong() * timeInterval)
                    else player!!.seekTo("0".toLong() * timeInterval)
                }
                startHandler()
            }
            handler = Handler(Looper.getMainLooper())
            handler!!.postDelayed(runnable!!, timeInterval)
        } else {
            player!!.repeatMode = Player.REPEAT_MODE_ONE
        }
        player!!.play()
    }

    override fun onPause() {
        player!!.pause()
        handler?.removeCallbacks(runnable!!)
        pbi!!.onPlayBackEvent(MediaPlayerActivity.PLAYBACK_RESUME)
        super.onPause()
    }

    override fun onStop() {
        if (player != null) {
            player!!.stop()
            if (player!!.mediaItemCount > 0) {
                player!!.clearMediaItems()
            }
            player!!.release()
        }
        super.onStop()
    }

    private fun onDestroyPlayerView() {
        if (player != null) {
            try {
                //player!!.release()
                player!!.clearVideoSurface()
                player!!.removeListener(this)
                player = null
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        exoPlayer!!.player = null
        exoPlayer = null
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY && player!!.playWhenReady) {
            val currentMediaItem = player?.currentMediaItem

            // Access information about the currently playing media
            val mediaUri = currentMediaItem?.localConfiguration?.uri
            AppUtils.logError("now playing $mediaUri", ApiInterfaceErrorLog.TYPE_INFO)
        }
    }

    override fun onDestroy() {
        onDestroyPlayerView()
        dataItem = null
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null
        binding = null
        super.onDestroy()
    }
}