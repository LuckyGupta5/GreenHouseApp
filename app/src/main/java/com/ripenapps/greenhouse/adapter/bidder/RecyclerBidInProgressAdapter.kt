package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.BidInProgressLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.BidProgressModel
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerproductModel

class RecyclerBidInProgressAdapter(

) : RecyclerView.Adapter<RecyclerBidInProgressAdapter.ViewHolder>() {
     val arrBisInProgress: MutableList<BidProgressModel>?= mutableListOf()
    private lateinit var mListner: onClickListner


    fun addItems(list: MutableList<BidProgressModel>) {
        arrBisInProgress?.clear()
        arrBisInProgress?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrBisInProgress?.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<BidProgressModel>?) {
        if (itemList.isNullOrEmpty() || arrBisInProgress.isNullOrEmpty()) return
        arrBisInProgress?.addAll(itemList)
        notifyItemRangeChanged((arrBisInProgress?.size ?: 0) - itemList.size, itemList.size)
    }
    interface onClickListner {
        fun progressClick(position: Int)
    }

    fun bidInProgressListener(listner: onClickListner) {
        mListner = listner
    }

    inner class ViewHolder(
        private val bindingBidInProgress: BidInProgressLayoutBinding,
        private val listner: onClickListner
    ) : RecyclerView.ViewHolder(bindingBidInProgress.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: BidProgressModel) {
            bindingBidInProgress.apply {
                progressBidModel = data
                data.image.let {
                    Glide.with(bindingBidInProgress.root.context).load(data.image).placeholder(
                        ContextCompat.getDrawable(
                            bindingBidInProgress.root.context, R.drawable.image_placeholder_new
                        )
                    ).into(this.vegImg)
                }
                sellerMyBids.setOnClickListener {
                    listner.progressClick(absoluteAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.bid_in_progress_layout, parent, false
            ), mListner
        )
    }

    override fun getItemCount(): Int {
        return arrBisInProgress?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrBisInProgress?.getOrNull(position).let { dataModel ->
            if (dataModel != null) {
                holder.bind(dataModel)
            }
        }
    }
}