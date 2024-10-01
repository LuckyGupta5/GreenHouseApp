package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider.OnChangeListener
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerMyProductFilterBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerProductListFilterModel
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES

class SellerProductFilter() : BottomSheetDialogFragment() {
     var applyCallBack: ((SellerProductListFilterModel?) -> Unit?)? =null
    private lateinit var sellerProductFilterBinding: FragmentSellerMyProductFilterBinding
    private var filterModel: SellerProductListFilterModel? = SellerProductListFilterModel()
    private var toSellerProductFilterAddNewAddress: SellerSelectAddress? = null

    companion object {
        fun newInstance(sellerFilterData: SellerProductListFilterModel?, applyCallBackClick: (SellerProductListFilterModel?) -> Unit): SellerProductFilter {
            val fragment = SellerProductFilter().apply {
                filterModel = sellerFilterData ?: SellerProductListFilterModel()
                applyCallBack = applyCallBackClick
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        sellerProductFilterBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_my_product_filter, container, false
        )

        initListeners()

        chooseFilter()
        return sellerProductFilterBinding.root
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        sellerProductFilterBinding.resetButton.setOnClickListener {
            filterModel = null
            // Resetting the drawable icons for radio buttons
            with(sellerProductFilterBinding) {
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
            applyCallBack?.invoke(filterModel)
            dismiss()
        }

        sellerProductFilterBinding.applyButton.setOnClickListener {
            if (filterModel?.address?.isNotEmpty() == true) {
                sellerProductFilterBinding.idAddress.text = filterModel?.address
            }
            filterModel?.let { it1 -> applyCallBack?.invoke(it1) }
            dismiss()
        }
        sellerProductFilterBinding.timeSlider.addOnChangeListener(OnChangeListener { _, value, _ ->
            sellerProductFilterBinding.timeRange.text = "0 - ${value.toInt()} hr."
            filterModel?.hours = value.toString()
        })

        sellerProductFilterBinding.selectAddress.setOnClickListener {
            if (toSellerProductFilterAddNewAddress?.isAdded != true) {
                toSellerProductFilterAddNewAddress = SellerSelectAddress.newInstance {
                    Log.d("TAG", "initListeners: ${Gson().toJson(it)}")
                    filterModel?.address = it.userAddress
                    filterModel?.latitude = it.lat
                    filterModel?.longitude = it.long
                    sellerProductFilterBinding.idAddress.text = it.userAddress
                }
                toSellerProductFilterAddNewAddress?.show(
                    parentFragmentManager, toSellerProductFilterAddNewAddress?.tag
                )
            }
        }

        sellerProductFilterBinding.apply {
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
                filterModel?.category = VEGETABLES
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
                filterModel?.category = FRUITS
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
                filterModel?.category = DATES
            }
        }
    }

    private fun chooseFilter() {
        sellerProductFilterBinding.idAddress.text =
            filterModel?.address ?: getString(R.string.select_address)
        when (filterModel?.categoryType) {
            "all" -> {
                sellerProductFilterBinding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                sellerProductFilterBinding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "vegetable" -> {
                sellerProductFilterBinding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                sellerProductFilterBinding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "fruits" -> {
                sellerProductFilterBinding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                sellerProductFilterBinding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            "dates" -> {
                sellerProductFilterBinding.datesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                sellerProductFilterBinding.vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                sellerProductFilterBinding.allText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

        }

        sellerProductFilterBinding.apply {
            timeSlider.value = filterModel?.hours?.toFloat() ?: 0f
        }
    }
}