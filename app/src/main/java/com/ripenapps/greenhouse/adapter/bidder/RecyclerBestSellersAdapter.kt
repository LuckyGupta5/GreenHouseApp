package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.dpToPixel
import com.ripenapps.greenhouse.databinding.BestSellersBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.BestSellerModel
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.setEndMargin
import com.ripenapps.greenhouse.utills.setStartMargin
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerBestSellersAdapter(val s: String? = null) :
    RecyclerView.Adapter<RecyclerBestSellersAdapter.ViewHolder>() {

    private var nListener: onClickListner? = null
    var arrBestSellers: MutableList<BestSellerModel> = mutableListOf<BestSellerModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(list: MutableList<BestSellerModel>) {
        this.arrBestSellers = list
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        this.arrBestSellers.clear()
        notifyDataSetChanged()
    }

    fun updateItem(list: MutableList<BestSellerModel>) {
        if (list.isNullOrEmpty() || arrBestSellers.isNullOrEmpty()) return
        arrBestSellers.addAll(list)
        notifyItemRangeChanged(list.size - list.size, list.size)
    }

    interface onClickListner {
        fun clickBestSeller(position: Int)
    }

    fun onClickListner2(listner: onClickListner) {
        nListener = listner
    }

    inner class ViewHolder(
        private val bestSellersBinding: BestSellersBinding, private val listener: onClickListner?
    ) : RecyclerView.ViewHolder(bestSellersBinding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: BestSellerModel) {
            bestSellersBinding.apply {
                sellerItemModel = data

                val isVegetable = data.categories?.indexOfFirst { it?.categoryId == VEGETABLES }
                    ?: -1//65c3c6536aa05c59c20219f2
                val isFruit = data.categories?.indexOfFirst { it?.categoryId == FRUITS }
                    ?: -1//65c3c6a06aa05c59c20219f6
                val isDate = data.categories?.indexOfFirst { it?.categoryId == DATES }
                    ?: -1//65c3c6f56aa05c59c20219fa
                this.capsicumImg.setVisibility(isVegetable != -1)
                this.appleImg.setVisibility(isFruit != -1)
                this.datesImg.setVisibility(isDate != -1)
                //this.backgroundImgSeller.backgroundTintList = ContextCompat.getColorStateList(bestSellersBinding.root.context,getColorFromResourceId(contextBestSeller,data.backgroundImage))

                this.bestSellerId.layoutParams.width =
                    bestSellersBinding.root.context.resources.displayMetrics.widthPixels / 2 - bestSellersBinding.root.context.dpToPixel(
                        20
                    )
                if (s != "horizontal") {
                    if (absoluteAdapterPosition == 0) {
                        bestSellerId.setStartMargin(R.dimen.dimen_15)
                        bestSellerId.setEndMargin(R.dimen.dimen_15)
                    } else {
                        bestSellerId.setEndMargin(R.dimen.dimen_15)
                    }
                }
                bestSellerId.setOnClickListener {
                    listener?.let {
                        listener.clickBestSeller(absoluteAdapterPosition)
                    } ?: run {
                        bestSellersBinding.root.context
                    }

                }
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerBestSellersAdapter.ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.best_sellers, parent, false
            ), nListener
        )
    }

    override fun getItemCount(): Int {
        return arrBestSellers.size
    }

    override fun onBindViewHolder(holder: RecyclerBestSellersAdapter.ViewHolder, position: Int) {
        arrBestSellers.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}
