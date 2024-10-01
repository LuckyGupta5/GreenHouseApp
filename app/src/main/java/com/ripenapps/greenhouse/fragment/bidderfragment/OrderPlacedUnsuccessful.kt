package com.ripenapps.greenhouse.fragment.bidderfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentOrderPlacedUnsuccessfulBinding

class OrderPlacedUnsuccessful : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOrderPlacedUnsuccessfulBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_order_placed_unsuccessful, container, false
        )
        // Inflate the layout for this fragment

        isCancelable = false
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.apply {
            okButton.setOnClickListener {
                dismiss()
            }
        }
    }
}