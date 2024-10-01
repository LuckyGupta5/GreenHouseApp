package com.ripenapps.greenhouse.adapter.seller

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.SellerSelectCategoryProductRecyclerBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectCategoryModel
import com.ripenapps.greenhouse.utills.setVisibility

class SellerRecyclerSelectCategoryAdapter(
    private val arrSelectCategory: MutableList<SelectCategoryModel>,
    private val itemClick: (Int) -> (Unit)
) : RecyclerView.Adapter<SellerRecyclerSelectCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val sellerSelectCategoryBinding: SellerSelectCategoryProductRecyclerBinding) :
        RecyclerView.ViewHolder(sellerSelectCategoryBinding.root) {
        fun bind(data: SelectCategoryModel, performCategories: (Int) -> Unit) {
            sellerSelectCategoryBinding.apply {
                selectCategoryModel = data
                root.setOnClickListener {
                    performCategories.invoke(absoluteAdapterPosition)
                }
                sellerSelectCategoryBinding.click.setVisibility(data.isSelect)
                sellerSelectCategoryBinding.view3.setVisibility(absoluteAdapterPosition != arrSelectCategory.size - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.seller_select_category_product_recycler,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrSelectCategory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrSelectCategory.getOrNull(position)?.let { holder.bind(it) { itemClick.invoke(it) } }
    }
}
