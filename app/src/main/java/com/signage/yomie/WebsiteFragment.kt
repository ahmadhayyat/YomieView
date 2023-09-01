package com.signage.yomie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.dispose
import coil.load
import com.signage.yomie.commons.AppPreferences
import com.signage.yomie.databinding.FragmentWebsiteBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import java.io.File


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class WebsiteFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var url: String? = null
    private var param2: String? = null
    private var binding: FragmentWebsiteBinding? = null
    private lateinit var webView: WebView
    private lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebsiteBinding.inflate(layoutInflater)
        val view = binding!!.root
        webView = binding!!.webVw
        webView.resumeTimers()
        imgView = binding!!.webImgVw
        if (AppUtils.isOnline(requireContext()) && preferences.getBoolean(AppPreferences.KEY_IS_DATE_TIME_UPDATE)) {
            val wvSetting = webView.settings
            wvSetting.javaScriptEnabled = true
//            wvSetting.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            wvSetting.domStorageEnabled = true
            wvSetting.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    AppUtils.logError("$url loaded", ApiInterfaceErrorLog.TYPE_INFO)
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                }

                @Deprecated(
                    "Deprecated in Java", ReplaceWith(
                        "AppUtils.logError(description, ApiInterfaceErrorLog.TYPE_ERROR)",
                        "com.signage.yomie.utils.AppUtils",
                        "com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog"
                    )
                )
                override fun onReceivedError(
                    view: WebView, errorCode: Int, description: String, failingUrl: String
                ) {
                    AppUtils.logError(description, ApiInterfaceErrorLog.TYPE_ERROR)
                }
            }
            webView.loadUrl(url!!)
        } else {
            webView.visibility = View.GONE
            imgView.visibility = View.VISIBLE
            imgView.load(param2?.let { File(it) })

        }
        webView.setOnTouchListener { _, _ -> true }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, thumb: String) = WebsiteFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, url)
                putString(ARG_PARAM2, thumb)
            }
        }
    }

    override fun onStop() {
        destroyWeb()
        super.onStop()
    }

    private fun destroyWeb() {
        webView.clearHistory()
        webView.clearCache(true)
        webView.onPause()
        webView.pauseTimers()
        binding?.webParent?.removeView(webView)
        webView.removeAllViews()
        webView.destroy()
        url = null
        binding = null
        imgView.dispose()
        url = null
        param2 = null
    }
}