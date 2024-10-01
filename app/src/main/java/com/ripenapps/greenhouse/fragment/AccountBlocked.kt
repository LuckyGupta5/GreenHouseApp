package com.ripenapps.greenhouse.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentAccountBlockedBinding
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.Preferences

class AccountBlocked(val message: String) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAccountBlockedBinding
    private lateinit var mContext:Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_account_blocked, container, false)

        isCancelable = false

        binding.accountBlockedDesc.text = message
        binding.okButton.setOnClickListener {
            Preferences.removeAllPreference(requireContext())
            val signin = Intent(mContext, SignIn::class.java)
            Preferences.removePreference(mContext, "token")
            Preferences.removePreference(mContext, "user_details")
            Preferences.removePreference(mContext, "isVerified")
            Preferences.removePreference(mContext, "roomId")
            startActivity(signin)
            activity?.finishAffinity()
        }
        return binding.root
    }
}