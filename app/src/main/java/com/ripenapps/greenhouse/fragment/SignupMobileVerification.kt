package com.ripenapps.greenhouse.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSignupMobileVerificationBinding
import com.ripenapps.greenhouse.model.register_user.response.SendOtpResponseModel
import com.ripenapps.greenhouse.screen.CompleteProfile
import com.ripenapps.greenhouse.screen.seller.SellerCompleteProfile
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_TYPE
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.SendOtpViewModel
import com.ripenapps.greenhouse.view_models.VerifyOtpViewModel
import java.util.Timer
import java.util.TimerTask

class SignupMobileVerification(
    val text: StringBuilder, val user: Int?, var user_type: String?, var numberUser: String?
) : BottomSheetDialogFragment() {
    private lateinit var mContext: Context
    private lateinit var sendOtpViewModel: SendOtpViewModel
    private lateinit var verifyOtpViewModel: VerifyOtpViewModel
    private lateinit var signupMobileVerificationBinding: FragmentSignupMobileVerificationBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        signupMobileVerificationBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_signup_mobile_verification, container, false
        )
        // Inflate the layout for this fragment
        signupMobileVerificationBinding.idMobileNumberSignup.text = text.toString()
        signupMobileVerificationBinding.apply {
            idMobileNumberSignup.textDirection = View.TEXT_DIRECTION_LTR
        }

        startResendTimer()
        initListener()
        initViewModel()
        initResendOtp()

        return signupMobileVerificationBinding.root
    }

    private fun initResendOtp() {
        sendOtpViewModel =
            ViewModelProvider(this@SignupMobileVerification)[SendOtpViewModel::class.java]
        signupMobileVerificationBinding.lifecycleOwner = this@SignupMobileVerification

        sendOtpViewModel.getOtpData().observe(viewLifecycleOwner) {
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
                            startResendTimer()
                        }

                        else -> {
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(mContext)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()

                }
            }
        }
    }


    private fun initViewModel() {
        verifyOtpViewModel = ViewModelProvider(this)[VerifyOtpViewModel::class.java]
        signupMobileVerificationBinding.viewModel = verifyOtpViewModel
        signupMobileVerificationBinding.lifecycleOwner = this
        signupMobileVerificationBinding.click = verifyOtpViewModel.ClickAction()
        verifyOtpViewModel.getVerifyOtpData().observe(this) {
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
                            if (user == 1) {
                                val toCompleteProfile =
                                    Intent(mContext, CompleteProfile::class.java)
                                toCompleteProfile.putExtra(USER_TYPE, user_type)
                                toCompleteProfile.putExtra("NUMBER1", numberUser)
                                startActivity(toCompleteProfile)
                                dismiss()
                            } else {
                                val toSellerComplete =
                                    Intent(mContext, SellerCompleteProfile::class.java)
                                toSellerComplete.putExtra("NUMBER", numberUser)
                                startActivity(toSellerComplete)
                                dismiss()
                            }
                        }

                        else -> {
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(mContext)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    ProcessDialog.dismissDialog()
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListener() {
        signupMobileVerificationBinding.idMobileNumberSignup.setOnClickListener {
            text.clear()
            dismiss()
        }
        signupMobileVerificationBinding.resendCodeSignup.setOnClickListener {
            sendOtpViewModel.request.userType = user_type
            sendOtpViewModel.request.mobile = numberUser
            Log.d("TAG", "mobileNumber: ${numberUser},$user_type")
            sendOtpViewModel.request.countryCode = CC
            sendOtpViewModel.getSendOtpRequest()
        }


        signupMobileVerificationBinding.apply {
            btnVerifySignup.setOnClickListener {
                verifyOtpViewModel.request.mobile = numberUser
                verifyOtpViewModel.request.countryCode = CC
                verifyOtpViewModel.getVerifyOtpRequest()
            }
        }
    }

    private fun startResendTimer() {
        signupMobileVerificationBinding.resendCodeSignup.isEnabled = false

        var timeOutSeconds = 120 // timeout duration
        val timer = Timer()

        timer.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                timeOutSeconds--
                activity?.runOnUiThread {

                    if (timeOutSeconds > 0) {
                        signupMobileVerificationBinding.resendCodeSignup.text =
                            getString(R.string.resend_code) + " " + "$timeOutSeconds " + getString(
                                R.string.seconds
                            )
                        signupMobileVerificationBinding.resendCodeSignup.textSize = 13f
                        signupMobileVerificationBinding.resendCodeSignup.setTextColor(
                            ContextCompat.getColor(
                                mContext, R.color.black
                            )
                        )
                    } else {
                        // Enable the button and reset the text when the timer reaches 0
                        timer.cancel()
                        signupMobileVerificationBinding.resendCodeSignup.isEnabled = true
                        signupMobileVerificationBinding.resendCodeSignup.text =
                            getString(R.string.resend_code_green)
                        signupMobileVerificationBinding.resendCodeSignup.setTextColor(
                            ContextCompat.getColor(
                                mContext, R.color.greenhousetheme
                            )
                        )
                        signupMobileVerificationBinding.resendCodeSignup.textSize = 17f
                        signupMobileVerificationBinding.resendCodeSignup.elevation = 10f
                    }
                }
            }
        }, 0, 1000)
    }
}