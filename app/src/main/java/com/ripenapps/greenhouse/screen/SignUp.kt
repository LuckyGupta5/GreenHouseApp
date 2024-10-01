package com.ripenapps.greenhouse.screen

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySignUpBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.SignupMobileVerification
import com.ripenapps.greenhouse.model.register_user.response.SendOtpResponseModel
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.SendOtpViewModel

class SignUp : BaseActivity() {
    private var number: String? = null
    private val finalMobileNumber = StringBuilder()
    private var user: Int = 1
    private var userType: String? = null
    private var isNumber = false
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var sendOtpViewModel: SendOtpViewModel
    var accountBlocked: AccountBlocked? = null
    private var mobileVerification: SignupMobileVerification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SignUp, R.color.status_bar)
        signUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        initListner()
        initViewModel()
        validationObserver()

        signUpBinding.apply {
            signupMobileNumber.setTitle(getString(R.string.mobile_number))
            signupMobileNumber.setHint(getString(R.string.enter_mobil))
            signupMobileNumber.setInputType(
                false, InputType.TYPE_CLASS_NUMBER
            )
            signupMobileNumber.setWidth()
            signupMobileNumber.setTypeFace(
                this@SignUp, R.font.euclid_circular_medium
            )
            signupMobileNumber.setValidationValue(isNumber)
            signupMobileNumber.setImeAction("done")
            signupMobileNumber.textDirection(View.TEXT_DIRECTION_LTR)
        }
    }

    private fun initViewModel() {
        sendOtpViewModel = ViewModelProvider(this)[SendOtpViewModel::class.java]
        signUpBinding.lifecycleOwner = this

        sendOtpViewModel.getOtpData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    ProcessDialog.dismissDialog()
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        SendOtpResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            if (mobileVerification?.isAdded != true) {
                                mobileVerification = SignupMobileVerification(
                                    finalMobileNumber, user, userType, number
                                )
                                mobileVerification?.show(
                                    supportFragmentManager, mobileVerification?.tag
                                )
                            }
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(supportFragmentManager, accountBlocked?.tag)
                            }
                        }

                        403,401 -> {
                            val signin = Intent(this@SignUp, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this@SignUp)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    ProcessDialog.dismissDialog()
                    Toast.makeText(this@SignUp, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListner() {
        signUpBinding.signupMobileNumber.initListener("mobile")

        signUpBinding.btnBidder.setOnClickListener {
            signUpBinding.radio1.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SignUp, R.drawable.ic_radio_button_selected
                )
            )
            signUpBinding.radio2.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SignUp, R.drawable.ic_radio_button_unselected
                )
            )
            user = 1
        }

        signUpBinding.btnSeller.setOnClickListener {
            signUpBinding.radio2.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SignUp, R.drawable.ic_radio_button_selected
                )
            )
            signUpBinding.radio1.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SignUp, R.drawable.ic_radio_button_unselected
                )
            )
            user = 2
        }

        signUpBinding.idSignIn.setOnClickListener {
            val toSignIn = Intent(this, SignIn::class.java)
            startActivity(toSignIn)
            finish()
        }

        signUpBinding.btnCloseSignUp.setOnClickListener {
            val backToSignin = Intent(this, SignIn::class.java)
            startActivity(backToSignin)
            finish()
        }

        signUpBinding.btnVerifySignup.setOnClickListener {
            number = signUpBinding.signupMobileNumber.getText()
            finalMobileNumber.clear()
            finalMobileNumber.append("+").append("966").append(" ").append(number)
//            finalMobileNumber = CC+number

            //api hit function
            userType = if (user == 1) {
                "bidder"
            } else {
                "seller"
            }
            sendOtpViewModel.request.userType = userType
            sendOtpViewModel.request.mobile = signUpBinding.signupMobileNumber.getText()
            sendOtpViewModel.request.countryCode = CC
            sendOtpViewModel.getSendOtpRequest()
        }
    }

    private fun validationObserver() {
        signUpBinding.apply {
            signupMobileNumber.getValidatedValue().observe(this@SignUp) { validated ->
                isNumber = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (isNumber) {
            signUpBinding.btnVerifySignup.isEnabled = true
            changeButtonColor()
        } else {
            signUpBinding.btnVerifySignup.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        signUpBinding.apply {
            btnVerifySignup.setBackgroundColor(
                ContextCompat.getColor(
                    this@SignUp, R.color.greenhousetheme
                )
            )
        }
    }
}