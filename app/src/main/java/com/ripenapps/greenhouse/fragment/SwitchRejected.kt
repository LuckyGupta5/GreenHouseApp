package com.ripenapps.greenhouse.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSwitchRejectedBinding
import com.ripenapps.greenhouse.screen.seller.SellerCompleteProfile2

class SwitchRejected(
    val reason: String?,
    val stringPreference: String?
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSwitchRejectedBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_switch_rejected, container, false)

        binding.resubmitButton.setOnClickListener {
            val toSellerCompleteProfile =
                Intent(requireContext(), SellerCompleteProfile2::class.java)
            toSellerCompleteProfile.putExtra("NUMBER", stringPreference)
            toSellerCompleteProfile.putExtra("fromBidder", 1)
            startActivity(toSellerCompleteProfile)
            dismiss()
            activity?.finish()
        }

        binding.Button.setOnClickListener {
            dismiss()
            activity?.finish()
        }

        binding.description.text = reason.toString()
        return binding.root
    }

}