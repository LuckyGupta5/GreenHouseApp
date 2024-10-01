package com.ripenapps.greenhouse.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.view_models.WalletAddViewModel
import com.ripenapps.greenhouse.databinding.ActivityPaymentGatewayBinding
import com.ripenapps.greenhouse.model.commonModels.transaction.CreatePaymentResponseModel
import com.ripenapps.greenhouse.model.seller.response.AddAmountResponseModel
import com.ripenapps.greenhouse.screen.seller.SellerWallet
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.PreEntity
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status

@Suppress("DEPRECATION")
class PaymentGateway : BaseActivity() {
    private lateinit var binding: ActivityPaymentGatewayBinding
    private var walletAddViewModel: WalletAddViewModel? = null
    private var loadedUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =
            DataBindingUtil.setContentView(this@PaymentGateway, R.layout.activity_payment_gateway)
        initWebView()
        initWalletAddModel()
    }
    private fun initWalletAddModel() {
        walletAddViewModel = ViewModelProvider(this@PaymentGateway)[WalletAddViewModel::class.java]
        binding.lifecycleOwner = this@PaymentGateway
        walletAddViewModel?.walletAddData()?.observe(this@PaymentGateway) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                Companion.SECRET_KEY, it.data
                            ), AddAmountResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(PreEntity.TAG, "bidHistory: ${Gson().toJson(data.data)}")
                                val intent = Intent(this@PaymentGateway, SellerWallet::class.java)
                                startActivity(intent)
                                finish()
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@PaymentGateway, "token")
                                Preferences.removePreference(this@PaymentGateway, "user_details")
                                Preferences.removePreference(this@PaymentGateway, "isVerified")
                                Preferences.removePreference(this@PaymentGateway, "roomId")
                                val signin = Intent(this@PaymentGateway, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@PaymentGateway, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@PaymentGateway)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@PaymentGateway, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun walletAddMoneyApi() {
        walletAddViewModel?.request?.token = Preferences.getStringPreference(this, Constants.TOKEN)
        walletAddViewModel?.request?.amount = intent.getStringExtra("amount")
        walletAddViewModel?.request?.subCategoryName = "wallet"
        walletAddViewModel?.walletAddRequest()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.idProgressBar.visibility = View.VISIBLE
        binding.idWebView.loadUrl(intent.getStringExtra("url").toString())
        Log.d("TAG", "urlLink: ${intent.getStringExtra("url").toString()}")
        binding.idWebView.settings.javaScriptEnabled = true
        binding.idWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        binding.idWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.loadUrl(
                    "javascript:window.Android.getBodyContent(document.body.innerText);"
                )
                Log.d("TAG", "onPageFinished: $url")
                loadedUrl = url ?: ""
                binding.idProgressBar.visibility = View.GONE
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.idWebView.canGoBack()) {
            binding.idWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class WebAppInterface(private val activity: AppCompatActivity) {
        @JavascriptInterface
        fun getBodyContent(content: String) {
            // Handle the extracted content from the WebView here
            activity.runOnUiThread {
                Log.d("TAG", "getBodyContent: $content")
                if (loadedUrl.isNotEmpty()) {
                    if (getEndpointFromUrl(loadedUrl) == "/payment/status") {
                        Log.d("TAG", "getBodyContent: inside success")
                        //decrypt the content
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                Companion.SECRET_KEY, content
                            ), CreatePaymentResponseModel::class.java
                        )

                        if (data.status == 200) {
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("status", "update")
                            })
                            walletAddMoneyApi()
                        }
                    }
                }
            }
        }
    }
    fun getEndpointFromUrl(url: String): String? {
        val uri = Uri.parse(url)
        return uri.path
    }
}

