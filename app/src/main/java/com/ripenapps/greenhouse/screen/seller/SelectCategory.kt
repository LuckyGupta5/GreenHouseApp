package com.ripenapps.greenhouse.screen.seller

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.SellerRecyclerSelectCategoryAdapter
import com.ripenapps.greenhouse.databinding.FragmentSelectCategoryBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectCategoryModel

class SelectCategory(
    val dataModel: MutableList<SelectCategoryModel>,
    private val dismissCallBack: (String, String, Int) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var selectCategoryBinding: FragmentSelectCategoryBinding
    private val arrSelectCategory: MutableList<SelectCategoryModel> = mutableListOf()
    private var recyclerCategory: SellerRecyclerSelectCategoryAdapter? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        selectCategoryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_category, container, false)
        isCancelable = true
        arrSelectCategory.addAll(dataModel)
        recyclerCategory = SellerRecyclerSelectCategoryAdapter(
            arrSelectCategory,
            ::itemClickAction
        )
        selectCategoryBinding.selectCategoryRecycler.adapter = recyclerCategory
        return selectCategoryBinding.root
    }

    private fun itemClickAction(position: Int) {
        dismissCallBack.invoke(arrSelectCategory.getOrNull(position)?.text ?: "",
            arrSelectCategory.getOrNull(position)?.categoryId ?: "", position)
        dismiss()
    }
}