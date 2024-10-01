package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.TicketLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.TicketModel

class RecyclerTicketAdapter(
    private val callBack: (TicketModel, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerTicketAdapter.ViewHolder>() {
    var arrTickets: MutableList<TicketModel> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<TicketModel>) {
        this.arrTickets.clear()
        this.arrTickets.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.arrTickets.clear()
        notifyDataSetChanged()
    }

    fun updateItemList(itemList: MutableList<TicketModel>?) {
        if (itemList.isNullOrEmpty() || arrTickets.isEmpty()) return
        arrTickets.addAll(itemList)
        notifyItemRangeChanged(arrTickets.size - itemList.size, itemList.size)
    }


    inner class ViewHolder(private val ticketBinding: TicketLayoutBinding) :
        RecyclerView.ViewHolder(ticketBinding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: TicketModel) {
            ticketBinding.apply {
                ticketModel = data

                if (data.ticketStatus == true) {
                    iButton.text = ticketBinding.root.context.getString(R.string.open)
                    iButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_i_yellow, 0, 0, 0
                    )
                    iButton.setTextColor(
                        ContextCompat.getColor(
                            ticketBinding.root.context, R.color.open_ticket_color
                        )
                    )

                } else {
                    iButton.text = ticketBinding.root.context.getString(R.string.solved)
                    iButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_i_green, 0, 0, 0
                    )
                    iButton.setTextColor(
                        ContextCompat.getColor(
                            ticketBinding.root.context, R.color.greenhousetheme
                        )
                    )
                }

                ticketLayout.setOnClickListener {
                    callBack.invoke(data, absoluteAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.ticket_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrTickets.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrTickets.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}