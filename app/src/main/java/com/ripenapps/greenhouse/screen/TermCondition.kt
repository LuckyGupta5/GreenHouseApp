package com.ripenapps.greenhouse.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityTermConditionBinding
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.setVisibility

class TermCondition : BaseActivity() {
    private lateinit var termConditionBinding: ActivityTermConditionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@TermCondition, R.color.status_bar)
        termConditionBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_term_condition)

        initWebView()
        initListener()
    }

    private fun initListener() {
        termConditionBinding.closeTerm.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        if (intent.getStringExtra("fromBidder") == "1") termConditionBinding.termWebView.loadUrl("http://13.235.137.221:6002/terms-condition?userType=bidder")
        else termConditionBinding.termWebView.loadUrl("http://13.235.137.221:6002/terms-condition?userType=seller")
        termConditionBinding.termWebView.settings.javaScriptEnabled = true
        termConditionBinding.termWebView.settings.safeBrowsingEnabled = true
        termConditionBinding.termWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                termConditionBinding.idProgressBar.setVisibility(false)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (termConditionBinding.termWebView.canGoBack()) {
            termConditionBinding.termWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}