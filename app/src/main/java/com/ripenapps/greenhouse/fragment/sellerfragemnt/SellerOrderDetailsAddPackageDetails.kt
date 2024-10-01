package com.ripenapps.greenhouse.fragment.sellerfragemnt

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
import com.ripenapps.greenhouse.view_models.SellerAddOrderPackedViewModel
import com.ripenapps.greenhouse.databinding.FragmentSellerOrderDetailsAddPackageDetailsBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.SellerAddOrderPackedResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.showToast

class SellerOrderDetailsAddPackageDetails(
    private val orderID: String?, private val onSaveCallback: (Float, Float, Float, Float) -> Unit
) : BottomSheetDialogFragment() {
    private var orderAddPacked: SellerAddOrderPackedViewModel? = null
    private lateinit var sellerOrderDetailsAddPackageDetailsBinding: FragmentSellerOrderDetailsAddPackageDetailsBinding
    var accountBlocked: AccountBlocked? = null
//    override fun getTheme(): Int {
//        return R.style.BottomSheetDialogTheme
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        sellerOrderDetailsAddPackageDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_order_details_add_package_details, container, false
        )
        initListeners()
        Log.d("TAG", "onCreateView: " + orderAddPacked?.request?.orderId)
        initViewModel()
        return sellerOrderDetailsAddPackageDetailsBinding.root
    }


    private fun initViewModel() {
        orderAddPacked = ViewModelProvider(this)[SellerAddOrderPackedViewModel::class.java]
        sellerOrderDetailsAddPackageDetailsBinding.lifecycleOwner = this
        orderAddPacked?.getAddBidData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerAddOrderPackedResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                onSaveCallback.invoke(
                                    sellerOrderDetailsAddPackageDetailsBinding.lengthCount.text.toString()
                                        .toFloat(),
                                    sellerOrderDetailsAddPackageDetailsBinding.widthCount.text.toString()
                                        .toFloat(),
                                    sellerOrderDetailsAddPackageDetailsBinding.heightCount.text.toString()
                                        .toFloat(),
                                    sellerOrderDetailsAddPackageDetailsBinding.boxCountNumber.text.toString()
                                        .toFloat()
                                )
                                dismiss()
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                val signin = Intent(requireContext(), SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            422 -> {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(requireContext())
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        sellerOrderDetailsAddPackageDetailsBinding.btnSave.setOnClickListener {
            if (sellerOrderDetailsAddPackageDetailsBinding.boxCountNumber.text.toString()
                    .isNotEmpty() && sellerOrderDetailsAddPackageDetailsBinding.heightCount.text.toString()
                    .isNotEmpty() && sellerOrderDetailsAddPackageDetailsBinding.widthCount.text.toString()
                    .isNotEmpty() && sellerOrderDetailsAddPackageDetailsBinding.lengthCount.text.toString()
                    .isNotEmpty()
            ) {
                orderAddPacked?.request?.orderId = orderID
                orderAddPacked?.request?.token =
                    Preferences.getStringPreference(requireContext(), Constants.TOKEN)
                orderAddPacked?.request?.boxesNumber =
                    sellerOrderDetailsAddPackageDetailsBinding.boxCountNumber.text.toString()
                orderAddPacked?.request?.boxHeight =
                    sellerOrderDetailsAddPackageDetailsBinding.heightCount.text.toString()
                orderAddPacked?.request?.boxWidth =
                    sellerOrderDetailsAddPackageDetailsBinding.widthCount.text.toString()
                orderAddPacked?.request?.boxLength =
                    sellerOrderDetailsAddPackageDetailsBinding.lengthCount.text.toString()
                orderAddPacked?.getAddOrderPackedRequest()
            } else {
                showToast("Please fill all the fields")
            }
        }
    }
}