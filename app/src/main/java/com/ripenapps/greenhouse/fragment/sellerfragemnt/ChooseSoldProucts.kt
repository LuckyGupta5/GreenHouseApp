package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentChooseSoldProuctsBinding

class ChooseSoldProucts : BottomSheetDialogFragment() {
    private lateinit var chooseSoldProuctsBinding:FragmentChooseSoldProuctsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        chooseSoldProuctsBinding=DataBindingUtil.inflate(inflater, R.layout.fragment_choose_sold_proucts, container, false)
        return chooseSoldProuctsBinding.root
    }

}