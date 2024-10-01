package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityProcessToDeliverBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.OrderPlacedSuccessful
import com.ripenapps.greenhouse.fragment.bidderfragment.OrderPlacedUnsuccessful
import com.ripenapps.greenhouse.model.bidder.response.BidderOrderPlaceResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.extractDigits
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.CATEGORY_ID
import com.ripenapps.greenhouse.utills.Constants.CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.BidderOrderPlaceViewModel

class ProcessToDeliver : BaseActivity() {
    private lateinit var binding: ActivityProcessToDeliverBinding
    private var placeOrderViewModel: BidderOrderPlaceViewModel? = null
    private var toPlacedSuccess: OrderPlacedSuccessful? = null
    private var toUnsuccessful: OrderPlacedUnsuccessful? = null
    private var accountBlocked: AccountBlocked? = null
//    private var shippingMethods: ShippingMethod? = null
//    private var filterModel: FilterModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@ProcessToDeliver, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_to_deliver)

        if (Preferences.getStringPreference(this@ProcessToDeliver, "language").equals("ar")) {
            binding.backButton.rotation = 180f
        }

        binding.apply {
            totalAmount.text =
                getString(R.string.sar) + " " + intent.getStringExtra("payableAmount")
            productAmount.text =
                getString(R.string.sar) + " " + intent.getStringExtra("winningAmount")
            vatAmount.text = getString(R.string.sar) + " " + intent.getStringExtra("vatAmount")
            remainingAmountTitle.text =
                getString(R.string.remaining_amount) + " " + intent.getStringExtra("remainingAmount")

//            shippingMethodText.doAfterTextChanged {
//                if (filterModel?.shippingMethod == null) {
//                    btnPlaceOrder.isEnabled = false
//                    btnPlaceOrder.setBackgroundColor(
//                        ContextCompat.getColor(
//                            this@ProcessToDeliver, R.color.inactive_background
//                        )
//                    )
//                } else {
//                    btnPlaceOrder.isEnabled = true
//                    btnPlaceOrder.setBackgroundColor(
//                        ContextCompat.getColor(
//                            this@ProcessToDeliver, R.color.greenhousetheme
//                        )
//                    )
//                }
//            }
        }

        initListeners()
        initViewModel()
    }

    private fun initViewModel() {
        placeOrderViewModel =
            ViewModelProvider(this@ProcessToDeliver)[BidderOrderPlaceViewModel::class.java]
        binding.lifecycleOwner = this@ProcessToDeliver
        placeOrderViewModel?.getBidderOrderPlaceData()?.observe(this@ProcessToDeliver) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderOrderPlaceResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                binding.btnPlaceOrder.isEnabled = true
                                Log.d(
                                    "TAG", "processToDeliverResponse: ${Gson().toJson(data.data)}"
                                )
                                Toast.makeText(
                                    this@ProcessToDeliver, data.message, Toast.LENGTH_SHORT
                                ).show()
                                if (toPlacedSuccess?.isAdded != true) {
                                    toPlacedSuccess =
                                        OrderPlacedSuccessful(data.data?.order?.orderId)
                                    toPlacedSuccess?.show(
                                        supportFragmentManager, toPlacedSuccess?.tag
                                    )
                                    setResult(Activity.RESULT_OK, Intent().apply {
                                        putExtra("status", "update")
                                    })
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@ProcessToDeliver, "token")
                                Preferences.removePreference(this@ProcessToDeliver, "user_details")
                                Preferences.removePreference(this@ProcessToDeliver, "isVerified")
                                Preferences.removePreference(this@ProcessToDeliver, "roomId")
                                val signin = Intent(this@ProcessToDeliver, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            422 -> {
                                Toast.makeText(
                                    this@ProcessToDeliver, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                binding.btnPlaceOrder.isEnabled = true
                                if (toUnsuccessful?.isAdded != true) {
                                    toUnsuccessful = OrderPlacedUnsuccessful()
                                    toUnsuccessful?.show(
                                        supportFragmentManager, toUnsuccessful?.tag
                                    )
                                }
                                Toast.makeText(
                                    this@ProcessToDeliver, it.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this)
                    }

                    Status.ERROR -> {
                        binding.btnPlaceOrder.isEnabled = true
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@ProcessToDeliver, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        binding.apply {
//            shippingMethod.setOnClickListener {
//                if (shippingMethods?.isAdded != true) {
//                    shippingMethods = ShippingMethod(filterModel, ::handelFilter)
//                    shippingMethods?.show(supportFragmentManager, shippingMethods?.tag)
//                }
//            }

            btnPlaceOrder.setOnClickListener {
                btnPlaceOrder.isEnabled = false
                placeOrderApi()
            }
            backButton.setOnClickListener {
                finish()
            }
        }
    }

    private fun placeOrderApi() {
        placeOrderViewModel?.request?.bidId = intent.getStringExtra("bidId")
        placeOrderViewModel?.request?.sellerId = intent.getStringExtra(SELLER_ID)
        placeOrderViewModel?.request?.productId = intent.getStringExtra(Constants.PRODUCT_ID)
        placeOrderViewModel?.request?.token =
            Preferences.getStringPreference(this@ProcessToDeliver, TOKEN)
        placeOrderViewModel?.request?.amount = extractDigits(binding.totalAmount.text.toString())
        placeOrderViewModel?.request?.shippingMethod = "myself"  //filterModel?.shippingMethod
        placeOrderViewModel?.request?.highestBidPrice = intent.getStringExtra("winningAmount")
        placeOrderViewModel?.request?.subCategoryName =
            intent.getStringExtra(Constants.SUB_CATEGORY_NAME)
        placeOrderViewModel?.request?.categoryName = intent.getStringExtra(CATEGORY_NAME)
        placeOrderViewModel?.request?.categoryId = intent.getStringExtra(CATEGORY_ID)
        placeOrderViewModel?.request?.notificationId = intent.getStringExtra("notificationId")
        placeOrderViewModel?.request?.vatAmount = intent.getStringExtra("vatAmount")
        placeOrderViewModel?.getOrderPlaceRequest()
    }

//    private fun handelFilter(model: FilterModel? = null) {
//        Log.d("TAG", "shippingMethodModelFilter: ${Gson().toJson(filterModel)}")
//        this@ProcessToDeliver.filterModel = model
//        setShippingMethod()
//    }

//    private fun setShippingMethod() {
//        when (filterModel?.shippingMethod) {
//            "myself" -> {
//                binding.shippingMethodText.text = getString(R.string.i_will_pick_by_myself)
//            }
//
//            else -> {
//                binding.shippingMethodText.text = getString(R.string.shipping_company_1)
//            }
//        }
//    }
}