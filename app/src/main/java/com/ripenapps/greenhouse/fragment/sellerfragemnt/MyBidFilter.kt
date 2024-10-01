package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentMyBidFilterBinding

class MyBidFilter : BottomSheetDialogFragment() {
    private lateinit var myBidFilterBinding: FragmentMyBidFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using DataBindingUtil and assign it to myBidFilterBinding
        myBidFilterBinding =DataBindingUtil.inflate(inflater, R.layout.fragment_my_bid_filter, container, false)

        // Return the root view of the inflated layout
        return myBidFilterBinding.root
    }

}
