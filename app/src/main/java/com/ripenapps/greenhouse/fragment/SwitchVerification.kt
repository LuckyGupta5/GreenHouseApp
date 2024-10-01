package com.ripenapps.greenhouse.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSwitchVerificationBinding

class SwitchVerification(private val fromProfile: Boolean? = false) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSwitchVerificationBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_switch_verification, container, false
        )

        isCancelable=false
        binding.continueBtn.setOnClickListener {
            dismiss()
            if (fromProfile == false) {
                activity?.finish()
            }
        }
        return binding.root
    }

}
