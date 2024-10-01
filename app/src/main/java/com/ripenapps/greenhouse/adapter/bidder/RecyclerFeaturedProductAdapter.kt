package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.dpToPixel
import com.ripenapps.greenhouse.databinding.FeaturedProductsHomeBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.utills.setEndMargin
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setStartMargin
import com.ripenapps.greenhouse.utills.setVisibility

@Suppress("DEPRECATION")
class RecyclerFeaturedProductAdapter(val s: String? = null) :
    RecyclerView.Adapter<RecyclerFeaturedProductAdapter.ViewHolder>() {

    var arrFeaturedProducts: MutableList<FeaturedProductModel>? = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<FeaturedProductModel>) {
        this.arrFeaturedProducts?.clear()
        this.arrFeaturedProducts?.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrFeaturedProducts?.clear()
        notifyDataSetChanged()
    }

    fun updateItem(list: MutableList<FeaturedProductModel>) {
        if (list.isNullOrEmpty() || arrFeaturedProducts.isNullOrEmpty()) return
        arrFeaturedProducts?.addAll(list)
        notifyItemRangeChanged(list.size - list.size, list.size)
    }

    private var nListener: onClickListner? = null

    interface onClickListner {
        fun click(position: Int)
        fun onClickBid(position: Int)
        fun onClickWishlist(position: Int)
    }

    fun onClickListner1(listner: onClickListner) {
        this.nListener = listner
    }

    inner class ViewHolder(
        private val featuredProductBinding: FeaturedProductsHomeBinding,
        private val listener: onClickListner?
    ) : RecyclerView.ViewHolder(featuredProductBinding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: FeaturedProductModel) {

            featuredProductBinding.apply {
                itemModel = data
                if (data.wishlist != true) {
                    this.homeWishlist.setImageDrawable(
                        featuredProductBinding.root.context.resources.getDrawable(
                            R.drawable.icon_wishlist_unselected
                        )
                    )
                } else {
                    this.homeWishlist.setImageDrawable(
                        featuredProductBinding.root.context.resources.getDrawable(
                            R.drawable.ic_wisghlist_green
                        )
                    )
                }

                if (s != "horizontal") {
                    if (absoluteAdapterPosition == 0) {
                        featureProductFix.setStartMargin(R.dimen.dimen_15)
                        featureProductFix.setEndMargin(R.dimen.dimen_15)
                    } else {
                        featureProductFix.setEndMargin(R.dimen.dimen_15)
                    }
                }

                if (data.isFeatured == true) this.featuredTag.setVisibility(true)
                else this.featuredTag.setVisibility(false)
                vegImg.setImage(data.vegetableImg.toString())
                this.featureProductFix.layoutParams.width =
                    featuredProductBinding.root.context.resources.displayMetrics.widthPixels / 2 - featuredProductBinding.root.context.dpToPixel(
                        20
                    )
                this.featureProductFix.setOnClickListener {
                    listener?.click(adapterPosition)
                }

                bidLayout.setOnClickListener {
                    listener?.onClickBid(adapterPosition)
                }
                homeWishlist.setOnClickListener {
                    listener?.onClickWishlist(adapterPosition)
                }

                if (data.isBid == true) {
                    bidLayout.isEnabled = false
                    bidLayout.background =
                        ContextCompat.getDrawable(bidLayout.context, R.drawable.inactive_button_bg)

                } else {
                    bidLayout.isEnabled = true
                    bidLayout.background =
                        ContextCompat.getDrawable(
                            bidLayout.context,
                            R.drawable.bid_button_background
                        )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FeaturedProductsHomeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), nListener
        )
    }

    override fun getItemCount(): Int {
        return arrFeaturedProducts?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrFeaturedProducts?.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}