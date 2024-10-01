package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.DateLayoutBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.DateClass

class RecyclerDateAdapter(
    var list: ArrayList<DateClass>, var context: Context, var onDateSelected: OnDateSelected
) : RecyclerView.Adapter<RecyclerDateAdapter.ViewHolder>() {
    class ViewHolder(var binding: DateLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    var oldSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.date_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size - 1
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val dataModel = list.getOrNull(position)
            weekDate.text = dataModel?.day
            weekDay.text = dataModel?.weekDay
        }

        holder.binding.click = ClickAction(position)
        holder.binding.model = list[position]
        if (list[position].isSelect == true) {
            holder.binding.dateLayout.setBackgroundResource(R.drawable.round_7dp_green_border)
            holder.binding.weekDate.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.white
                )
            )
            holder.binding.weekDay.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.white
                )
            )

        } else {
            holder.binding.dateLayout.setBackgroundResource(R.drawable.round_7dp_grey_border)
            holder.binding.weekDate.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.black
                )
            )
            holder.binding.weekDay.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.black
                )
            )
        }
    }

    inner class ClickAction(var position: Int) {
        @SuppressLint("NotifyDataSetChanged")
        fun onDate(view: View) {
            val selectedDate = list.getOrNull(position)?.month
            unSelectOther(position)
            list[position].isSelect = !(list.getOrNull(position)?.isSelect ?: false)
            Log.d("item click", "onDate: ${list[position].isSelect}")
            if (oldSelectedPosition != -1 && oldSelectedPosition != position) {
                list.getOrNull(oldSelectedPosition)?.isSelect = false
                notifyItemChanged(oldSelectedPosition)
            }
            onDateSelected.onDateClick(
                date = selectedDate ?: "",
                isCurrentDate = false,
                fullDate = list.getOrNull(position)?.date ?: "",
                position = position,
                isSelected = list.getOrNull(position)?.isSelect ?: false
            )
            notifyItemChanged(position)
            oldSelectedPosition = position
        }

    }

    private fun unSelectOther(position: Int) {
        for (i in list.indices) {
            if (position == i) return
            list[i].isSelect = false
        }
    }

    interface OnDateSelected {
        fun onDateClick(
            date: String,
            isCurrentDate: Boolean,
            fullDate: String,
            position: Int,
            isSelected: Boolean
        )
    }
}
