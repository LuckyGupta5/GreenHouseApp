package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentAddPackageConfirmedBinding


class AddPackageConfirmed : BottomSheetDialogFragment() {
    private lateinit var addPackageConfirmedBinding: FragmentAddPackageConfirmedBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        addPackageConfirmedBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_package_confirmed,
            container,
            false
        )
        addPackageConfirmedBinding.btnOk.setOnClickListener {
            dismiss()
        }
        return addPackageConfirmedBinding.root
    }

}