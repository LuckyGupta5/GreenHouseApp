package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.SelectSoldProductClickBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectSoldProductModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerSelectSoldProduct (
    private val selectedItem: (SelectSoldProductModel) -> (Unit)
) : RecyclerView.Adapter<RecyclerSelectSoldProduct.ViewHolder>() {
    private var arrSoldProduct = mutableListOf<SelectSoldProductModel>()
    @SuppressLint("NotifyDataSetChanged")
    fun addAddress(listOfAddress: MutableList<SelectSoldProductModel>) {
        arrSoldProduct = listOfAddress
        notifyDataSetChanged()
    }
    inner class ViewHolder(
        private val binding: SelectSoldProductClickBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: SelectSoldProductModel, selectedItemClick: (SelectSoldProductModel) -> Unit
        ) {
            binding.apply {
                sellerSoldProductModel = data
                root.setOnClickListener {
                    selectedItemClick.invoke(data)
                }
                view2.setVisibility(absoluteAdapterPosition != arrSoldProduct.size - 1)
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerSelectSoldProduct.ViewHolder, position: Int) {
        arrSoldProduct.getOrNull(position)?.let { dataModel ->
            holder.bind(dataModel, selectedItem)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerSelectSoldProduct.ViewHolder {
        return ViewHolder(
            SelectSoldProductClickBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrSoldProduct.size
    }
}