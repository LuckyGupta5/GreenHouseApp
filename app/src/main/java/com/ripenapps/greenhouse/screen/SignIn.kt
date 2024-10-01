package com.ripenapps.greenhouse.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySignInBinding
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.BackgroundVerficationAdmin
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import com.ripenapps.greenhouse.screen.seller.Rejected3Times
import com.ripenapps.greenhouse.screen.seller.VerificationRejectedByAdmin
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.LoginViewModel

@Suppress("DEPRECATION")
class SignIn : BaseActivity() {
    private lateinit var signInBinding: ActivitySignInBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loginData: LoginResponseModel.LoginData
    private var reason: String = ""
    private var isNumber = false
    private var isPass = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SignIn, R.color.status_bar) //status bar color function
        signInBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        checkNotificationPermission()
        initOnClickListner()
        initViewModel()
        validationObserver()

        signInBinding.editPassword.setTitle(getString(R.string.password))
        signInBinding.editPassword.setHint(getString(R.string.enter_password))
        signInBinding.editPassword.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
        signInBinding.editPassword.setWidth()
        signInBinding.editPassword.setTypeFace(this@SignIn, R.font.euclid_circular_medium)
        signInBinding.editPassword.eyeIcon(true)
        signInBinding.editPassword.setValidationValue(isPass)
        signInBinding.editPassword.setImeAction("done")

        signInBinding.editMobileNumSignin.setTitle(getString(R.string.mobile_number))
        signInBinding.editMobileNumSignin.setHint(getString(R.string.enter_mobil))
        signInBinding.editMobileNumSignin.setInputType(
            false, InputType.TYPE_CLASS_NUMBER
        )
        signInBinding.editMobileNumSignin.setWidth()
        signInBinding.editMobileNumSignin.setTypeFace(this@SignIn, R.font.euclid_circular_medium)
        signInBinding.editMobileNumSignin.setValidationValue(isNumber)
        signInBinding.editMobileNumSignin.setImeAction("next")
        signInBinding.editMobileNumSignin.textDirection(View.TEXT_DIRECTION_LTR)
    }
    private fun initViewModel() {
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        signInBinding.lifecycleOwner = this

        loginViewModel.getLoginData().observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        val data = Gson().fromJson(
                            AESHelper.decrypt(SECRET_KEY, it.data), LoginResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Preferences.setBooleanPreference(this@SignIn, "isVerified", false)
                                Preferences.setStringPreference(
                                    this@SignIn, "token", data.data?.token
                                )

                                loginData = data?.data ?: LoginResponseModel.LoginData()
                                //saving user details in local storage.
                                Preferences.setStringPreference(
                                    this@SignIn, "user_details", Gson().toJson(loginData)
                                )

                                if (loginData.userDetails?.userType?.getOrNull(0) == "seller") {
                                    Preferences.setBooleanPreference(this@SignIn, "isSeller", true)
                                    Preferences.setBooleanPreference(this@SignIn, "signin", false)
                                    val toHome = Intent(this, HomeSeller::class.java)
                                    startActivity(toHome)
                                    finish()
                                } else {
                                    Preferences.setBooleanPreference(this@SignIn, "isBidder", true)
                                    Preferences.setBooleanPreference(this@SignIn, "signin", false)
                                    val toHome = Intent(this, Home::class.java)
                                    startActivity(toHome)
                                    finish()
                                }
                            }

                            424 -> {
                                val toAdminVerify =
                                    Intent(this, BackgroundVerficationAdmin::class.java)
                                startActivity(toAdminVerify)
                            }

                            423 -> {
                                loginData = data.data ?: LoginResponseModel.LoginData()
                                reason = loginData.rejectedReason.toString()
                                val toRejected =
                                    Intent(this, VerificationRejectedByAdmin::class.java)
                                toRejected.putExtra("REJECTED_REASON", reason)
                                toRejected.putExtra(
                                    "NUMBER", signInBinding.editMobileNumSignin.getText()
                                )
                                startActivity(toRejected)
                            }

                            425 -> {
                                val rejected3Times = Intent(this@SignIn, Rejected3Times::class.java)
                                startActivity(rejected3Times)
                                rejected3Times.putExtra("type", "bidder")
                            }

                            402 -> {
                                Toast.makeText(this@SignIn, data.message, Toast.LENGTH_SHORT).show()
                            }


                            else -> {
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                        ProcessDialog.dismissDialog()
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@SignIn, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()

                Log.d("error", e.message.toString())
            }
        }
    }

    private fun validationObserver() {
        signInBinding.apply {
            editMobileNumSignin.getValidatedValue().observe(this@SignIn) { validated ->
                isNumber = validated
                checkValidation()
            }
            editPassword.getValidatedValue().observe(this@SignIn) { validated ->
                isPass = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (isNumber && isPass) {
            signInBinding.btnSignIn.isEnabled = true
            changeButtonColor()
        } else {
            signInBinding.btnSignIn.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        signInBinding.apply {
            btnSignIn.setBackgroundColor(
                ContextCompat.getColor(
                    this@SignIn, R.color.greenhousetheme
                )
            )
        }
    }

    //calling of onClickListner Functions
    private fun initOnClickListner() {
        signInBinding.editMobileNumSignin.initListener("mobile")
        signInBinding.editPassword.initListener("other")

        signInBinding.idForgotPassword.setOnClickListener {
            val toForgotPassword = Intent(this, ForgotPassword::class.java)
            startActivity(toForgotPassword)
        }

        signInBinding.idRegisterHere.setOnClickListener {
            val toSignUp = Intent(this, SignUp::class.java)
            startActivity(toSignUp)
        }
        signInBinding.btnSignIn.setOnClickListener {
            signInBinding.btnSignIn.isEnabled = false
            Handler().postDelayed({
                signInBinding.btnSignIn.isEnabled = true
            }, 2000)
            loginViewModel.request.mobile = signInBinding.editMobileNumSignin.getText()
            loginViewModel.request.password = signInBinding.editPassword.getText()
            loginViewModel.request.countryCode = CC
            loginViewModel.request.deviceToken =
                Preferences.getStringPreference(this@SignIn, "deviceToken")
            Log.d(
                "TAG",
                "initOnClickListner: ${Preferences.getStringPreference(this@SignIn, "deviceToken")}"
            )
            loginViewModel.getLoginRequest()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this@SignIn, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    CommonUtils.showSettingDialog(this@SignIn, askedPermission = "Notification")
                }

                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("TAG", "checkNotificationPermission: ")
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast("Notification permission granted")
            } else {
                CommonUtils.showSettingDialog(this@SignIn, askedPermission = "Notification")
            }
        }
}

