package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.TransactionHistoryBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.TransactionHistoryModel
@SuppressLint("NotifyDataSetChanged")
class RecyclerTransactionHistory : RecyclerView.Adapter<RecyclerTransactionHistory.ViewHolder>() {

    var arrTransactionHistory: MutableList<TransactionHistoryModel> = mutableListOf()

    fun addItems(list: MutableList<TransactionHistoryModel>) {
        this.arrTransactionHistory = list
        notifyDataSetChanged()
    }
    fun clearData() {
        this.arrTransactionHistory.clear()
        notifyDataSetChanged()
    }
    fun updateList(itemList: List<TransactionHistoryModel>?) {
        if (itemList.isNullOrEmpty() || arrTransactionHistory.isNullOrEmpty()) return
        arrTransactionHistory.addAll(itemList)
        notifyItemRangeChanged(itemList.size - itemList.size, itemList.size)
    }
    inner class ViewHolder(private val transactionHistoryBinding: TransactionHistoryBinding) :
        RecyclerView.ViewHolder(transactionHistoryBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: TransactionHistoryModel) {
            transactionHistoryBinding.apply {
                transactionHistoryModel = data
                if (data.transactionType == "debit") {
                    addMoneyHistory.setTextColor(
                        ContextCompat.getColor(
                            transactionHistoryBinding.root.context, R.color.red
                        )
                    )
                    addMoneyHistory.text = "-"
                } else {

                    addMoneyHistory.setTextColor(
                        ContextCompat.getColor(
                            transactionHistoryBinding.root.context, R.color.greenhousetheme
                        )
                    )
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.transaction_history, parent, false
            )
        )
    }
    override fun getItemCount(): Int {
        return arrTransactionHistory.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrTransactionHistory.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}