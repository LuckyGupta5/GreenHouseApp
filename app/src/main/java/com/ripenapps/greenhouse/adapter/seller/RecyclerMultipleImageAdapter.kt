package com.ripenapps.greenhouse.adapter.seller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.MultipleImagesBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMultipleImagesModel

interface ImageDeleteListener {
    fun onDeleteClicked(position: Int)
}
class RecyclerMultipleImageAdapter(
    // Change itemClick to accept a String parameter
    private val deleteListener: ImageDeleteListener,
     val arrMultipleImage: MutableList<SellerMultipleImagesModel>
) : RecyclerView.Adapter<RecyclerMultipleImageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: MultipleImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: SellerMultipleImagesModel, position: Int) {
            binding.apply {
                itemMultipleModel = data
                Glide.with(binding.root.context).load(data.multipleImages)
                    .into(this.img)
                deleteImg.setOnClickListener {
                    deleteListener.onDeleteClicked(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<MultipleImagesBinding>(
            LayoutInflater.from(parent.context),
            R.layout.multiple_images,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrMultipleImage.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrMultipleImage[position],position)
    }
}
