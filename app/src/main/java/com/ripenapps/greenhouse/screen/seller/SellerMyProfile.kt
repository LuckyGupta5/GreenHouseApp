package com.ripenapps.greenhouse.screen.seller

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
import com.ripenapps.greenhouse.databinding.ActivitySellerMyProfileBinding
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

class SellerMyProfile : BaseActivity() {
    private lateinit var sellerMyProfileBinding: ActivitySellerMyProfileBinding
    private lateinit var getUserDetailsViewModel: UserDetailsViewModel
    private var editProfileViewModel: EditProfileViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var loginData: LoginResponseModel.LoginData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@SellerMyProfile, R.color.status_bar)
        super.onCreate(savedInstanceState)
        sellerMyProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_my_profile)
        if (Preferences.getStringPreference(this@SellerMyProfile, "language") == "ar") {
            sellerMyProfileBinding.btnBack.rotation = 180f
        }

        initListener()
        initViewModel()
        initEditProfileViewModel()
        sellerMyProfileBinding.apply {
            selleUserId.setTitle("Username")
            selleUserId.setInputType(false, InputType.TYPE_CLASS_TEXT)
            selleUserId.setWidth()
            selleUserId.setTypeFace(
                this@SellerMyProfile, R.font.euclid_circular_medium
            )
            selleUserId.setImeAction("next")
            selleUserId.setTextColor(R.color.hint_color)
            selleUserId.isEnable(false)

            sellerUserNameProfile.setTitle(resources.getString(R.string.full_name))
            sellerUserNameProfile.setHint(resources.getString(R.string.enter_full_name))
            sellerUserNameProfile.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            sellerUserNameProfile.setWidth()
            sellerUserNameProfile.setMaxLines(2)
            sellerUserNameProfile.setMaxLength(30)
            sellerUserNameProfile.setTypeFace(
                this@SellerMyProfile, R.font.euclid_circular_regular
            )
            sellerUserNameProfile.setImeAction("next")

            sellerEditMobileNumSignin.setTitle(resources.getString(R.string.mobile_number))
            sellerEditMobileNumSignin.setHint(resources.getString(R.string.please_enter_a_mobile_number))
            sellerEditMobileNumSignin.setInputType(
                false, InputType.TYPE_CLASS_NUMBER
            )
            sellerEditMobileNumSignin.setWidth()
            sellerEditMobileNumSignin.setTypeFace(
                this@SellerMyProfile, R.font.euclid_circular_medium
            )
            sellerEditMobileNumSignin.setImeAction("next")
            sellerEditMobileNumSignin.isEnable(false)
            sellerEditMobileNumSignin.setTextColor(R.color.hint_color)
            sellerEditMobileNumSignin.textDirection(View.TEXT_DIRECTION_LTR)
            sellerUserMailAddress.setTitle(resources.getString(R.string.email))
            sellerUserMailAddress.setHint(resources.getString(R.string.email_address_optional))
            sellerUserMailAddress.setInputType(
                false, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            )
            sellerUserMailAddress.setWidth()
            sellerUserMailAddress.setTypeFace(
                this@SellerMyProfile, R.font.euclid_circular_regular
            )
            sellerUserMailAddress.setImeAction("next")
        }
        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@SellerMyProfile, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        getUserDetailsViewModel.request.token =
            Preferences.getStringPreference(this@SellerMyProfile, TOKEN)
        getUserDetailsViewModel.getUserDetailsRequest()
    }

    private fun initEditProfileViewModel() {
        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        sellerMyProfileBinding.lifecycleOwner = this

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
                                    sellerMyProfileBinding.sellerUserMailAddress.getText().trim()
                                loginData?.userDetails?.name =
                                    sellerMyProfileBinding.sellerUserNameProfile.getText().trim()
                                loginData?.userDetails?.bio =
                                    sellerMyProfileBinding.bioEditTextField.text.toString().trim()
                                Preferences.setStringPreference(
                                    this@SellerMyProfile, "user_details", Gson().toJson(loginData)
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
                                Preferences.removePreference(this@SellerMyProfile, "token")
                                Preferences.removePreference(this@SellerMyProfile, "user_details")
                                Preferences.removePreference(this@SellerMyProfile, "isVerified")
                                Preferences.removePreference(this@SellerMyProfile, "roomId")
                                val signIn = Intent(this@SellerMyProfile, SignIn::class.java)
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
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@SellerMyProfile, it.message, Toast.LENGTH_SHORT).show()
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
            ViewModelProvider(this@SellerMyProfile)[UserDetailsViewModel::class.java]
        sellerMyProfileBinding.lifecycleOwner = this@SellerMyProfile
        getUserDetailsViewModel.getUserDetailsData().observe(this@SellerMyProfile) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        sellerMyProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(false)
                        sellerMyProfileBinding.scrollView.setVisibility(true)
                        sellerMyProfileBinding.btnSaveMyProfile.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                sellerMyProfileBinding.apply {
                                    selleUserId.setText(data.data?.userDetails?.userName.toString().trim())
                                    sellerUserNameProfile.setText(data.data?.userDetails?.name.toString())
                                    sellerEditMobileNumSignin.setText(data.data?.userDetails?.mobile.toString())
                                    sellerUserMailAddress.setText(data.data?.userDetails?.email.toString().trim())
                                    bioEditTextField.setText(data.data?.userDetails?.bio?.trim() ?: "")
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
                                Preferences.removePreference(this@SellerMyProfile, "token")
                                Preferences.removePreference(this@SellerMyProfile, "user_details")
                                Preferences.removePreference(this@SellerMyProfile, "isVerified")
                                Preferences.removePreference(this@SellerMyProfile, "roomId")
                                val signIn = Intent(this@SellerMyProfile, SignIn::class.java)
                                startActivity(signIn)
                                finishAffinity()
                            }
                            else -> {
                                sellerMyProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(
                                    false
                                )
                                sellerMyProfileBinding.scrollView.setVisibility(true)
                                Toast.makeText(this@SellerMyProfile, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        sellerMyProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(
                            true
                        )
                        sellerMyProfileBinding.scrollView.setVisibility(false)
                        sellerMyProfileBinding.btnSaveMyProfile.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        sellerMyProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(
                            true
                        )
                        sellerMyProfileBinding.scrollView.setVisibility(false)
                        sellerMyProfileBinding.btnSaveMyProfile.setVisibility(false)
                        Toast.makeText(this@SellerMyProfile, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                sellerMyProfileBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                sellerMyProfileBinding.scrollView.setVisibility(false)
                sellerMyProfileBinding.btnSaveMyProfile.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {

        sellerMyProfileBinding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            btnSaveMyProfile.setOnClickListener {
                sellerMyProfileBinding.apply {
                    editProfileApi()
                }
            }
        }
    }

    private fun editProfileApi() {
        editProfileViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerMyProfile, TOKEN)
        editProfileViewModel?.request?.name =
            sellerMyProfileBinding.sellerUserNameProfile.getText().trim()
        editProfileViewModel?.request?.email =
            sellerMyProfileBinding.sellerUserMailAddress.getText().trim()
        editProfileViewModel?.request?.bio =
            sellerMyProfileBinding.bioEditTextField.text.toString().trim()
        editProfileViewModel?.request?.type = "myProfile"
        editProfileViewModel?.request?.userType = "seller"
        editProfileViewModel?.getEditProfileRequest()
    }
}