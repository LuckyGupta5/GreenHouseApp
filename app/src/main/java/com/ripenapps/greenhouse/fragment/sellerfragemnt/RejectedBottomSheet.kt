package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentRejectedBottomSheetBinding
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.SignUp

class RejectedBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentRejectedBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_rejected_bottom_sheet, container, false)

        binding.Button.setOnClickListener {
            val toSignup = Intent(requireContext(),SignIn::class.java)
            startActivity(toSignup)
            dismiss()
            activity?.finish()
        }
        return binding.root
    }

}