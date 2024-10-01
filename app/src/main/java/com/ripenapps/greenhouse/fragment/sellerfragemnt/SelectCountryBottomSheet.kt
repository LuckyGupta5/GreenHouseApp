package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.RecyclerBankListAdapter
import com.ripenapps.greenhouse.databinding.FragmentSelectCountryBottomSheetBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.BankListModel


class SelectCountryBottomSheet : BottomSheetDialogFragment() {
    private lateinit var bankListBinding: FragmentSelectCountryBottomSheetBinding
    private val arrBankList: ArrayList<BankListModel> = arrayListOf()
    private var bankListAdapter: RecyclerBankListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bankListBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_select_country_bottom_sheet,
                container,
                false
            )
        arrBankList.add(BankListModel("Saudi Arabia"))
        arrBankList.add(BankListModel("India"))
        arrBankList.add(BankListModel("Canada"))

        bankListAdapter = RecyclerBankListAdapter(arrBankList)
        bankListBinding.bankListRecycler.adapter = bankListAdapter

        return bankListBinding.root
    }
}
