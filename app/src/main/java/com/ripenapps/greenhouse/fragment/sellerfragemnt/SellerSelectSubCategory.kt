package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.SellerRecyclerSelectCategoryAdapter
import com.ripenapps.greenhouse.databinding.FragmentSellerSelectSubCategoryBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectCategoryModel

class SellerSelectSubCategory(
    private val selectedCategory: MutableList<SelectCategoryModel>,
    private val dismissCallBack: (String, String) -> Unit
)
    : BottomSheetDialogFragment() {

    private lateinit var  binding:FragmentSellerSelectSubCategoryBinding
    private val arrSelectSubCategory: MutableList<SelectCategoryModel> = mutableListOf()
    private var recyclerSubCategory: SellerRecyclerSelectCategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_seller_select_sub_category, container, false)
        isCancelable = true
        arrSelectSubCategory.addAll(selectedCategory)
//
        recyclerSubCategory = SellerRecyclerSelectCategoryAdapter( arrSelectSubCategory,::itemClickAction)
        binding.selectSubCategoryRecycler.adapter = recyclerSubCategory
        return binding.root
    }
    private fun itemClickAction(position: Int) {
        dismissCallBack.invoke(arrSelectSubCategory.getOrNull(position)?.text ?: "",arrSelectSubCategory.getOrNull(position)?.categoryId ?: "")
        dismiss()
        /*     Toast.makeText(mcontext, "Hello $position", Toast.LENGTH_SHORT).show()*/

    }
}