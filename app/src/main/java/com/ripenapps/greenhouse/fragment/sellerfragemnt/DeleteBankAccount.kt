package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentDeleteBankAccountBinding

class DeleteBankAccount : BottomSheetDialogFragment() {

    private lateinit var deleteBankAccountBinding: FragmentDeleteBankAccountBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        deleteBankAccountBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_delete_bank_account, container, false
        )
        isCancelable = false
        // Inflate the layout for this fragment
        initListeners()
        return deleteBankAccountBinding.root
    }

    private fun initListeners() {
        deleteBankAccountBinding.cancelBUtton.setOnClickListener {
            dismiss()
        }
        deleteBankAccountBinding.removeButton.setOnClickListener {
            dismiss()
        }
    }
}