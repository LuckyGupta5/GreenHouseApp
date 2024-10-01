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
import com.ripenapps.greenhouse.databinding.FragmentForgotPasswordVerificationBinding
import com.ripenapps.greenhouse.model.register_user.response.SendOtpResponseModel
import com.ripenapps.greenhouse.model.register_user.response.VerifyOtpResponseModel
import com.ripenapps.greenhouse.screen.ResetPassword
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Companion.Companion.FORGOT_NUMBER
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.SendOtpViewModel
import com.ripenapps.greenhouse.view_models.VerifyOtpViewModel
import java.util.Timer
import java.util.TimerTask

class ForgotPasswordVerification(
    val text: StringBuilder, private val numberMobile: String? = null
) : BottomSheetDialogFragment() {
    lateinit var mContext: Context
    private lateinit var sendOtpViewModel: SendOtpViewModel
    private lateinit var verifyOtpViewModel: VerifyOtpViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private lateinit var binding: FragmentForgotPasswordVerificationBinding

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_forgot_password_verification, container, false
        )

        // Inflate the layout for this fragment
        binding.idMobileNumber.text = text.toString()
        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            binding.idMobileNumber.textDirection = View.TEXT_DIRECTION_LTR
        }
        startResendTimer()
        intiListner()
        initViewModel()
        initResendOtp()

        return binding.root
    }

    private fun initResendOtp() {
        sendOtpViewModel =
            ViewModelProvider(this@ForgotPasswordVerification)[SendOtpViewModel::class.java]
        binding.lifecycleOwner = this@ForgotPasswordVerification

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

                        403,401 -> {
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
        verifyOtpViewModel =
            ViewModelProvider(this@ForgotPasswordVerification)[VerifyOtpViewModel::class.java]
        binding.viewModel = verifyOtpViewModel
        binding.lifecycleOwner = this
        binding.click = verifyOtpViewModel.ClickAction()
        verifyOtpViewModel.getVerifyOtpData().observe(this@ForgotPasswordVerification) {

            try {
                when (it.status) {

                    Status.SUCCESS -> {
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            VerifyOtpResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                                val toResetPassword =
                                    Intent(requireContext(), ResetPassword::class.java)
                                toResetPassword.putExtra(FORGOT_NUMBER, numberMobile)
                                startActivity(toResetPassword)
                                dismiss()
                            }

                            403,401 -> {
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()

                            }
                        }
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun intiListner() {
        binding.btnVerify.setOnClickListener {

            verifyOtpViewModel.request.mobile = numberMobile
            verifyOtpViewModel.request.countryCode = CC
            Log.d("TAG", "afterTextChanged: ${Gson().toJson(verifyOtpViewModel.request)}")
            verifyOtpViewModel.getVerifyOtpRequest()
        }

        binding.idMobileNumber.setOnClickListener {
            text.clear()
            dismiss()
        }

        binding.resendCode.setOnClickListener {
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        binding.resendCode.isEnabled = false

        var timeOutSeconds = 120 // timeout duration
        val timer = Timer()

        timer.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                timeOutSeconds--
                activity?.runOnUiThread {

                    if (timeOutSeconds > 0) {
                        binding.resendCode.text =
                            getString(R.string.resend_code) + "$timeOutSeconds " + getString(R.string.seconds)
                        binding.resendCode.textSize = 13f
                        binding.resendCode.setTextColor(
                            ContextCompat.getColor(
                                mContext, R.color.black
                            )
                        )
                    } else {
                        // Enable the button and reset the text when the timer reaches 0
                        timer.cancel()
                        binding.resendCode.isEnabled = true
                        binding.resendCode.text = getString(R.string.resend_code_green)
                        binding.resendCode.setTextColor(
                            ContextCompat.getColor(
                                mContext, R.color.greenhousetheme
                            )
                        )
                        binding.resendCode.textSize = 17f
                        binding.resendCode.elevation = 10f
                    }
                }
            }
        }, 0, 1000)
    }
}