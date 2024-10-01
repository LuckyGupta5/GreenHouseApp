package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentBidSuccessfullPopUpBinding
import com.ripenapps.greenhouse.utills.Preferences

class BidSuccessfullPopUp(private val enteredAmount: String) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBidSuccessfullPopUpBinding

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_bid_successfull_pop_up, container, false
        )
        if (Preferences.getStringPreference(requireContext(), "language") == "ar") {
        }

        initListeners()
        binding.desc.text =
            getString(
                R.string.sar_has_been_freezed_from_your_wallet_it_will_be_deducted_once_you_won_the_auction_else_it_will_be_unfreezed,
                enteredAmount
            )
        return binding.root
    }

    private fun initListeners() {
        binding.okButton.setOnClickListener {
            dismiss()
            activity?.finish()
        }
    }


}

