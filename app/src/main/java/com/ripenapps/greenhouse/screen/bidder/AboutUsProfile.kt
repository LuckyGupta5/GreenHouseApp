package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityAboutUsPofileBinding
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Preferences

class AboutUsProfile : BaseActivity() {
    private lateinit var aboutUSProfileBinding: ActivityAboutUsPofileBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@AboutUsProfile, R.color.status_bar)
        super.onCreate(savedInstanceState)
        aboutUSProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_about_us_pofile)
        if (Preferences.getBooleanPreference(this@AboutUsProfile, "isBidder")) {
            aboutUSProfileBinding.aboutUsWebView.loadUrl("http://13.235.137.221:6002/about-us?userType=bidder")
        } else {
            aboutUSProfileBinding.aboutUsWebView.loadUrl("http://13.235.137.221:6002/about-us?userType=seller")
        }
        aboutUSProfileBinding.aboutUsWebView.settings.javaScriptEnabled = true
        aboutUSProfileBinding.aboutUsWebView.settings.safeBrowsingEnabled = true

        aboutUSProfileBinding.aboutUsWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                aboutUSProfileBinding.pgbarAboutUs.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                aboutUSProfileBinding.pgbarAboutUs.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }

        aboutUSProfileBinding.btnCloseAboutUs.setOnClickListener {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (aboutUSProfileBinding.aboutUsWebView.canGoBack()) {
            aboutUSProfileBinding.aboutUsWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}