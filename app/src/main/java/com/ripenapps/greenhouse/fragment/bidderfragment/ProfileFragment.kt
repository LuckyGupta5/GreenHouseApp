package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentProfileBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.SwitchRejected
import com.ripenapps.greenhouse.fragment.SwitchRejected3Times
import com.ripenapps.greenhouse.fragment.SwitchVerification
import com.ripenapps.greenhouse.fragment.sellerfragemnt.DeleteAccount
import com.ripenapps.greenhouse.model.register_user.response.CheckProfileResponse
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.PrivacyPolicy
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.TermCondition
import com.ripenapps.greenhouse.screen.bidder.AboutUsProfile
import com.ripenapps.greenhouse.screen.bidder.ChangePassword
import com.ripenapps.greenhouse.screen.bidder.FAQs
import com.ripenapps.greenhouse.screen.bidder.FavouriteSellers
import com.ripenapps.greenhouse.screen.bidder.HelpSupport
import com.ripenapps.greenhouse.screen.bidder.MyOrders
import com.ripenapps.greenhouse.screen.bidder.MyProfile
import com.ripenapps.greenhouse.screen.bidder.MyWishlistProfile
import com.ripenapps.greenhouse.screen.seller.BackgroundVerficationAdmin
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import com.ripenapps.greenhouse.screen.seller.Rejected3Times
import com.ripenapps.greenhouse.screen.seller.SellerCompleteProfile2
import com.ripenapps.greenhouse.screen.seller.SellerMyAddress
import com.ripenapps.greenhouse.screen.seller.SellerWallet
import com.ripenapps.greenhouse.screen.seller.VerificationRejectedByAdmin
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.capitalizeName
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.CheckProfileViewModel

class ProfileFragment : Fragment() {
    private lateinit var profileFragmentBinding: FragmentProfileBinding
    private lateinit var checkProfileViewModel: CheckProfileViewModel
    private var loginData: LoginResponseModel.LoginData? = null
    private var switchVerification: SwitchVerification? = null
    private var switchRejected: SwitchRejected? = null
    private var rejected3Times: SwitchRejected3Times? = null
    var accountBlocked: AccountBlocked? = null
    private var accountDeleted: DeleteAccount? = null
    private var logout: Logout? = null
    private lateinit var mContext: Context
    private var changeLanguageSheet: ChangeLanguageMyProfile? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        changeStatusBarColor(this@ProfileFragment, R.color.status_bar)
        profileFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            profileFragmentBinding.arrow.rotation = 180f
            profileFragmentBinding.arrow1.rotation = 180f
            profileFragmentBinding.arrow2.rotation = 180f
            profileFragmentBinding.arrow3.rotation = 180f
            profileFragmentBinding.arrow4.rotation = 180f
            profileFragmentBinding.arrow5.rotation = 180f
            profileFragmentBinding.arrow6.rotation = 180f
            profileFragmentBinding.arrow7.rotation = 180f
            profileFragmentBinding.arrow8.rotation = 180f
            profileFragmentBinding.arrow9.rotation = 180f
            profileFragmentBinding.arrow10.rotation = 180f
            profileFragmentBinding.arrow11.rotation = 180f
        }

        initListener()
        initViewModel()
        initStartActivityInstance()
        initData()
        return profileFragmentBinding.root
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    Log.d("TAG", "$status")
                    if (status?.isNotEmpty() == true) {
                        initData()
                    }
                }
            }
    }


    @SuppressLint("SetTextI18n")
    private fun initData() {
        loginData = Gson().fromJson(
            Preferences.getStringPreference(mContext, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        profileFragmentBinding.profileUserName.text =
            capitalizeName(loginData?.userDetails?.name.toString())
        Log.d("TAG", "beforeReverse: ${CC + loginData?.userDetails?.mobile}")
        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            profileFragmentBinding.profileNumber.textDirection = View.TEXT_DIRECTION_LTR
        }
        profileFragmentBinding.profileNumber.text = CC + " " + loginData?.userDetails?.mobile

        profileFragmentBinding.userNameInitials.text = getInitials(
            loginData?.userDetails?.name.toString()
        )
    }

    private fun initViewModel() {
        checkProfileViewModel = ViewModelProvider(this)[CheckProfileViewModel::class.java]
        profileFragmentBinding.lifecycleOwner = this

        checkProfileViewModel.getCheckProfileData().observe(viewLifecycleOwner) {

            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        profileFragmentBinding.switchToSeller.isEnabled = true
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            CheckProfileResponse::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                                Preferences.setBooleanPreference(mContext, "isBidder", false)
                                Preferences.setBooleanPreference(mContext, "isSeller", true)
                                Preferences.setStringPreference(
                                    mContext, "token", data.data.token
                                )
                                val toHome = Intent(mContext, HomeSeller::class.java)
                                startActivity(toHome)
                                activity?.finish()
                            }

                            424 -> {
                                ProcessDialog.dismissDialog()
                                if (switchVerification?.isAdded != true) {
                                    val adminVerify = Intent(
                                        mContext, BackgroundVerficationAdmin::class.java
                                    )
                                    adminVerify.putExtra("type", "seller")
                                    startActivity(adminVerify)
                                }
                            }

                            423 -> {
                                ProcessDialog.dismissDialog()
                                if (switchRejected?.isAdded != true) {
                                    val adminVerify = Intent(
                                        mContext, VerificationRejectedByAdmin::class.java
                                    )
                                    adminVerify.putExtra("type", "bidder")
                                    adminVerify.putExtra("reason", data.data.rejectedReason ?: "")
                                    startActivity(adminVerify)
                                }
                            }

                            425 -> {
                                ProcessDialog.dismissDialog()
                                if (rejected3Times?.isAdded != true) {
                                    val rejected3Times =
                                        Intent(mContext, Rejected3Times::class.java)
                                    rejected3Times.putExtra("type", "seller")
                                    startActivity(rejected3Times)
                                }
                            }

                            426 -> {
                                ProcessDialog.dismissDialog()
                                val toCompleteProfile =
                                    Intent(mContext, SellerCompleteProfile2::class.java)
                                toCompleteProfile.putExtra("fromBidder", 1)
                                startActivity(toCompleteProfile)
                            }

                            402 -> {
                                ProcessDialog.dismissDialog()
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message)
                                    accountBlocked?.show(parentFragmentManager, accountBlocked?.tag)
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(mContext, "token")
                                Preferences.removePreference(mContext, "user_details")
                                Preferences.removePreference(mContext, "isVerified")
                                Preferences.removePreference(mContext, "roomId")
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        context?.let { it1 -> ProcessDialog.startDialog(it1) }
                    }

                    Status.ERROR -> {
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {

        profileFragmentBinding.deleteAccountLayout.setOnClickListener {
            if (accountDeleted?.isAdded != true) {
                accountDeleted = DeleteAccount()
                accountDeleted?.show(parentFragmentManager, accountDeleted?.tag)
            }
        }
        profileFragmentBinding.btnMyorder.setOnClickListener {
            val tpMyOrder = Intent(mContext, MyOrders::class.java)
            startActivity(tpMyOrder)
        }
        profileFragmentBinding.switchToSeller.setOnClickListener {
            Log.d(
                "TAG", "initListener: ${Preferences.getStringPreference(mContext, "bidderMobile")}"
            )
            profileFragmentBinding.switchToSeller.isEnabled = false
            checkProfileViewModel.request.mobile = loginData?.userDetails?.mobile
            checkProfileViewModel.request.countryCode = CC
            checkProfileViewModel.getCheckProfileRequest()
        }
        profileFragmentBinding.myProfileLayout.setOnClickListener {
            val toMyProfile = Intent(mContext, MyProfile::class.java)
            getContent.launch(Intent(toMyProfile))
        }
        profileFragmentBinding.btnWallet.setOnClickListener {
            val toWallet = Intent(mContext, SellerWallet::class.java)
            toWallet.putExtra("forBidderWallet", "1")
            startActivity(toWallet)
        }

        profileFragmentBinding.aboutGreenHouseLayout.setOnClickListener {
            val toAboutUs = Intent(mContext, AboutUsProfile::class.java)
            startActivity(toAboutUs)
        }

        profileFragmentBinding.changePasswordLayout.setOnClickListener {
            val toChangePassword = Intent(mContext, ChangePassword::class.java)
            startActivity(toChangePassword)
        }
        profileFragmentBinding.myAddressLayout.setOnClickListener {
            val toMyAddresses = Intent(mContext, SellerMyAddress::class.java)
            toMyAddresses.putExtra("fromBidder", 1)
            startActivity(toMyAddresses)
        }
        profileFragmentBinding.changeLanguageLayout.setOnClickListener {
            if (changeLanguageSheet?.isAdded != true) {
                changeLanguageSheet = ChangeLanguageMyProfile("fromBidder")
                changeLanguageSheet?.show(parentFragmentManager, changeLanguageSheet?.tag)
            }
        }

        profileFragmentBinding.privacyPolicyLayout.setOnClickListener {
            val toPrivacyPolicy = Intent(mContext, PrivacyPolicy::class.java)
            toPrivacyPolicy.putExtra("fromBidder", "1")
            startActivity(toPrivacyPolicy)
        }
        profileFragmentBinding.termsconditionLayout.setOnClickListener {
            val toTermsCondition = Intent(mContext, TermCondition::class.java)
            toTermsCondition.putExtra("fromBidder", "1")
            startActivity(toTermsCondition)
        }
        profileFragmentBinding.logoutButton.setOnClickListener {
            if (logout?.isAdded != true) {
                logout = Logout()
                logout?.show(parentFragmentManager, logout?.tag)
            }
        }
        profileFragmentBinding.helpSupportLayout.setOnClickListener {
            val toHelpSupport = Intent(mContext, HelpSupport::class.java)
            startActivity(toHelpSupport)
        }
        profileFragmentBinding.FAQLayout.setOnClickListener {
            val toFaqPage = Intent(mContext, FAQs::class.java)
            toFaqPage.putExtra("fromBidder", "1")
            startActivity(toFaqPage)
        }
        profileFragmentBinding.btnFav.setOnClickListener {
            val toFav = Intent(mContext, FavouriteSellers::class.java)
            startActivity(toFav)
        }
        profileFragmentBinding.myWishlistLayout.setOnClickListener {
            val toMyWishlist = Intent(mContext, MyWishlistProfile::class.java)
            startActivity(toMyWishlist)
        }
    }
}