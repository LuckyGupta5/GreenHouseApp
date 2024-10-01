package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.dpToPixel
import com.ripenapps.greenhouse.databinding.SellerProductLayoutBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerproductModel

class SellerProductFragmentAdapter(
    private val privateContext: Context,
    private val itemClick: (Int) -> (Unit)
) : RecyclerView.Adapter<SellerProductFragmentAdapter.ViewHolder>() {

    var arrSellerProduct: MutableList<SellerproductModel>? = mutableListOf()

    fun addItems(list: MutableList<SellerproductModel>) {
        arrSellerProduct?.clear()
        arrSellerProduct?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrSellerProduct?.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<SellerproductModel>?) {
        if (itemList.isNullOrEmpty() || arrSellerProduct.isNullOrEmpty()) return
        arrSellerProduct?.addAll(itemList)
        notifyItemRangeChanged((arrSellerProduct?.size ?: 0) - itemList.size, itemList.size)
    }

    inner class ViewHolder(private val sellerProductsBinding: SellerProductLayoutBinding) :
        RecyclerView.ViewHolder(sellerProductsBinding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: SellerproductModel, performItemClick: (Int) -> Unit) {
            sellerProductsBinding.apply {
                sellerModel = data
                data.itemImg.let {
                    Glide.with(sellerProductsBinding.root.context).load(data.itemImg).placeholder(
                        ContextCompat.getDrawable(
                            sellerProductsBinding.root.context, R.drawable.image_placeholder_new
                        )
                    ).into(this.sellerImg)
                }
                sellerProductsBinding.isFeatured.visibility =
                    if (data.isFeatured == true) View.VISIBLE else View.GONE

                this.sellerLayout.layoutParams.width =
                    privateContext.resources.displayMetrics.widthPixels / 2 - sellerProductsBinding.root.context.dpToPixel(
                        25
                    )
                root.setOnClickListener {
                    performItemClick.invoke(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SellerProductLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrSellerProduct?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrSellerProduct?.getOrNull(position)
            ?.let { dataModel -> holder.bind(dataModel, itemClick) }
    }
}