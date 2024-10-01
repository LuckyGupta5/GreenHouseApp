package com.ripenapps.greenhouse.screen

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityForgotPasswordBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.ForgotPasswordVerification
import com.ripenapps.greenhouse.model.register_user.response.ForgotPassword
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.ForgotPasswordViewModel

class  ForgotPassword : BaseActivity() {
    private lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding
    private var mobileNumber: String? = null
    private val finalNumber = StringBuilder()
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel
    var accountBlocked: AccountBlocked? = null
    private var isNumber = false
    private var toForgotVerify: ForgotPasswordVerification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@ForgotPassword, R.color.status_bar)
        forgotPasswordBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_forgot_password
        )
        forgotPasswordBinding.apply {
            forgotMobileNum.setTitle(getString(R.string.mobile_number))
            forgotMobileNum.setHint(getString(R.string.enter_mobil))
            forgotMobileNum.setInputType(
                false, InputType.TYPE_CLASS_NUMBER
            )
            forgotMobileNum.setWidth()
            forgotMobileNum.setTypeFace(
                this@ForgotPassword, R.font.euclid_circular_medium
            )
            forgotMobileNum.isEnable(true)
            forgotMobileNum.setImeAction("done")
        }
        initListner()
        initViewModel()
        validationObserver()
    }

    private fun initViewModel() {
        forgotPasswordViewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        forgotPasswordBinding.lifecycleOwner = this

        forgotPasswordViewModel.getForgotPasswordData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(SECRET_KEY, it.data), ForgotPassword::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            finalNumber.clear()
                            finalNumber.append("+").append("966").append(" ").append(mobileNumber)
                            if (toForgotVerify?.isAdded != true) {
                                toForgotVerify =
                                    ForgotPasswordVerification(finalNumber, mobileNumber)
                                toForgotVerify?.show(supportFragmentManager, toForgotVerify?.tag)
                            }
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(supportFragmentManager, accountBlocked?.tag)
                            }

                        }

                        403,401 -> {
                            val signin = Intent(this@ForgotPassword, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@ForgotPassword, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListner() {
        forgotPasswordBinding.forgotMobileNum.initListener("mobile")

        forgotPasswordBinding.btnBackForgotPassword.setOnClickListener {
            finish()
        }
        forgotPasswordBinding.btnVerifyCode.setOnClickListener {
            mobileNumber = forgotPasswordBinding.forgotMobileNum.getText()
            forgotPasswordViewModel.request.mobile = mobileNumber
            forgotPasswordViewModel.request.countryCode = CC
            forgotPasswordViewModel.getForgotPasswordRequest() //api hit function
        }
    }


    private fun validationObserver() {
        forgotPasswordBinding.apply {
            forgotMobileNum.getValidatedValue().observe(this@ForgotPassword) { validated ->
                isNumber = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (isNumber) {
            forgotPasswordBinding.btnVerifyCode.isEnabled = true
            changeButtonColor()
        } else {
            forgotPasswordBinding.btnVerifyCode.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        forgotPasswordBinding.apply {
            btnVerifyCode.setBackgroundColor(
                ContextCompat.getColor(
                    this@ForgotPassword, R.color.greenhousetheme
                )
            )
        }
    }
}