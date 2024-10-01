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
import com.ripenapps.greenhouse.databinding.FragmentMyOrderFilterBinding
import com.ripenapps.greenhouse.utills.Constants.CANCELLED
import com.ripenapps.greenhouse.utills.Constants.CONSTANTS
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.DELIVERED
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.RETURNED
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES

class MyOrderFilter(
    private val model: FilterModel? = null,
    private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMyOrderFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_my_order_filter, container, false)
        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()
        return binding.root
    }

    private fun initListeners() {

        binding.apply {
            allStatus.setOnClickListener {
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = null
                filterModel?.statusType = "allStatus"
            }

            upcoming.setOnClickListener {
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = CONSTANTS
                filterModel?.statusType = "confirmed"
            }

            delivered.setOnClickListener {
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = DELIVERED
                filterModel?.statusType = "delivered"
            }

            cancelled.setOnClickListener {
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = CANCELLED
                filterModel?.statusType = "cancelled"
            }

            returned.setOnClickListener {
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = RETURNED
                filterModel?.statusType = "returned"
            }

            allCategory.setOnClickListener {
                allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                allCategory.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.category = DATES
                filterModel?.categoryType = "dates"
            }

            resetButton.setOnClickListener {
                filterModel = null

                // Resetting the drawable icons for radio buttons
                allStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                upcoming.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                returned.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allCategory.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
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
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            applyButton.setOnClickListener {
                applyCallBack.invoke(filterModel)
                dismiss()
            }
        }
    }

    private fun chooseFilter() {
        when (filterModel?.categoryType) {
            "allCategory" -> {
                binding.allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                binding.allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                binding.allCategory.setCompoundDrawablesWithIntrinsicBounds(
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
                binding.allCategory.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }
        }

        when (filterModel?.statusType) {
            "allStatus" -> {
                binding.apply {
                    allStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    upcoming.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    returned.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "confirmed" -> {
                binding.apply {
                    upcoming.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    allStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    returned.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "delivered" -> {
                binding.apply {
                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    upcoming.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    allStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    returned.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "returned" -> {
                binding.apply {
                    returned.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    upcoming.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    allStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "cancelled" -> {
                binding.apply {
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )
                    upcoming.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    returned.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    allStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }
        }
    }
}