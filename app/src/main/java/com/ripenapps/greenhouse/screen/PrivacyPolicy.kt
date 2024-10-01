package com.ripenapps.greenhouse.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityPrivacyPolicyBinding
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor

@Suppress("DEPRECATION")
class PrivacyPolicy : BaseActivity() {
    private lateinit var privacyPolicyBinding: ActivityPrivacyPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@PrivacyPolicy, R.color.status_bar)
        privacyPolicyBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)

        initWebView()
        privacyPolicyBinding.closePrivacy.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        if (intent.getStringExtra("fromBidder") == "1") privacyPolicyBinding.privacyWebView.loadUrl(
            "http://13.235.137.221:6002/privacy-policy?userType=bidder"
        )
        else privacyPolicyBinding.privacyWebView.loadUrl("http://13.235.137.221:6002/privacy-policy?userType=seller")
        privacyPolicyBinding.privacyWebView.settings.javaScriptEnabled = true
        privacyPolicyBinding.privacyWebView.settings.safeBrowsingEnabled = true
        privacyPolicyBinding.privacyWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                privacyPolicyBinding.pdBarPrivacy.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                privacyPolicyBinding.pdBarPrivacy.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (privacyPolicyBinding.privacyWebView.canGoBack()) {
            privacyPolicyBinding.privacyWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}