package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.view_models.CreatePaymentViewModel
import com.ripenapps.greenhouse.view_models.WalletWithdrawViewModel
import com.ripenapps.greenhouse.databinding.ActivityAddMoneyInWalletBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.commonModels.transaction.CreatePaymentResponseModel
import com.ripenapps.greenhouse.model.seller.response.AddAmountResponseModel
import com.ripenapps.greenhouse.screen.PaymentGateway
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.SellerWallet
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.PreEntity
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status

class AddMoneyInWallet : BaseActivity() {
    private lateinit var addMoneyInWalletBinding: ActivityAddMoneyInWalletBinding
    private var createPaymentViewModel: CreatePaymentViewModel? = null
    private var accountBlocked: AccountBlocked? = null
    private var walletWithdraw: WalletWithdrawViewModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@AddMoneyInWallet, R.color.status_bar)
        super.onCreate(savedInstanceState)
        addMoneyInWalletBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_money_in_wallet)
        addMoneyInWalletBinding.availBalance.text =
            getString(R.string.sar) + " " + intent.getStringExtra("availableBalance")

        if (intent.getStringExtra("forAddMoney") == "2") {
            addMoneyInWalletBinding.apply {
                addMoneyToWallet.text = getString(R.string.amount_)
                proceedToAdd.text = getString(R.string.withdraw)
            }
        }

        addMoneyInWalletBinding.enterNumber.doAfterTextChanged {
            if (addMoneyInWalletBinding.enterNumber.text.isNullOrEmpty() || addMoneyInWalletBinding.enterNumber.text.toString() == "0") {
                addMoneyInWalletBinding.proceedToAdd.setBackgroundColor(
                    ContextCompat.getColor(
                        this@AddMoneyInWallet, R.color.inactive_background
                    )
                )
                addMoneyInWalletBinding.proceedToAdd.isEnabled = false
            } else {
                addMoneyInWalletBinding.proceedToAdd.setBackgroundColor(
                    ContextCompat.getColor(
                        this@AddMoneyInWallet, R.color.greenhousetheme
                    )
                )
                addMoneyInWalletBinding.proceedToAdd.isEnabled = true
            }
        }

        initCreatePaymentViewModel()
        initWalletWithDrawModel()
        initListener()
    }

    private fun initWalletWithDrawModel() {
        walletWithdraw =
            ViewModelProvider(this@AddMoneyInWallet)[WalletWithdrawViewModel::class.java]
        addMoneyInWalletBinding.lifecycleOwner = this@AddMoneyInWallet
        walletWithdraw?.walletWithdrawData()?.observe(this@AddMoneyInWallet) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            AddAmountResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(PreEntity.TAG, "wallerWithdrawResponse: ${Gson().toJson(data.data)}")
                                val intent = Intent(this@AddMoneyInWallet, SellerWallet::class.java)
                                startActivity(intent)
                                finish()
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@AddMoneyInWallet, "token")
                                Preferences.removePreference(this@AddMoneyInWallet, "user_details")
                                Preferences.removePreference(this@AddMoneyInWallet, "isVerified")
                                Preferences.removePreference(this@AddMoneyInWallet, "roomId")
                                val signin = Intent(this@AddMoneyInWallet, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@AddMoneyInWallet, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@AddMoneyInWallet)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@AddMoneyInWallet, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initCreatePaymentViewModel() {
        createPaymentViewModel =
            ViewModelProvider(this@AddMoneyInWallet)[CreatePaymentViewModel::class.java]
        addMoneyInWalletBinding.lifecycleOwner = this@AddMoneyInWallet
        createPaymentViewModel?.getPaymentData()?.observe(this@AddMoneyInWallet) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            CreatePaymentResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(PreEntity.TAG, "walletAddMoneyResponse: ${Gson().toJson(data.data)}")
                                val toPaymentGateway =
                                    Intent(this@AddMoneyInWallet, PaymentGateway::class.java)
                                toPaymentGateway.putExtra("url", data.data?.redirectUrl)
                                toPaymentGateway.putExtra(
                                    "amount", addMoneyInWalletBinding.enterNumber.text.toString()
                                )
                                addMoneyInWalletBinding.enterNumber.text?.clear()
                                startActivity(toPaymentGateway)
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@AddMoneyInWallet, "token")
                                Preferences.removePreference(this@AddMoneyInWallet, "user_details")
                                Preferences.removePreference(this@AddMoneyInWallet, "isVerified")
                                Preferences.removePreference(this@AddMoneyInWallet, "roomId")
                                val signin = Intent(this@AddMoneyInWallet, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@AddMoneyInWallet, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@AddMoneyInWallet)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@AddMoneyInWallet, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun walletWithdrawApi() {
        walletWithdraw?.request?.token = Preferences.getStringPreference(this, TOKEN)
        walletWithdraw?.request?.amount =  addMoneyInWalletBinding.enterNumber.text.toString()
        walletWithdraw?.request?.subCategoryName = "wallet"
        walletWithdraw?.walletWithdrawRequest()
    }

    private fun initListener() {
        //cross button
        addMoneyInWalletBinding.crossAddMoneyWallet.setOnClickListener {
            finish()
        }

        addMoneyInWalletBinding.proceedToAdd.setOnClickListener {
            if (intent.getStringExtra("forAddMoney") == "1") {
                createPaymentViewModel?.request?.token =
                    Preferences.getStringPreference(this@AddMoneyInWallet, TOKEN)
                createPaymentViewModel?.request?.amount =
                    addMoneyInWalletBinding.enterNumber.text.toString()
                createPaymentViewModel?.getPaymentRequest()
            } else {
                walletWithdrawApi()
            }
        }
    }
}