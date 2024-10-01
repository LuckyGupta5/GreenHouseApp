package com.ripenapps.greenhouse.screen

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityResetPasswordBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Companion.Companion.FORGOT_NUMBER
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.ResetPasswordViewModel

class ResetPassword : BaseActivity() {
    private lateinit var resetPasswordBinding: ActivityResetPasswordBinding
    private lateinit var resetPasswordModel: ResetPasswordViewModel
    private var number: String? = null
    var accountBlocked: AccountBlocked? = null
    private var isPasswordValidated = false
    private var isConfirmPasswordValidated = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@ResetPassword, R.color.status_bar)

        resetPasswordBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        number = intent.getStringExtra(FORGOT_NUMBER)
        initViewModel()
        iniListeners()
        validationObserver()

        resetPasswordBinding.apply {
            password.setTitle(getString(R.string.password))
            password.setHint(getString(R.string.enter_password))
            password.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            password.setWidth()
            password.setTypeFace(
                this@ResetPassword, R.font.euclid_circular_regular
            )
            password.eyeIcon(true)
            password.setValidationValue(isPasswordValidated)


            confirmPassword.setTitle(getString(R.string.confirm_password))
            confirmPassword.setHint(getString(R.string.enter_confirm_password))
            confirmPassword.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            confirmPassword.setWidth()
            confirmPassword.setTypeFace(
                this@ResetPassword, R.font.euclid_circular_regular
            )
            confirmPassword.eyeIcon(true)
            confirmPassword.setValidationValue(isConfirmPasswordValidated)
        }
    }

    private fun validationObserver() {
        resetPasswordBinding.apply {
            password.getValidatedValue().observe(this@ResetPassword) { validated ->
                isPasswordValidated = validated
                checkValidation()
            }
            confirmPassword.getValidatedValue().observe(this@ResetPassword) { validated ->
                isConfirmPasswordValidated = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (isPasswordValidated && isConfirmPasswordValidated) {
            resetPasswordBinding.btnSaveConfirmPass.isEnabled = true
            changeButtonColor()
        } else {
            resetPasswordBinding.btnSaveConfirmPass.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        resetPasswordBinding.apply {
            btnSaveConfirmPass.setBackgroundColor(
                ContextCompat.getColor(
                    this@ResetPassword, R.color.greenhousetheme
                )
            )
        }
    }


    private fun iniListeners() {
        resetPasswordBinding.btnCloseResetPass.setOnClickListener {
            finish()
        }

        resetPasswordBinding.btnSaveConfirmPass.setOnClickListener {
            val pass = resetPasswordBinding.password.getText()
            val confirmPass = resetPasswordBinding.confirmPassword.getText()
            if (pass != confirmPass) {
                resetPasswordBinding.confirmPassword.setError(getString(R.string.password_does_not_match))
            } else {
                resetPasswordModel.request.newPassword = resetPasswordBinding.password.getText()
                resetPasswordModel.request.confirm_newPassword =
                    resetPasswordBinding.confirmPassword.getText()
                resetPasswordModel.request.countryCode = CC
                resetPasswordModel.request.mobile = number
                resetPasswordModel.getResetPasswordRequest()
            }
        }
        resetPasswordBinding.apply {
            password.initListener("password")
            confirmPassword.initListener("password", password.getText())
        }
    }

    private fun initViewModel() {
        resetPasswordModel = ViewModelProvider(this)[ResetPasswordViewModel::class.java]
        resetPasswordBinding.lifecycleOwner = this

        resetPasswordModel.getResetPasswordData().observe(this) {
            when (it.status) {

                Status.SUCCESS -> {
                    ProcessDialog.dismissDialog()
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        com.ripenapps.greenhouse.model.register_user.response.ReseTPasswordResponse::class.java
                    )
                    when (data.status) {

                        200 -> {

                            Toast.makeText(
                                this@ResetPassword,
                                getString(R.string.password_changed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            val toSignIn = Intent(this, SignIn::class.java)
                            startActivity(toSignIn)
                            finish()
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(supportFragmentManager, accountBlocked?.tag)
                            }
                        }

                        403,401 -> {
                            val signin = Intent(this@ResetPassword, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@ResetPassword, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}