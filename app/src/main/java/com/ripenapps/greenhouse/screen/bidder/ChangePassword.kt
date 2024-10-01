package com.ripenapps.greenhouse.screen.bidder

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
import com.ripenapps.greenhouse.databinding.ActivityChangePasswordBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.change_password.ChangePasswordResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.ChangePasswordViewModel

class ChangePassword : BaseActivity() {
    private lateinit var changePasswordBinding: ActivityChangePasswordBinding
    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    var accountBlocked: AccountBlocked? = null
    private var isOld = false
    private var isNew = false
    private var isConfirm = false
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@ChangePassword, R.color.status_bar)
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@ChangePassword, R.color.status_bar)
        changePasswordBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        if (Preferences.getStringPreference(this@ChangePassword, "language") == "ar") {
            changePasswordBinding.backChangePassword.rotation = 180f
        }

        initListener()
        initViewModel()
        validationObserver()

        changePasswordBinding.apply {
            editOldPassword.setTitle(getString(R.string.old_password))
            editOldPassword.setHint(getString(R.string.enter_old_password))
            editOldPassword.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            editOldPassword.setWidth()
            editOldPassword.setTypeFace(
                this@ChangePassword, R.font.euclid_circular_regular
            )
            editOldPassword.eyeIcon(true)
            editOldPassword.setImeAction("next")

            editNewPassword.setTitle(getString(R.string.new_pass))
            editNewPassword.setHint(getString(R.string.enter_new_password))
            editNewPassword.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            editNewPassword.setWidth()
            editNewPassword.setTypeFace(
                this@ChangePassword, R.font.euclid_circular_regular
            )
            editNewPassword.eyeIcon(true)
            editNewPassword.setImeAction("next")

            editConfirmNewPassword.eyeIcon(true)
            editConfirmNewPassword.setTitle(getString(R.string.confirm_pass))
            editConfirmNewPassword.setHint(getString(R.string.confirm_new_password))
            editConfirmNewPassword.setInputType(
                true, InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
            editConfirmNewPassword.setWidth()
            editConfirmNewPassword.setTypeFace(
                this@ChangePassword, R.font.euclid_circular_regular
            )
            editConfirmNewPassword.setImeAction("done")
        }
    }

    private fun validationObserver() {
        changePasswordBinding.apply {
            editOldPassword.getValidatedValue().observe(this@ChangePassword) { validated ->
                isOld = validated
                checkValidation()
            }
            editNewPassword.getValidatedValue().observe(this@ChangePassword) { validated ->
                isNew = validated
                checkValidation()
            }
            editConfirmNewPassword.getValidatedValue().observe(this@ChangePassword) { validated ->
                isConfirm = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (isOld && isNew && isConfirm) {
            changePasswordBinding.btnUpdate.isEnabled = true
            changeButtonColor()
        } else {
            changePasswordBinding.btnUpdate.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        changePasswordBinding.apply {
            btnUpdate.setBackgroundColor(
                ContextCompat.getColor(
                    this@ChangePassword, R.color.greenhousetheme
                )
            )
        }
    }

    private fun initViewModel() {
        changePasswordViewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]
        changePasswordBinding.lifecycleOwner = this
        changePasswordViewModel.getChangePasswordData().observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ChangePasswordResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Toast.makeText(
                                    this@ChangePassword, data.message, Toast.LENGTH_SHORT
                                ).show()
                                val toSigin = Intent(this@ChangePassword, SignIn::class.java)
                                startActivity(toSigin)
                                finish()
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }
                            403,401 -> {
                                val signin = Intent(this@ChangePassword, SignIn::class.java)
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
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@ChangePassword, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        changePasswordBinding.apply {
            editOldPassword.initListener("other")
            editNewPassword.initListener("password")
            editConfirmNewPassword.initListener("password", editNewPassword.getText())
        }
        changePasswordBinding.btnUpdate.setOnClickListener {
            val pass = changePasswordBinding.editNewPassword.getText()
            val confirmPass = changePasswordBinding.editConfirmNewPassword.getText()
            if (pass != confirmPass) {
                changePasswordBinding.editConfirmNewPassword.setError("Password does not match")
            } else {
                changePasswordViewModel.request.oldPassword =
                    changePasswordBinding.editOldPassword.getText()
                changePasswordViewModel.request.newPassword =
                    changePasswordBinding.editNewPassword.getText()
                changePasswordViewModel.request.confirm_newPassword =
                    changePasswordBinding.editConfirmNewPassword.getText()
                changePasswordViewModel.request.token =
                    Preferences.getStringPreference(this@ChangePassword, TOKEN)
                changePasswordViewModel.getChangePasswordRequest()
            }
        }

        changePasswordBinding.backChangePassword.setOnClickListener {
            finish()
        }
    }
}