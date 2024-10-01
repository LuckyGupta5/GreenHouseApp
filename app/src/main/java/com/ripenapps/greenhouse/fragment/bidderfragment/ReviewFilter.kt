package com.ripenapps.greenhouse.fragment.bidderfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentReviewFilterBinding
import com.ripenapps.greenhouse.datamodels.FilterModel

class ReviewFilter(
    private val model: FilterModel? = null, private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentReviewFilterBinding
    private var filterModel: FilterModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_review_filter, container, false)

        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()

        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            lowestRating.setOnClickListener {
                lowestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.reviewFiler = "asc"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            highestRating.setOnClickListener {
                highestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.reviewFiler = "desc"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            btnAll.setOnClickListener {
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.reviewFiler = null
                applyCallBack.invoke(filterModel)
                dismiss()
            }
        }
    }

    private fun chooseFilter() {
        binding.apply {
            when (filterModel?.reviewFiler) {
                "asc" -> {
                    lowestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    highestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                "desc" -> {
                    highestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    lowestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                else -> {
                    allText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    lowestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    highestRatingText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }
            }
        }
    }
}