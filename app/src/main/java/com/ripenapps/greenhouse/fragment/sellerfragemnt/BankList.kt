package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.RecyclerBankListAdapter
import com.ripenapps.greenhouse.databinding.FragmentBankListBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.BankListModel

class BankList : BottomSheetDialogFragment() {
    private lateinit var bankListBinding: FragmentBankListBinding
    private val arrBankList: ArrayList<BankListModel> = arrayListOf()
    private var bankListAdapter: RecyclerBankListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        bankListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bank_list, container, false)
        arrBankList.add(BankListModel("National Commercial Bank"))
        arrBankList.add(BankListModel("Al Rajhi Bank"))
        arrBankList.add(BankListModel("Riyad Bank"))
        arrBankList.add(BankListModel("Banque Saudi Fransi."))
        arrBankList.add(BankListModel("Arab National Bank"))
        arrBankList.add(BankListModel("Saudi British Bank (SABB)"))
        arrBankList.add(BankListModel(" Saudi Investment Bank"))

        bankListAdapter = RecyclerBankListAdapter(arrBankList)
        bankListBinding.bankListRecycler.adapter = bankListAdapter

        return bankListBinding.root
    }
}