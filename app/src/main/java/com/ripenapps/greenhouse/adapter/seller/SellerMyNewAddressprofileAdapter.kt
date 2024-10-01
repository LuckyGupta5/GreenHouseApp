package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.SellerMyAddressBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMyAddressModel
import com.ripenapps.greenhouse.screen.seller.SellerMyAddress
import com.ripenapps.greenhouse.utills.setVisibility

class SellerMyNewAddressprofileAdapter :
    RecyclerView.Adapter<SellerMyNewAddressprofileAdapter.ViewHolder>() {

    private var mListner: onClickListner? = null

    interface onClickListner {
        fun onEdit(position: Int)
        fun onDelete(position: Int)
    }


    fun onClickListner(listner: SellerMyAddress) {
        mListner = listner
    }

    var arrAddress: MutableList<SellerMyAddressModel>? = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<SellerMyAddressModel>) {
        arrAddress?.clear()
        arrAddress?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrAddress?.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<SellerMyAddressModel>?) {
        if (itemList.isNullOrEmpty() || arrAddress.isNullOrEmpty()) return
        arrAddress?.addAll(itemList)
        notifyItemRangeChanged((arrAddress?.size ?: 0) - itemList.size, itemList.size)
    }

    inner class ViewHolder(
        private val binding: SellerMyAddressBinding, private val listner: onClickListner
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SellerMyAddressModel) {
            binding.apply {
                sellerMyAdreessModel = data
                btnEdit.setOnClickListener {
                    listner.onEdit(absoluteAdapterPosition)
                }
                btnDeleteAddress.setOnClickListener {
                    listner.onDelete(absoluteAdapterPosition)
                }

                lastView.setVisibility(absoluteAdapterPosition != (arrAddress?.size?.minus(1) ?: 0))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SellerMyAddressBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), mListner!!
        )
    }

    override fun getItemCount(): Int {
        return arrAddress?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrAddress?.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}