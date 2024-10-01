package com.ripenapps.greenhouse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.OnBoardingFragmentBinding
import com.ripenapps.greenhouse.datamodels.ViewPagerItem

class OnBoardingViewPagerAdapter(
    private val images: ArrayList<ViewPagerItem>
) : RecyclerView.Adapter<OnBoardingViewPagerAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: OnBoardingFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dataModel: ViewPagerItem) {
            binding.apply {
                onBoardingModel = dataModel
                mobileImage.setImageResource(dataModel.imageId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.on_boarding_fragment, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        images.getOrNull(position).let { dataModel ->
            if (dataModel != null) {
                holder.bind(dataModel)
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

}