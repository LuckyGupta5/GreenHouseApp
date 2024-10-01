package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.ImageBannerBinding
import com.ripenapps.greenhouse.utills.setImage

class RecyclerImageSliderAdapter(
    private val context: Context,
    private val arrImage: MutableList<String>
) : RecyclerView.Adapter<RecyclerImageSliderAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ImageBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun bind(data: String, image: Int, total: Int) {
            binding.apply {
//                itemSliderModel = data
                this.image.setImage(data)
                text.text = "$image/$total" // Using string interpolation

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ImageBannerBinding>(
            LayoutInflater.from(parent.context),
            R.layout.image_banner,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrImage.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(arrImage.getOrNull(0) ?: "", position + 1)
        val imageUrl = arrImage[position]
        holder.bind(imageUrl, position + 1, arrImage.size)
    }
}


