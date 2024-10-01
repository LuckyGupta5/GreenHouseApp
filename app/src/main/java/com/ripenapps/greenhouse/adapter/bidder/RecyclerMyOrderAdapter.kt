package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.MyOrderLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.MyOrderModel
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.setVisibility

@SuppressLint("NotifyDataSetChanged")
class RecyclerMyOrderAdapter(
    private val itemClick: (Int) -> (Unit)
) : RecyclerView.Adapter<RecyclerMyOrderAdapter.ViewHolder>() {
    val arrMyOrders: MutableList<MyOrderModel> = mutableListOf()

    fun addItems(list: MutableList<MyOrderModel>) {
        arrMyOrders.clear()
        arrMyOrders.addAll(list)
        notifyDataSetChanged()
    }


    fun clearData() {
        this.arrMyOrders.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<MyOrderModel>?) {
        if (itemList.isNullOrEmpty() || arrMyOrders.isNullOrEmpty()) return
        arrMyOrders.addAll(itemList)
        notifyItemRangeChanged(arrMyOrders.size - itemList.size, itemList.size)
    }

    inner class ViewHolder(private val binding: MyOrderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: MyOrderModel, itemClickEvent: (Int) -> (Unit)
        ) {
            binding.apply {
                myOrderModel = data
//                data.orderStatusImage.let {
//                    Glide.with(binding.root.context).load(data.orderStatusImage).placeholder(
//                        ContextCompat.getDrawable(
//                            binding.root.context, R.drawable.image_placeholder_new
//                        )
//                    ).into(this.orderStatusImage)
//                }
                data.productImage.let {
                    Glide.with(binding.root.context).load(data.productImage).placeholder(
                        ContextCompat.getDrawable(
                            binding.root.context, R.drawable.image_placeholder_new
                        )
                    ).into(this.productImage)
                }
                when (data.orderStatus) {
                    "Confirmed" -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.order_confirmed)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_status_small)
                        binding.timeLeft.setVisibility(false)
                    }
                    "Received" -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.order_received)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_status_small)
                        binding.timeLeft.setVisibility(true)
                    }

                    "Cancelled" -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.order_cancelled)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_cancelled)
                        binding.timeLeft.setVisibility(false)
                    }

                    "Delivered" -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.order_delivered)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_delivered)
                        binding.timeLeft.setVisibility(false)
                    }

                    "Packed" -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.order_packed)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_delivered)
                        binding.timeLeft.setVisibility(true)
                    }

                    else -> {
                        binding.orderStatus.text =
                            binding.root.context.getString(R.string.return_received)
                        binding.orderStatusImage.setImageResource(R.drawable.ic_order_returned)
                    }
                }
                root.setOnClickListener {
                    itemClickEvent.invoke(absoluteAdapterPosition)
                }

                if (Preferences.getStringPreference(binding.root.context, "language").equals("ar")) {
                    arrow.rotation = 180f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.my_order_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrMyOrders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrMyOrders.getOrNull(position)?.let { dataModel ->
            holder.bind(dataModel) {
                itemClick.invoke(it)
            }
        }
    }
}