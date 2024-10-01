package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerOrderCancelSelectOneBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked

class SellerOrderCancelSelectOne( ) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSellerOrderCancelSelectOneBinding

    var accountBlocked: AccountBlocked? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_order_cancel_select_one, container, false
        )
        binding.Button.setOnClickListener {
            dismiss()
            activity?.finish()
        }
        return binding.root
    }
}

