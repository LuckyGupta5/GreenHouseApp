package com.ripenapps.greenhouse.adapter.seller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.SellerFeaturedProductsDetailsBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.HighestBiddingListModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.setBottomMargin
import com.ripenapps.greenhouse.utills.setStartMargin
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerHighestBidListAdapter(
    private val arrHighestBidList: ArrayList<HighestBiddingListModel>,
    val callback: (HighestBiddingListModel) -> Unit
) : RecyclerView.Adapter<RecyclerHighestBidListAdapter.ViewHolder>() {
    private var loginData: LoginResponseModel.LoginData? = null
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerHighestBidListAdapter.ViewHolder {
        val binding: SellerFeaturedProductsDetailsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.seller_featured_products_details,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerHighestBidListAdapter.ViewHolder, position: Int) {
        holder.bind(arrHighestBidList[position], position)
    }


    override fun getItemCount(): Int {
        return arrHighestBidList.size
    }

    inner class ViewHolder(private val highestListBinding: SellerFeaturedProductsDetailsBinding) :
        RecyclerView.ViewHolder(highestListBinding.root) {
        fun bind(data: HighestBiddingListModel, position: Int) {
            highestListBinding.featuredBidding = data
            loginData = Gson().fromJson(
                Preferences.getStringPreference(highestListBinding.root.context, "user_details"),
                LoginResponseModel.LoginData::class.java
            )
            if (data.userId == loginData?.userDetails?.id) {
                highestListBinding.detailLayer.setBackgroundResource(R.drawable.light_green_bg)
            }

            highestListBinding.highestBid.setOnClickListener {
                callback.invoke(data)
            }

            if (position == arrHighestBidList.size - 1) {
                highestListBinding.highestBid.setBottomMargin(R.dimen.dimen_15)
            }

            when (position) {
                0 -> {
                    highestListBinding.apply {
                        if (arrHighestBidList.size == 1) {
                            highestBid.setBackgroundResource(R.drawable.bg_bottom_view)
                        } else {
                            highestBid.setBackgroundResource(R.drawable.bg_left_right_corners)
                        }
                        if (data.userId == loginData?.userDetails?.id) {
                            highestListBinding.detailLayer.setBackgroundResource(R.drawable.light_green_bg)
                        }
                        idNameCard.setBackgroundResource(R.drawable.bg_position_1)
                        idNameCard.setStartMargin(R.dimen.dimen_15)
                    }
                }

                1 -> {
                    highestListBinding.apply {
                        if (arrHighestBidList.size == 2) {
                            highestBid.setBackgroundResource(R.drawable.bg_bottom_view)
                        } else {
                            highestBid.setBackgroundResource(R.drawable.bg_left_right_corners)
                        }
                        if (data.userId == loginData?.userDetails?.id) {
                            highestListBinding.detailLayer.setBackgroundResource(R.drawable.light_green_bg)
                        }
                        idNameCard.setBackgroundResource(R.drawable.bg_position_2)
                        idNameCard.setStartMargin(R.dimen.dimen_15)
                    }
                }

                2 -> {
                    highestListBinding.apply {
                        highestBid.setBackgroundResource(R.drawable.bg_bottom_view)
//                        if (arrHighestBidList.size == 3) {
//                            highestBid.setBackgroundResource(R.drawable.bg_bottom_view)
//                        } else {
//                            highestBid.setBackgroundResource(R.drawable.bg_left_right_corners)
//                        }
                        if (data.userId == loginData?.userDetails?.id) {
                            highestListBinding.detailLayer.setBackgroundResource(R.drawable.light_green_bg)
                        }
                        idNameCard.setBackgroundResource(R.drawable.bg_position_3)
                        idNameCard.setStartMargin(R.dimen.dimen_15)
                        view.setVisibility(false)
                    }
                }

                else -> {
                    highestListBinding.apply {
                        idNameCard.setBackgroundResource(R.drawable.bg_highest_bid_position)
                    }
                }
            }
        }
    }
}

