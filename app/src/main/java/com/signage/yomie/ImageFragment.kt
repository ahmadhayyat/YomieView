package com.signage.yomie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.dispose
import coil.load
import com.signage.yomie.commons.AppConstants
import com.signage.yomie.commons.AppPreferences
import com.signage.yomie.databinding.FragmentImageBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ImageFragment : Fragment() {
    private var uri: String? = null
    private var param2: String? = null
    private var binding: FragmentImageBinding? = null
    private lateinit var imageView: ImageView
    private lateinit var gifVw: GifImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uri = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(layoutInflater)
        val view = binding!!.root
        imageView = binding!!.imgVw
        gifVw = binding!!.gifVw

        val ext = uri!!.substring(uri!!.lastIndexOf(".") + 1)
        if (ext == "gif") {
            gifVw.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            val gifFromUri = GifDrawable(File(uri!!))
            gifVw.setImageDrawable(gifFromUri)
            AppUtils.logError("$uri gif loaded", ApiInterfaceErrorLog.TYPE_INFO)
        } else {
            gifVw.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            imageView.load(uri?.let { File(it) })
            if (preferences.getString(AppPreferences.KEY_PLAYER_ORIENTATION)
                    .equals(AppConstants.ORIENTATION_LANDSCAPE)
            ) {
                imageView.scaleType = ImageView.ScaleType.FIT_XY
            }
            AppUtils.logError("$uri image loaded", ApiInterfaceErrorLog.TYPE_INFO)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(uri: String, param2: String) = ImageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, uri)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onDestroy() {
        uri = null
        param2 = null
        binding = null
        imageView.dispose()
        super.onDestroy()
    }
}