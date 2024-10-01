package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.SellerSelectAddressBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMyAddressModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerSelectAddress(
    private val selectedItem: (SellerMyAddressModel) -> (Unit)
) : RecyclerView.Adapter<RecyclerSelectAddress.ViewHolder>() {

    private var arrAddress = mutableListOf<SellerMyAddressModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun addAddress(listOfAddress: MutableList<SellerMyAddressModel>) {
        arrAddress = listOfAddress
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: SellerSelectAddressBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SellerMyAddressModel, selectedItemClick: (SellerMyAddressModel) -> Unit) {
            binding.apply {
                sellerSelectAddressModel = data
                root.setOnClickListener {
                    selectedItemClick.invoke(data)
                }
                view2.setVisibility(absoluteAdapterPosition != arrAddress.size - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SellerSelectAddressBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrAddress.getOrNull(position)?.let { dataModel ->
            holder.bind(dataModel, selectedItem)
        }
    }

    override fun getItemCount(): Int {
        return arrAddress.size
    }

}