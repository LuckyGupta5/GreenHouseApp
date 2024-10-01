package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentOrderPlacedSuccessfulBinding

class OrderPlacedSuccessful(val orderId: Int?) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOrderPlacedSuccessfulBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_order_placed_successful, container, false
        )
        // Inflate the layout for this fragment
        isCancelable = false
        initListeners()

        binding.orderId.text = "#$orderId"
        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            okButton.setOnClickListener {
                dismiss()
                activity?.finish()
            }
        }
    }
}