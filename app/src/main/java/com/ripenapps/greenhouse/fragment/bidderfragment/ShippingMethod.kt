package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.databinding.FragmentShippingMethodBinding

class ShippingMethod(
    private val model: FilterModel? = null,
    private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentShippingMethodBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext:Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_shipping_method, container, false)
        // Inflate the layout for this fragment

        filterModel = model ?: FilterModel()
        initListeners()
        chooseFilter()
        return binding.root
    }

    private fun chooseFilter() {
        when (filterModel?.shippingMethod) {
            "myself" -> {
                binding.apply {
                    pickUpMyself.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_radio_button_selected, 0
                    )
                    shippingCompany1.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_radio_button_unselected, 0
                    )
                }
            }

            "shippingCompany"->{
                binding.apply {
                    shippingCompany1.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_radio_button_selected, 0
                    )
                    pickUpMyself.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_radio_button_unselected, 0
                    )
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            pickUpMyself.setOnClickListener {
                pickUpMyself.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_radio_button_selected, 0
                )
                shippingCompany1.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_radio_button_unselected, 0
                )
                filterModel?.shippingMethod = "myself"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            shippingCompany1.setOnClickListener {
                Toast.makeText(mContext, "Under Development", Toast.LENGTH_SHORT).show()

//                shippingCompany1.setCompoundDrawablesWithIntrinsicBounds(
//                    0, 0, R.drawable.ic_radio_button_selected, 0
//                )
//                pickUpMyself.setCompoundDrawablesWithIntrinsicBounds(
//                    0, 0, R.drawable.ic_radio_button_unselected, 0
//                )
//                filterModel?.shippingMethod = "shippingCompany"
//                applyCallBack.invoke(filterModel)
//                dismiss()
            }
        }
    }
}