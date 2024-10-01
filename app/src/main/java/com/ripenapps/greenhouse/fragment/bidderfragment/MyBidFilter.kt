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
import com.ripenapps.greenhouse.databinding.FragmentMyBidFilterBinding
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.LOST_STATUS
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.Constants.WON_STATUS

class MyBidFilter(
    private val model: FilterModel?, private val callBack: (FilterModel?) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMyBidFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_my_bid_filter, container, false)

        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun initListeners() {
        binding.apply {
            resetButton.setOnClickListener {
                filterModel = null

                allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                callBack.invoke(filterModel)
                dismiss()
            }
            applyButton.setOnClickListener {
                callBack.invoke(filterModel)
                dismiss()
            }

            allAuctionText.setOnClickListener {
                allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = null
                filterModel?.statusType = "allStatus"
            }

            wonAuctionText.setOnClickListener {
                wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = WON_STATUS
                filterModel?.statusType = "wonStatus"
            }

            lostAuctionText.setOnClickListener {
                lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = LOST_STATUS
                filterModel?.statusType = "lossStatus"
            }

            allText.setOnClickListener {
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )

                filterModel?.category = null
                filterModel?.categoryType = "allCategory"
            }

            vegetableText.setOnClickListener {
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.category = VEGETABLES
                filterModel?.categoryType = "vegetable"
            }

            fruitesText.setOnClickListener {
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.category = FRUITS
                filterModel?.categoryType = "fruites"
            }

            datesText.setOnClickListener {
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.category = DATES
                filterModel?.categoryType = "dates"
            }
        }
    }

    private fun chooseFilter() {
        when (filterModel?.categoryType) {
            "allCategory" -> {
                binding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                binding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "vegetable" -> {
                binding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                binding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "fruites" -> {
                binding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                binding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "dates" -> {
                binding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                binding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                binding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }
        }

        when (filterModel?.statusType) {
            "allStatus" -> {
                binding.apply {
                    allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "wonStatus" -> {
                binding.apply {
                    wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "lossStatus" -> {
                binding.apply {
                    lostAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    allAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    wonAuctionText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }
        }
    }
}