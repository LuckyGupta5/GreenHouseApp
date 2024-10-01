package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerMyOrderFilterBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.utills.Constants

class SellerMyOrderFilter(
    private val model: FilterModel? = null, private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSellerMyOrderFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_my_order_filter, container, false
        )
        isCancelable = true
        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()
        return binding.root
    }

    private fun initListeners() {

        binding.apply {
            all.setOnClickListener {
                all.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )

                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                received.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = null
                filterModel?.statusType = "allStatus"
            }

            delivered.setOnClickListener {
                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )

                all.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                received.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = Constants.DELIVERED
                filterModel?.statusType = "delivered"
            }

            cancelled.setOnClickListener {
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )

                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                received.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                all.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = Constants.CANCELLED
                filterModel?.statusType = "cancelled"
            }

            received.setOnClickListener {
                received.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )

                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                all.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.status = Constants.RECEIVED
                filterModel?.statusType = "received"
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
                filterModel?.category = Constants.VEGETABLES
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
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                filterModel?.category = Constants.FRUITS
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
                filterModel?.category = Constants.DATES
                filterModel?.categoryType = "dates"
            }

            resetButton.setOnClickListener {
                filterModel = null
                // Resetting the drawable icons for radio buttons
                all.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )

                delivered.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                received.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                cancelled.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                allText.setCompoundDrawablesWithIntrinsicBounds(
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
                    all.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )

                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    received.setCompoundDrawablesWithIntrinsicBounds(
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

                    all.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    received.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    cancelled.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }

            "received" -> {
                binding.apply {
                    received.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_selected, 0, 0, 0
                    )

                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    all.setCompoundDrawablesWithIntrinsicBounds(
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

                    delivered.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    received.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                    all.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_radio_button_unselected, 0, 0, 0
                    )
                }
            }
        }
    }

}