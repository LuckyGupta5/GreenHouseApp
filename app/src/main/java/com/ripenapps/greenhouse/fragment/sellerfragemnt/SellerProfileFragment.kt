package com.ripenapps.greenhouse.fragment.sellerfragemnt

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
import com.ripenapps.greenhouse.databinding.FragmentSellerProfileBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.ChangeLanguageMyProfile
import com.ripenapps.greenhouse.fragment.bidderfragment.Logout
import com.ripenapps.greenhouse.model.profile.GetUserDetailResponseModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.switch_profile.SwitchProfileResponseModel
import com.ripenapps.greenhouse.screen.PrivacyPolicy
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.TermCondition
import com.ripenapps.greenhouse.screen.bidder.AboutUsProfile
import com.ripenapps.greenhouse.screen.bidder.ChangePassword
import com.ripenapps.greenhouse.screen.bidder.FAQs
import com.ripenapps.greenhouse.screen.bidder.HelpSupport
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.OngoingFeatureBidding
import com.ripenapps.greenhouse.screen.seller.SellerBusinessDetails
import com.ripenapps.greenhouse.screen.seller.SellerMyAddress
import com.ripenapps.greenhouse.screen.seller.SellerMyProfile
import com.ripenapps.greenhouse.screen.seller.SellerWallet
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.capitalizeName
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.formatToReadableNumber
import com.ripenapps.greenhouse.view_models.SwitchProfileViewModel
import com.ripenapps.greenhouse.view_models.UserDetailsViewModel

@SuppressLint("SetTextI18n", "DefaultLocale")
class SellerProfileFragment : Fragment() {
    private lateinit var sellerProfileBinding: FragmentSellerProfileBinding
    private lateinit var switchProfileViewModel: SwitchProfileViewModel
    private var loginData: LoginResponseModel.LoginData? = null
    private var accountBlocked: AccountBlocked? = null
    private var accountDeleted: DeleteAccount? = null
    private var logout: Logout? = null
    private var changeLanguage: ChangeLanguageMyProfile? = null
    private lateinit var mContext: Context
    private lateinit var getUserDetailsViewModel: UserDetailsViewModel
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        CommonUtils.changeStatusBarColor(this@SellerProfileFragment, R.color.status_bar)
        sellerProfileBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_seller_profile, container, false)

        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            sellerProfileBinding.arrow0.rotation = 180f
            sellerProfileBinding.arrow.rotation = 180f
            sellerProfileBinding.arrow1.rotation = 180f
            sellerProfileBinding.arrow2.rotation = 180f
            sellerProfileBinding.arrow3.rotation = 180f
            sellerProfileBinding.arrow4.rotation = 180f
            sellerProfileBinding.arrow5.rotation = 180f
            sellerProfileBinding.arrow6.rotation = 180f
            sellerProfileBinding.arrow7.rotation = 180f
            sellerProfileBinding.arrow8.rotation = 180f
            sellerProfileBinding.arrow9.rotation = 180f
            sellerProfileBinding.arrow10.rotation = 180f
            sellerProfileBinding.arrow11.rotation = 180f
        }

        initListeners()
        initViewModel2()
        userDeatilsViewModel()//user detail
        userDetailApi()
        initStartActivityInstance()
        initData()
        return sellerProfileBinding.root
    }

    @SuppressLint("SetTextI18n")

    private fun initData() {
        loginData = Gson().fromJson(
            Preferences.getStringPreference(mContext, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        sellerProfileBinding.profileUserName.text =
            capitalizeName(loginData?.userDetails?.name.toString())
        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            sellerProfileBinding.profileNumber.textDirection = View.TEXT_DIRECTION_LTR
            sellerProfileBinding.balanceAmount.textDirection = View.TEXT_DIRECTION_LTR
        }

        sellerProfileBinding.profileNumber.text = CC + " " + loginData?.userDetails?.mobile
        sellerProfileBinding.userNameInitials.text = CommonUtils.getInitials(
            loginData?.userDetails?.name.toString()
        )
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    Log.d("TAG", "$status")
                    if (status?.isNotEmpty() == true) {
                        initData()
                        userDetailApi()
                    }
                }
            }
    }

    private fun userDeatilsViewModel() {
        getUserDetailsViewModel =
            ViewModelProvider(this@SellerProfileFragment)[UserDetailsViewModel::class.java]
        sellerProfileBinding.lifecycleOwner = this@SellerProfileFragment
        getUserDetailsViewModel.getUserDetailsData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                sellerProfileBinding.apply {
                                    balanceAmount.text =
                                        getString(R.string.sar) + " " + data?.data?.userDetails?.amount?.formatToReadableNumber()
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
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
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun userDetailApi() {
        getUserDetailsViewModel.request.token = Preferences.getStringPreference(mContext, TOKEN)
        getUserDetailsViewModel.getUserDetailsRequest()
    }

    private fun initViewModel2() {
        switchProfileViewModel = ViewModelProvider(this)[SwitchProfileViewModel::class.java]
        sellerProfileBinding.lifecycleOwner = this

        switchProfileViewModel.getSwitchProfileData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {

                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        sellerProfileBinding.switchToBidder.isEnabled = true
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SwitchProfileResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                                Preferences.setBooleanPreference(
                                    mContext, "isSeller", false
                                )
                                Preferences.setBooleanPreference(mContext, "isBidder", true)
                                Preferences.setStringPreference(
                                    mContext, "token", data.data?.token
                                )
                                val toHome = Intent(mContext, Home::class.java)
                                startActivity(toHome)
                                activity?.finish()
                            }

                            402 -> {
                                ProcessDialog.dismissDialog()
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked =
                                        data.message?.let { it1 -> AccountBlocked(it1) }
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
                        ProcessDialog.startDialog(mContext)
                    }

                    Status.ERROR -> {
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                        ProcessDialog.dismissDialog()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        sellerProfileBinding.deleteAccountLayout.setOnClickListener {
            if (accountDeleted?.isAdded != true) {
                accountDeleted = DeleteAccount()
                accountDeleted?.show(parentFragmentManager, accountDeleted?.tag)
            }
        }
        sellerProfileBinding.myProfileLayout.setOnClickListener {
            val toSellerMyProfile = Intent(mContext, SellerMyProfile::class.java)
            getContent.launch(Intent(toSellerMyProfile))
        }
        sellerProfileBinding.btnWallet.setOnClickListener {
            val toWalletSeller = Intent(mContext, SellerWallet::class.java)
            getContent.launch(toWalletSeller)
        }
//        sellerProfileBinding.bankAccount.setOnClickListener {
//            val toAddBankAccount = Intent(mContext, AddBankAccountSeller::class.java)
//            startActivity(toAddBankAccount)
//        }
        sellerProfileBinding.businessDetailsLayout.setOnClickListener {
            val toSellerBusinessDetails = Intent(mContext, SellerBusinessDetails::class.java)
            startActivity(toSellerBusinessDetails)
        }
        sellerProfileBinding.aboutGreenHouseLayout.setOnClickListener {
            val toAboutUs = Intent(mContext, AboutUsProfile::class.java)
            startActivity(toAboutUs)
        }
        sellerProfileBinding.changePasswordLayout.setOnClickListener {
            val toChangePass = Intent(mContext, ChangePassword::class.java)
            startActivity(toChangePass)
        }
        sellerProfileBinding.myAddressLayout.setOnClickListener {
            val toSellerAddress = Intent(mContext, SellerMyAddress::class.java)
            startActivity(toSellerAddress)
        }
        sellerProfileBinding.changeLanguageLayout.setOnClickListener {
            if (changeLanguage?.isAdded != true) {
                changeLanguage = ChangeLanguageMyProfile("fromSeller")
                changeLanguage?.show(parentFragmentManager, changeLanguage?.tag)
            }
        }
        sellerProfileBinding.FAQLayout.setOnClickListener {
            val toFaq = Intent(mContext, FAQs::class.java)
            startActivity(toFaq)
        }
        sellerProfileBinding.helpSupportLayout.setOnClickListener {
            val toHelpSupport = Intent(mContext, HelpSupport::class.java)
            startActivity(toHelpSupport)
        }
        sellerProfileBinding.privacyPolicyLayout.setOnClickListener {
            val toPrivacyPolicy = Intent(mContext, PrivacyPolicy::class.java)
            startActivity(toPrivacyPolicy)
        }
        sellerProfileBinding.termsconditionLayout.setOnClickListener {
            val toTerm = Intent(mContext, TermCondition::class.java)
            startActivity(toTerm)
        }
        sellerProfileBinding.logoutButton.setOnClickListener {
            if (logout?.isAdded != true) {
                logout = Logout()
                logout?.show(parentFragmentManager, logout?.tag)
            }
        }
        sellerProfileBinding.featuredBiddingLayout.setOnClickListener {
            val intent = Intent(mContext, OngoingFeatureBidding::class.java)
            startActivity(intent)
        }
        sellerProfileBinding.switchToBidder.setOnClickListener {
            sellerProfileBinding.switchToBidder.isEnabled = false
            switchProfileViewModel.request.token = Preferences.getStringPreference(mContext, TOKEN)
            switchProfileViewModel.request.userType = "bidder"
            switchProfileViewModel.getSwitchProfileRequest("")
        }
    }
}