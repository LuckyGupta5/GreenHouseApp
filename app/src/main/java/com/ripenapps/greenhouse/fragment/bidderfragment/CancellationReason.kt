package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentCancellationReasonBinding
import com.ripenapps.greenhouse.model.bidder.response.CancelOrderResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.CancelOrderViewModel

class CancellationReason(
    val orderId: String,
    val productName: String?,
    private var cancellationCharge: String? = null,
    private val applyCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCancellationReasonBinding
    private var cancelOrderViewModel: CancelOrderViewModel? = null
    private var cancellationReason: String? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_cancellation_reason, container, false
        )

        binding.description.text =
            "${getString(R.string.cancellation_reason_desc1)} $cancellationCharge ${getString(R.string.cancellation_reason_desc2)}"
        isCancelable = false
        initListener()
        initViewModel()
        return binding.root
    }

    private fun initViewModel() {
        cancelOrderViewModel =
            ViewModelProvider(this@CancellationReason)[CancelOrderViewModel::class.java]
        binding.lifecycleOwner = this@CancellationReason
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
                            Log.d("TAG", "cancelOrderApiResponse" + Gson().toJson(data))
                            dismiss()
                            applyCallBack.invoke()
                        }

                        403, 401 -> {
                            Preferences.removePreference(mContext, "token")
                            Preferences.removePreference(mContext, "user_details")
                            Preferences.removePreference(mContext, "isVerified")
                            Preferences.removePreference(mContext, "roomId")
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
                yesButton.isEnabled = false
                Handler().postDelayed({
                    yesButton.isEnabled = true
                }, 2000)
                Log.d("TAG", "cancelReason: $cancellationReason")
                cancelOrderViewModel?.request?.cancelReason = cancellationReason
                cancelOrderViewModel?.request?.orderId = orderId
                cancelOrderViewModel?.request?.type = "cancel"
                cancelOrderViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                cancelOrderViewModel?.request?.subCategoryName = productName
                cancelOrderViewModel?.getCancelOrderRequest()
            }

            reason1.setOnClickListener {
                reasonLayer.setVisibility(false)
                cancellationReason = reason1.text.toString().trim()
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
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason2.setOnClickListener {
                reasonLayer.setVisibility(false)
                cancellationReason = reason2.text.toString().trim()
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
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason3.setOnClickListener {
                reasonLayer.setVisibility(false)
                cancellationReason = reason3.text.toString().trim()
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
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            reason4.setOnClickListener {
                reasonLayer.setVisibility(false)
                cancellationReason = reason4.text.toString().trim()
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
                otherReason.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
            }

            otherReason.setOnClickListener {
                reasonLayer.setVisibility(true)
                reasonEditTextField.doAfterTextChanged {
                    cancellationReason = reasonEditTextField.getText().toString().trim()
                }
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
            }
        }
    }
}