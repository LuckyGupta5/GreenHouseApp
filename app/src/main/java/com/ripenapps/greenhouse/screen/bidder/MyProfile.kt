package com.ripenapps.greenhouse.screen.bidder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityMyProfileBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.profile.GetUserDetailResponseModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.seller.response.EditProfileResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.EditProfileViewModel
import com.ripenapps.greenhouse.view_models.UserDetailsViewModel


class MyProfile : BaseActivity() {
    private lateinit var myProfileBinding: ActivityMyProfileBinding
    private lateinit var getUserDetailsViewModel: UserDetailsViewModel
    private var editProfileViewModel: EditProfileViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var loginData: LoginResponseModel.LoginData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@MyProfile, R.color.status_bar)
        myProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile)

        if (Preferences.getStringPreference(this@MyProfile, "language").equals("ar")) {
            myProfileBinding.btnBack.rotation = 180f
        }

        initListener()
        initViewModel()
        initEditProfileViewModel()
        myProfileBinding.apply {
            userNameId.setTitle("Username")
            userNameId.setInputType(false, InputType.TYPE_CLASS_TEXT)
            userNameId.setWidth()
            userNameId.setTypeFace(
                this@MyProfile, R.font.euclid_circular_medium
            )
            userNameId.setImeAction("next")
            userNameId.setTextColor(R.color.hint_color)
            userNameId.isEnable(false)

            userNameMyProfile.setTitle(getString(R.string.full_name))
            userNameMyProfile.setHint(getString(R.string.enter_full_name))
            userNameMyProfile.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            userNameMyProfile.setWidth()
            userNameMyProfile.setTypeFace(
                this@MyProfile, R.font.euclid_circular_regular
            )
            userNameMyProfile.setImeAction("next")

            editMobileNumSignin.setTitle(getString(R.string.mobile_number))
            editMobileNumSignin.setHint(getString(R.string.enter_mobil))
            editMobileNumSignin.setInputType(
                false, InputType.TYPE_CLASS_NUMBER
            )
            editMobileNumSignin.setWidth()
            editMobileNumSignin.setTypeFace(this@MyProfile, R.font.euclid_circular_medium)
            editMobileNumSignin.setImeAction("next")
            editMobileNumSignin.isEnable(false)
            editMobileNumSignin.setTextColor(R.color.hint_color)
            editMobileNumSignin.isEnable(false)
            editMobileNumSignin.textDirection(View.TEXT_DIRECTION_LTR)

            userMailAddress.setTitle(getString(R.string.email))
            userMailAddress.setHint(getString(R.string.enter_email_address_optional))
            userMailAddress.setInputType(
                false, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            )
            userMailAddress.setWidth()
            userMailAddress.setTypeFace(
                this@MyProfile, R.font.euclid_circular_regular
            )
            userMailAddress.setImeAction("next")

            taxResgistrationNumber.setTitle(getString(R.string.tax_registration_number_optional))
            taxResgistrationNumber.setHint(getString(R.string.tax_registration_number))
            taxResgistrationNumber.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            taxResgistrationNumber.setWidth()
            taxResgistrationNumber.setTypeFace(
                this@MyProfile, R.font.euclid_circular_regular
            )
            taxResgistrationNumber.setImeAction("done")

        }
        initEditProfileViewModel()
        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@MyProfile, "user_details"),
            LoginResponseModel.LoginData::class.java
        )
        getUserDetailApi()
    }

    private fun getUserDetailApi() {
        getUserDetailsViewModel.request.token =
            Preferences.getStringPreference(this@MyProfile, TOKEN)
        getUserDetailsViewModel.getUserDetailsRequest()
    }

    private fun initEditProfileViewModel() {
        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        myProfileBinding.lifecycleOwner = this

        editProfileViewModel?.getEditProfileData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            EditProfileResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                loginData?.userDetails?.email =
                                    myProfileBinding.userMailAddress.getText().trim()
                                loginData?.userDetails?.name =
                                    myProfileBinding.userNameMyProfile.getText().trim()
                                loginData?.userDetails?.taxRegistrationNumber =
                                    myProfileBinding.taxResgistrationNumber.getText().trim()
                                Preferences.setStringPreference(
                                    this@MyProfile, "user_details", Gson().toJson(loginData)
                                )
                                showToast(data.message ?: "Profile Updated Successfully")
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("status", "update")
                                })
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

                            403, 401 -> {
                                Preferences.removePreference(this@MyProfile, "token")
                                Preferences.removePreference(this@MyProfile, "user_details")
                                Preferences.removePreference(this@MyProfile, "isVerified")
                                Preferences.removePreference(this@MyProfile, "roomId")
                                val signIn = Intent(this@MyProfile, SignIn::class.java)
                                startActivity(signIn)
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
                        Toast.makeText(this@MyProfile, it.message, Toast.LENGTH_SHORT).show()
                        ProcessDialog.dismissDialog()

                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }
    private fun initViewModel() {
        getUserDetailsViewModel =
            ViewModelProvider(this@MyProfile)[UserDetailsViewModel::class.java]
        myProfileBinding.lifecycleOwner = this@MyProfile
        getUserDetailsViewModel.getUserDetailsData().observe(this@MyProfile) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        myProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(false)
                        myProfileBinding.scrollView.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                myProfileBinding.apply {
                                    userNameId.setText(
                                        data.data?.userDetails?.userName.toString().trim()
                                    )
                                    userNameMyProfile.setText(
                                        data.data?.userDetails?.name.toString().trim()
                                    )
                                    editMobileNumSignin.setText(
                                        data.data?.userDetails?.mobile.toString().trim()
                                    )
                                    userMailAddress.setText(
                                        data.data?.userDetails?.email.toString().trim()
                                    )
                                    taxResgistrationNumber.setText(
                                        data.data?.userDetails?.taxRegistrationNumber.toString()
                                            .trim()
                                    )
                                }
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
                                Preferences.removePreference(this@MyProfile, "token")
                                Preferences.removePreference(this@MyProfile, "user_details")
                                Preferences.removePreference(this@MyProfile, "isVerified")
                                Preferences.removePreference(this@MyProfile, "roomId")
                                val signIn = Intent(this@MyProfile, SignIn::class.java)
                                startActivity(signIn)
                                finishAffinity()
                            }
                            else -> {
                                myProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(false)
                                myProfileBinding.scrollView.setVisibility(true)
                                Toast.makeText(this@MyProfile, data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        myProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                        myProfileBinding.scrollView.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(this@MyProfile, it.message, Toast.LENGTH_SHORT).show()
                        myProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                        myProfileBinding.scrollView.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        myProfileBinding.btnBack.setOnClickListener {
            finish()
        }
        myProfileBinding.btnSaveMyProfile.setOnClickListener {
            editProfileViewModel?.request?.token = Preferences.getStringPreference(this, TOKEN)
            editProfileViewModel?.request?.name =
                myProfileBinding.userNameMyProfile.getText().trim()
            editProfileViewModel?.request?.email = myProfileBinding.userMailAddress.getText().trim()
            editProfileViewModel?.request?.taxRegistrationNumber =
                myProfileBinding.taxResgistrationNumber.getText().trim()
            editProfileViewModel?.request?.type = "myProfile"
            editProfileViewModel?.request?.userType = "bidder"
            editProfileViewModel?.getEditProfileRequest()
        }
    }
}