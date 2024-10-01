package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerMarkAsReceivedBinding

class SellerMarkAsReceivedFragment : BottomSheetDialogFragment() {
private  lateinit var  binding:FragmentSellerMarkAsReceivedBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil. inflate(inflater,R.layout.fragment_seller_mark_as_received, container, false)
        return binding.root

    }

}