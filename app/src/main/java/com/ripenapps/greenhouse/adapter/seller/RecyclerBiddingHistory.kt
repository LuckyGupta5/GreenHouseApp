package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.BiddingHistoryBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.HistoryModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerBiddingHistory(

) : RecyclerView.Adapter<RecyclerBiddingHistory.ViewHolder>() {
    val arrHistory: MutableList<HistoryModel>? = mutableListOf()
    fun addItems(list: MutableList<HistoryModel>) {
        arrHistory?.clear()
        arrHistory?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrHistory?.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<HistoryModel>?) {
        if (itemList.isNullOrEmpty() || arrHistory.isNullOrEmpty()) return
        arrHistory?.addAll(itemList)
        notifyItemRangeChanged(itemList.size - itemList.size, itemList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: BiddingHistoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.bidding_history, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrHistory?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrHistory?.get(position), position)
    }

    inner class ViewHolder(private val biddingHistoryBinding: BiddingHistoryBinding) :
        RecyclerView.ViewHolder(biddingHistoryBinding.root) {
        fun bind(data: HistoryModel?, position: Int) {
            biddingHistoryBinding.historyModel = data
            when (position) {
                0 -> {
                    biddingHistoryBinding.highestBtn.setVisibility(true)
                }

                else -> {
                    biddingHistoryBinding.highestBtn.setVisibility(false)
                }
            }
            try {
                biddingHistoryBinding.view.setVisibility(
                    adapterPosition != (arrHistory?.size ?: 0) - 1
                )
            } catch (ex: Exception) {
                Toast.makeText(
                    biddingHistoryBinding.root.context, "Something went wrong!", Toast.LENGTH_SHORT
                ).show()
            }


        }
    }
}
