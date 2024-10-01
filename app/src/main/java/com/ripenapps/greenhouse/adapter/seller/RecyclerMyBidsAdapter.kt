package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.MyBidLayoutBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.MyBidsSellerModel
import com.ripenapps.greenhouse.utills.setImage

class RecyclerMyBidsAdapter(
    private val itemClick: (Int) -> (Unit)
) : RecyclerView.Adapter<RecyclerMyBidsAdapter.ViewHolder>() {
    val arrMyBids: MutableList<MyBidsSellerModel>? = mutableListOf()
    private lateinit var mListner: onClickListner

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<MyBidsSellerModel>) {
        arrMyBids?.clear()
        arrMyBids?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrMyBids?.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<MyBidsSellerModel>?) {
        if (itemList.isNullOrEmpty() || arrMyBids.isNullOrEmpty()) return
        arrMyBids.addAll(itemList)
        notifyItemRangeChanged(itemList.size - itemList.size, itemList.size)
    }


    interface onClickListner {
        fun onClick(position: Int)
    }

    fun onClickListner(listner: onClickListner) {
        mListner = listner
    }

    inner class ViewHolder(private val MyBidBinding: MyBidLayoutBinding) :
        RecyclerView.ViewHolder(MyBidBinding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: MyBidsSellerModel, listener: (Int) -> Unit) {
            MyBidBinding.apply {
                productModel = data
                data.vegimg.let {
                    vegImg.setImage(data.vegimg.toString())
                    sellerMyBids.setOnClickListener {
                        listener.invoke(absoluteAdapterPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MyBidLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrMyBids?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrMyBids?.getOrNull(position)?.let { dataModel -> holder.bind(dataModel, itemClick) }
    }
}
