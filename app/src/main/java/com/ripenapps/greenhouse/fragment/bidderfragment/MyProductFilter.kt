package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider.OnChangeListener
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.databinding.FragmentMyProductFilterBinding

class MyProductFilter(
    private val model: FilterModel?,
    private val callBack: (FilterModel?) -> Unit,
    private val highestRange: Float
) : BottomSheetDialogFragment() {
    private lateinit var myProductFilterBinding: FragmentMyProductFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        myProductFilterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_my_product_filter, container, false)

        myProductFilterBinding.highestRangePrice.text =
            getString(R.string.sar) + " " + highestRange.toString()
        myProductFilterBinding.priceSlider.valueTo = highestRange

        initListeners()

        filterModel = model ?: FilterModel()
        chooseFilter()
        return myProductFilterBinding.root
    }

    private fun chooseFilter() {
        myProductFilterBinding.apply {
            priceSlider.value = filterModel?.price?.toFloat() ?: 0f
            timeSlider.value = filterModel?.hours?.toFloat() ?: 0f
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        myProductFilterBinding.priceSlider.addOnChangeListener(OnChangeListener { _, price, _ ->
            myProductFilterBinding.range.text =
                getString(R.string.sar) + " 0 - " + getString(R.string.sar) + " " + "${price.toInt()}"
            filterModel?.price = price.toString()
        })

        myProductFilterBinding.timeSlider.addOnChangeListener(OnChangeListener { _, hours, _ ->
            myProductFilterBinding.timeRange.text = "0-${hours.toInt()} hr."
            filterModel?.hours = hours.toString()
        })

        myProductFilterBinding.clearAllBtn.setOnClickListener {
            filterModel = null
            callBack.invoke(filterModel)
            dismiss()
        }
        myProductFilterBinding.applyBtn.setOnClickListener {
            callBack.invoke(filterModel)
            dismiss()
        }
    }
}
