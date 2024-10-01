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
import com.ripenapps.greenhouse.databinding.FragmentOrderRecievedBinding
import com.ripenapps.greenhouse.model.bidder.response.CancelOrderResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status

class OrderReceived(
    val orderId: String,
    private val applyCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOrderRecievedBinding
    private var cancelOrderViewModel: CancelOrderViewModel? = null
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_order_recieved, container, false)
        isCancelable = false
        initViewModel()
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.apply {
            yesButton.setOnClickListener {
                cancelOrderViewModel?.request?.orderId = orderId
                cancelOrderViewModel?.request?.type = "receive"
                cancelOrderViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                cancelOrderViewModel?.getCancelOrderRequest()
            }
            goBackButton.setOnClickListener{
                dismiss()
            }
        }
    }

    private fun initViewModel() {
        cancelOrderViewModel =
            ViewModelProvider(this@OrderReceived)[CancelOrderViewModel::class.java]
        binding.lifecycleOwner = this@OrderReceived

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

                        403,401 -> {
                            val signin = Intent(mContext, SignIn::class.java)
                            startActivity(signin)
                            activity?.finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT)
                                .show()
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
}