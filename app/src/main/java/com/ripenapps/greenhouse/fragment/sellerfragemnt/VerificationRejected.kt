package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentVerificationRejectedBinding
import com.ripenapps.greenhouse.screen.seller.SellerCompleteProfile

class VerificationRejected(private val stringExtra: String?, private val stringExtra1: String?) :
    BottomSheetDialogFragment() {
    private lateinit var binding: FragmentVerificationRejectedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        isCancelable = false
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_verification_rejected, container, false
        )
        binding.description.text = stringExtra1

        // binding.description.text = reason
        binding.Button.setOnClickListener {
            val toSellerCompleteProfile =
                Intent(requireContext(), SellerCompleteProfile::class.java)
            toSellerCompleteProfile.putExtra("NUMBER", stringExtra)
            Log.d("TAG", "onCreateView:$stringExtra")
            startActivity(toSellerCompleteProfile)
            dismiss()
            activity?.finish()
        }
        return binding.root
    }
}