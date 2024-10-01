package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.NotificationLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.NotificationModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerNotificationAdapter(val callBack: (NotificationModel, String) -> Unit) :
    RecyclerView.Adapter<RecyclerNotificationAdapter.ViewHolder>() {
    val arrNotification: MutableList<NotificationModel> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<NotificationModel>) {
        arrNotification.clear()
        arrNotification.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrNotification.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<NotificationModel>?) {
        if (itemList.isNullOrEmpty() || arrNotification.isNullOrEmpty()) return
        arrNotification.addAll(itemList)
        notifyItemRangeChanged(itemList.size - itemList.size, itemList.size)
    }

    inner class ViewHolder(private val notificationBinding: NotificationLayoutBinding) :
        RecyclerView.ViewHolder(notificationBinding.root) {
        fun bind(data: NotificationModel) {
            notificationBinding.apply {
                notificationModel = data
                this.notificationIcon.setImageResource(R.drawable.ic_notification)

                if (absoluteAdapterPosition > 0) {
                    notificationDate.isVisible = arrNotification.getOrNull(absoluteAdapterPosition)
                        ?.formattedDate() != arrNotification.getOrNull(absoluteAdapterPosition - 1)
                        ?.formattedDate()
                } else {
                    notificationDate.isVisible = true
                }
                view.setVisibility(absoluteAdapterPosition != arrNotification.size - 1)
                buttonLayer.setVisibility(data.buttonKey == 1)

                rejectButton.setOnClickListener {
                    callBack.invoke(data, "reject")
                }
                acceptButton.setOnClickListener {
                    callBack.invoke(data, "accept")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.notification_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrNotification.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}