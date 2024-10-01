package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.MyBidsLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.MyBidModel
import com.ripenapps.greenhouse.utills.CommonUtils.startTimer
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerMyBidAdapter : RecyclerView.Adapter<RecyclerMyBidAdapter.ViewHolder>() {
    val arrMyBids: MutableList<MyBidModel> = mutableListOf()
    private lateinit var mListner: onClickListner

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<MyBidModel>) {
        arrMyBids.clear()
        arrMyBids.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrMyBids.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<MyBidModel>?) {
        if (itemList.isNullOrEmpty() || arrMyBids.isEmpty()) return
        arrMyBids.addAll(itemList)
        notifyItemRangeChanged(arrMyBids.size - itemList.size, itemList.size)
    }


    interface onClickListner {
        fun onClick(position: Int)
    }

    fun onClickListner(listner: onClickListner) {
        mListner = listner
    }

    inner class ViewHolder(
        private val binding: MyBidsLayoutBinding, private val listner: onClickListner
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MyBidModel) {
            binding.apply {
                bidStatus.text = when (data.statusText) {
                    "won" -> {
                        if (data.orderPlaced == true) timer.setVisibility(false)
                        else timer.setVisibility(true)
                        Log.d(
                            "TAG", "startTimerInputs: ${binding.timer} ${data.timerToOrder}"
                        )
                        if (startTimer(binding.timer, data.timerToOrder) == 0) {
                            startTimer(binding.timer, data.secondTimer)
                        } else {
                            startTimer(binding.timer, data.timerToOrder)
                        }

                        bidStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.greenhousetheme
                            )
                        )
                        bidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_bid, 0, 0, 0
                        )
                        binding.root.context.getString(R.string.you_won)
                    }

                    "missed" -> {
                        timer.setVisibility(false)
                        bidStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.red
                            )
                        )
                        bidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_red_bid, 0, 0, 0
                        )
                        binding.root.context.getString(R.string.you_lost)
                    }

                    "loss" -> {
                        timer.setVisibility(false)
                        bidStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.red
                            )
                        )
                        bidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_red_bid, 0, 0, 0
                        )
                        binding.root.context.getString(R.string.you_lost)
                    }

                    "pending" -> {
                        timer.setVisibility(false)
                        bidStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.pending
                            )
                        )
                        bidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_pending, 0, 0, 0
                        )
                        binding.root.context.getString(R.string.bid_in_process)
                    }

                    else -> {
                        timer.setVisibility(false)
                        bidStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.unfilled
                            )
                        )
                        bidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_blue_bids, 0, 0, 0
                        )
                        binding.root.context.getString(R.string.unfilled_seller_request)
                    }
                }
                myBidModel = data
                sellerMyBids.setOnClickListener {
                    listner.onClick(absoluteAdapterPosition)
                }
                productImage.setImage(data.productImage.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.my_bids_layout, parent, false
            ), mListner
        )
    }

    override fun getItemCount(): Int {
        return arrMyBids.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrMyBids.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}