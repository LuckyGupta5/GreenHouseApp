package com.ripenapps.greenhouse.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSwitchRejected3TimesBinding

class SwitchRejected3Times : BottomSheetDialogFragment() {

    private lateinit var binding:FragmentSwitchRejected3TimesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_switch_rejected3_times, container, false)

        binding.Button.setOnClickListener {
            dismiss()
            activity?.finish()
        }
        return binding.root
    }
}