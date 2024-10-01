package com.ripenapps.greenhouse.adapter.bidder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.ReviewLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.ReviewModel

class RecyclerReviewAdapter(
    private val arrReviews: ArrayList<ReviewModel>
) : RecyclerView.Adapter<RecyclerReviewAdapter.ViewHolder>() {
    inner class ViewHolder(private val reviewBinding: ReviewLayoutBinding) :
        RecyclerView.ViewHolder(reviewBinding.root) {
        fun bind(data: ReviewModel) {
            reviewBinding.apply {
                reviewModel = data
//                lastView.setVisibility(absoluteAdapterPosition != arrReviews.size - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.review_layout, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrReviews.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrReviews.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}