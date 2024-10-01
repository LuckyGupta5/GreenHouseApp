package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.OngoingFeatureBiddingBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.OnGoingFeatureBiddingModel
import com.ripenapps.greenhouse.utills.setImage

class RecyclerOngoingFeatureAdapter :
    RecyclerView.Adapter<RecyclerOngoingFeatureAdapter.ViewHolder>() {
    val arrFeature: MutableList<OnGoingFeatureBiddingModel> = mutableListOf()

    fun addItems(list: MutableList<OnGoingFeatureBiddingModel>) {
        arrFeature.clear()
        arrFeature.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrFeature.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val ongoingFeatureBinding: OngoingFeatureBiddingBinding) :
        RecyclerView.ViewHolder(ongoingFeatureBinding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: OnGoingFeatureBiddingModel) {
            ongoingFeatureBinding.apply {
                ongoingFeatureModel = data
                vegImg.setImage(data.img.toString())
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            OngoingFeatureBiddingBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrFeature.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrFeature.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}