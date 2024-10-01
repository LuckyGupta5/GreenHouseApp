package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentVerificationToAdminSellerBinding
import com.ripenapps.greenhouse.model.register_user.response.CheckProfileResponse
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import com.ripenapps.greenhouse.screen.seller.VerificationRejectedByAdmin
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.CheckProfileViewModel

class VerificationToAdminSeller : BottomSheetDialogFragment() {
    private lateinit var checkProfileViewModel: CheckProfileViewModel
    private lateinit var verificationToAdminSellerBinding: FragmentVerificationToAdminSellerBinding
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        verificationToAdminSellerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_verification_to_admin_seller, container, false
        )

        isCancelable = false
        initViewModel()

        checkProfileViewModel.request.mobile =
            Preferences.getStringPreference(mContext, "mobile")
        checkProfileViewModel.request.countryCode = CC
        checkProfileViewModel.getCheckProfileRequest()
        return verificationToAdminSellerBinding.root
    }


    private fun initViewModel() {
        checkProfileViewModel =
            ViewModelProvider(this@VerificationToAdminSeller)[CheckProfileViewModel::class.java]
        verificationToAdminSellerBinding.lifecycleOwner = this@VerificationToAdminSeller

        checkProfileViewModel.getCheckProfileData().observe(viewLifecycleOwner) {

            try {
                when (it.status) {

                    Status.SUCCESS -> {
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            CheckProfileResponse::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Preferences.setStringPreference(
                                    mContext, "token", data.data.token
                                )
                                val toHome = Intent(mContext, HomeSeller::class.java)
                                startActivity(toHome)
                                dismiss()
                                activity?.finish()
                            }

                            423 -> {
                                val toRejected = Intent(
                                    mContext, VerificationRejectedByAdmin::class.java
                                )
                                val reason = data.data.rejectedReason
                                toRejected.putExtra("REJECTED_REASON", reason)
                                toRejected.putExtra(
                                    "NUMBER",
                                    Preferences.getStringPreference(mContext, "mobile")
                                )
                                startActivity(toRejected)
                            }

                            425 -> {
                                val rejected3Times = RejectedBottomSheet()
                                rejected3Times.show(parentFragmentManager, rejected3Times.tag)
                            }

                            401 -> {
                                Toast.makeText(
                                    mContext, "User not found", Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT)
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
}