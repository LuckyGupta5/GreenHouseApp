package com.ripenapps.greenhouse.adapter.seller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.BankListLayoutBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.BankListModel

class RecyclerBankListAdapter(
private val arrBAnkList: ArrayList<BankListModel>
) : RecyclerView.Adapter<RecyclerBankListAdapter.ViewHolder>() {
    inner class ViewHolder(private val bankListLayoutBinding: BankListLayoutBinding) :
        RecyclerView.ViewHolder(bankListLayoutBinding.root) {
        fun bind(data: BankListModel) {
            bankListLayoutBinding.apply {
                bankListModel = data
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.bank_list_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrBAnkList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrBAnkList.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}