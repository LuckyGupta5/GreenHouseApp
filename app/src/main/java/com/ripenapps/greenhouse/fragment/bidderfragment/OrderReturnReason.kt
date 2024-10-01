package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.view_models.CancelOrderViewModel
import com.ripenapps.greenhouse.databinding.FragmentOrderReturnReasonBinding
import com.ripenapps.greenhouse.model.bidder.response.CancelOrderResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility

class OrderReturnReason(
    val orderId: String, private val applyCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOrderReturnReasonBinding
    private var cancelOrderViewModel: CancelOrderViewModel? = null
    private var returnReason: String? = null
    private lateinit var mContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_order_return_reason, container, false
        )
        initListener()
        initViewModel()
        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun initViewModel() {
        cancelOrderViewModel =
            ViewModelProvider(this@OrderReturnReason)[CancelOrderViewModel::class.java]
        binding.lifecycleOwner = this@OrderReturnReason

        cancelOrderViewModel?.getCancelOrderData()?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        CancelOrderResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "returnOrderApiResponse" + Gson().toJson(data))
                            dismiss()
                            applyCallBack.invoke()
                        }

                        403,401 -> {
                            val signin = Intent(mContext, SignIn::class.java)
                            startActivity(signin)
                            activity?.finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(mContext)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun initListener() {
        binding.apply {
            goBackButton.setOnClickListener {
                dismiss()
            }
            yesButton.setOnClickListener {
                Log.d("TAG", "cancelReason: $returnReason")
                cancelOrderViewModel?.request?.returnReason = returnReason
                cancelOrderViewModel?.request?.orderId = orderId
                cancelOrderViewModel?.request?.type = "return"
                cancelOrderViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                cancelOrderViewModel?.getCancelOrderRequest()
            }

            reason1.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason1.text.toString()
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason2.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason2.text.toString()
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason3.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason3.text.toString()
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason4.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason4.text.toString()
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason5.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason5.text.toString()
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason6.setOnClickListener {
                reasonLayer.setVisibility(false)
                returnReason = reason6.text.toString()
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            otherReason.setOnClickListener {
                reasonLayer.setVisibility(true)
                returnReason = reasonEditTextField.text.toString()
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                reason2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason4.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason5.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                reason6.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }
        }
    }
}