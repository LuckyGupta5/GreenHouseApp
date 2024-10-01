package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerMyBidsFilterBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerProductListFilterModel
import com.ripenapps.greenhouse.utills.Constants

class SellerMyBidsFilter(
    private var sellerFilterModel: SellerProductListFilterModel?,
    private val applyCallBack: (SellerProductListFilterModel?) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSellerMyBidsFilterBinding
    var filterModel: SellerProductListFilterModel? = SellerProductListFilterModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_my_bids_filter, container, false
        )
        initListeners()
        filterModel = sellerFilterModel ?: SellerProductListFilterModel()
        Log.d("SellerProductFilter", "Initialized filterModel: $filterModel")
        chooseFilter()
        binding.apply {
            timeSlider.value = filterModel?.hours?.toFloat() ?: 0f
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        binding.resetButton.setOnClickListener {
            filterModel = null
            // Resetting the drawable icons for radio buttons
            with(binding) {
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
            }
            dismiss()
            applyCallBack.invoke(filterModel)
        }

        binding.applyButton.setOnClickListener {
            applyCallBack.invoke(filterModel)
            dismiss()
        }
        binding.timeSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            binding.timeRange.text = "0 - ${value.toInt()} hr."
            filterModel?.hours = value.toString()
        })


        binding.apply {
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
                filterModel?.categoryType = "all"
                filterModel?.category = null
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
                filterModel?.categoryType = "vegetable"
                filterModel?.category = "Vegetables"
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
                filterModel?.categoryType = "fruits"
                filterModel?.category = Constants.FRUITSMYBID
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
                filterModel?.categoryType = "dates"
                filterModel?.category = Constants.DATESMYBID
            }
        }
    }

    private fun chooseFilter() {
        when (filterModel?.categoryType) {
            "all" -> {
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

            "fruits" -> {
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
    }

}