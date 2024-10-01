package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.BankAccountLayoutBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.BankAccountModel

class RecyclerBankAccountAdapter(
    private val contextBankAccount: Context, private val arrBankAccount: ArrayList<BankAccountModel>
) : RecyclerView.Adapter<RecyclerBankAccountAdapter.ViewHolder>() {

    private lateinit var mListner: onClickBank

    interface onClickBank {
        fun onDeleteBank(position: Int)
    }

    fun onClickListner(listner: onClickBank) {
        mListner = listner
    }

    inner class ViewHolder(
        private val bankAccountBinding: BankAccountLayoutBinding, private val listner: onClickBank
    ) : RecyclerView.ViewHolder(bankAccountBinding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: BankAccountModel) {
            bankAccountBinding.apply {
                bankAccountModel = data
                this.bankImage.setImageDrawable(contextBankAccount.resources.getDrawable(data.bankImage))
                btnDelete.setOnClickListener {
                    listner.onDeleteBank(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.bank_account_layout, parent, false
            ), mListner
        )
    }

    override fun getItemCount(): Int {
        return arrBankAccount.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrBankAccount.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}