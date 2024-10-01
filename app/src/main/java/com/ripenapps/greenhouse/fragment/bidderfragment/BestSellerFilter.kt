package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.databinding.FragmentBestSellerFilterBinding

class BestSellerFilter(
    private val model: FilterModel? = null, private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var bestSellerFilterBinding: FragmentBestSellerFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bestSellerFilterBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_best_seller_filter, container, false
        )
        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()
        return bestSellerFilterBinding.root
    }

    private fun chooseFilter() {
        bestSellerFilterBinding.apply {
            when (filterModel?.bestSeller) {
                "mostFavourite" -> {
                    mostFav.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }

                "highestRating" -> {
                    highestRating.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }

                "lowestRating" -> {
                    lowesrRating.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }

                "newlyListed" -> {
                    newlyListed.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }

                "mostSelling" -> {
                    mostSelling.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }

                "nearestSeller" -> {
                    nearestSeller.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                }
            }
        }
    }

    private fun initListeners() {
        bestSellerFilterBinding.apply {
            btnMostFav.setOnClickListener {
                filterModel?.bestSeller = "mostFavourite"
                applyCallBack.invoke(filterModel)
                dismiss()
            }
            btnMostSelling.setOnClickListener {
                filterModel?.bestSeller = "mostSelling"
                applyCallBack.invoke(filterModel)
                dismiss()
            }
            btnNearestSeller.setOnClickListener {
                filterModel?.bestSeller = "nearestSeller"
                applyCallBack.invoke(filterModel)
                dismiss()
            }
            btnHighestRating.setOnClickListener {
                filterModel?.bestSeller = "highestRating"
                dismiss()
                applyCallBack.invoke(filterModel)
            }
            btnLowestRating.setOnClickListener {
                filterModel?.bestSeller = "lowestRating"
                dismiss()
                applyCallBack.invoke(filterModel)

            }
            btnNewlyListed.setOnClickListener {
                filterModel?.bestSeller = "newlyListed"
                dismiss()
                applyCallBack.invoke(filterModel)
            }
        }
    }
}