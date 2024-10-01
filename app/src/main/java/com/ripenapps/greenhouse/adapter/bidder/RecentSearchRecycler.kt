package com.ripenapps.greenhouse.adapter.bidder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.RecentSearchItemBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.RecentSearchModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecentSearchRecycler(
    val arrRecentSearch: ArrayList<RecentSearchModel>,
    private val callBack: (RecentSearchModel,String) -> Unit
) : RecyclerView.Adapter<RecentSearchRecycler.ViewHolder>() {

    inner class ViewHolder(
        private val binding: RecentSearchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RecentSearchModel) {
            binding.apply {
                recentSearchMode = data
                view.setVisibility(absoluteAdapterPosition != arrRecentSearch.size - 1)
                crossIcon.setOnClickListener {
                    callBack.invoke(data,"forDelete")
                }

                itemSearch.setOnClickListener {
                    callBack.invoke(data,"forSearch")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecentSearchItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrRecentSearch.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrRecentSearch.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}