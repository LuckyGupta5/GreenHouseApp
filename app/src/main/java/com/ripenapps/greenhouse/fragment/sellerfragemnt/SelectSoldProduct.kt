package com.ripenapps.greenhouse.fragment.sellerfragemnt

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
import com.ripenapps.greenhouse.adapter.seller.RecyclerSelectSoldProduct
import com.ripenapps.greenhouse.databinding.FragmentSelectSoldProductBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectSoldProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerSoldProductResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.SellerSoldProductViewModel

class SelectSoldProduct(
    private var dataModel: SellerSoldProductResponseModel?,
    private val selectedAddressCallback: (SelectSoldProductModel) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSelectSoldProductBinding
    private val arrSold: ArrayList<SelectSoldProductModel> = arrayListOf()
    private var recyclerSoldAdapter: RecyclerSelectSoldProduct? = null
    private var soldProductViewModel: SellerSoldProductViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_select_sold_product, container, false
        )
        if (dataModel == null) {
            initViewModel()
            soldProductViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
            soldProductViewModel?.soldProductRequest()
        } else {
            handleResponse()
        }
        return binding.root
    }

    private fun initViewModel() {
        soldProductViewModel =
            ViewModelProvider(this@SelectSoldProduct)[SellerSoldProductViewModel::class.java]
        binding.lifecycleOwner = this@SelectSoldProduct
        soldProductViewModel?.soldProductData()?.observe(this@SelectSoldProduct) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerSoldProductResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                dataModel = data
                                handleResponse()
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
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun handleResponse() {
        if (dataModel?.data?.orders?.isEmpty() == true) {
            binding.apply {
                recycler.visibility = View.GONE
                headingPlaceHolder.setVisibility(true)
                placeHolder.setVisibility(true)

            }
            return
        } else {
            binding.apply {
                recycler.visibility = View.VISIBLE
                headingPlaceHolder.setVisibility(false)
                placeHolder.setVisibility(false)
            }
        }
        arrSold.clear()
        Log.d("TAG", "initViewModel: ${Gson().toJson(dataModel?.data?.orders)}")
        dataModel?.data?.orders?.forEachIndexed { _, soldProduct ->
            arrSold.add(
                SelectSoldProductModel(
                    subCategory = soldProduct.productDetails?.subCategory?.enName.toString(),
                    location = soldProduct.productDetails?.productLocation,
                    weight = soldProduct.productDetails?.quantity.toString() + " " + soldProduct.productDetails?.unit + ".",
                    price = getString(R.string.sar) + " " + soldProduct.productDetails?.productPrice.toString(),
                    category = soldProduct.productDetails?.categoryId?.enName ?: "",
                    description = soldProduct.productDetails?.description?.trim(),
                    mobNumber = soldProduct.productDetails?.mobile,
                    quantity = soldProduct.productDetails?.quantity?.toString() ?: "",
                    categoryId = soldProduct.productDetails?.categoryId?.id ?: "",
                    subCategoryId = soldProduct.productDetails?.subCategory?.id,
                    imageUrl = soldProduct.productDetails?.imageUrl,
                    latitude = soldProduct.productDetails?.location?.coordinates?.getOrNull(1),
                    longitude = soldProduct.productDetails?.location?.coordinates?.getOrNull(0)
                )
            )
            initAdapter()
        }
    }

    private fun initAdapter() {
        recyclerSoldAdapter = RecyclerSelectSoldProduct {
            selectedAddressCallback.invoke(it)
            dismiss()
        }
        recyclerSoldAdapter?.addAddress(arrSold)
        binding.recycler.adapter = recyclerSoldAdapter
    }
}