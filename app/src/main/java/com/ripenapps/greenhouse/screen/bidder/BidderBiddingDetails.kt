package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityBidderBiddingDetailsBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.BidderBidDetailsResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetRoomIdResponse
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.CommonUtils.startTimer
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeftWithoutSecond
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CATEGORY_ID
import com.ripenapps.greenhouse.utills.Constants.CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidderBidDetailsViewModel
import com.ripenapps.greenhouse.view_models.RejectOrderViewModel

class BidderBiddingDetails : BaseActivity() {
    private lateinit var binding: ActivityBidderBiddingDetailsBinding
    private var bidderBidDetailViewModel: BidderBidDetailsViewModel? = null
    private var sellerId: String? = null
    private var accountBlocked: AccountBlocked? = null
    private var productId: String? = null
    private var orderId: String? = null
    private var bidId: String? = null
    private var vatAmount: String? = null
    private var payableAmount: String? = null
    private var winningAmount: String? = null
    private var remainingAmount: String? = null
    private var productPrice: String? = null
    private var categoryName: String? = null
    private var categoryId: String? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>
    private var rejectOrderViewModel: RejectOrderViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@BidderBiddingDetails, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bidder_bidding_details)

        if (Preferences.getStringPreference(this@BidderBiddingDetails, "language") == "ar") {
            binding.backButton.rotation = 180f
            binding.arrow.rotation = 180f
        }

        initListeners()
        initViewModel()
        biddingDetailsApi()
        pullToRefresh()
        initStartActivityInstance()
        initRejectOrderViewModel()
    }

    private fun initRejectOrderViewModel() {
        rejectOrderViewModel =
            ViewModelProvider(this@BidderBiddingDetails)[RejectOrderViewModel::class.java]
        binding.lifecycleOwner = this@BidderBiddingDetails
        rejectOrderViewModel?.getRejectOrderData()?.observe(this@BidderBiddingDetails) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetRoomIdResponse::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "bidHistory: ${Gson().toJson(data.result)}")
                                finish()
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
                                Preferences.removePreference(this@BidderBiddingDetails, "token")
                                Preferences.removePreference(
                                    this@BidderBiddingDetails,
                                    "user_details"
                                )
                                Preferences.removePreference(
                                    this@BidderBiddingDetails,
                                    "isVerified"
                                )
                                Preferences.removePreference(this@BidderBiddingDetails, "roomId")
                                val signin = Intent(this@BidderBiddingDetails, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@BidderBiddingDetails, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@BidderBiddingDetails)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@BidderBiddingDetails, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        biddingDetailsApi()
                    }
                }
            }
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            biddingDetailsApi()
        }
    }

    private fun biddingDetailsApi() {
        bidderBidDetailViewModel?.request?.bidId = intent.getStringExtra("bidId")
        bidderBidDetailViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderBiddingDetails, TOKEN)
        bidderBidDetailViewModel?.getBidderBidDeatilRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        bidderBidDetailViewModel =
            ViewModelProvider(this@BidderBiddingDetails)[BidderBidDetailsViewModel::class.java]
        binding.lifecycleOwner = this@BidderBiddingDetails
        bidderBidDetailViewModel?.getBidderBidDeatilData()?.observe(this@BidderBiddingDetails) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.bidingDetailShimmer.biddingDetailMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderBidDetailsResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG",
                                    "BidderBiddingDetailsResponse ${Gson().toJson(data.data)}"
                                )
                                binding.idRefreshLayout.isRefreshing = false
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("status", "update")
                                })
                                binding.apply {
                                    categoryName = data.data?.product?.categoryId?.enName
                                    categoryId = data.data?.product?.categoryId?.id
                                    when (data.data?.bid?.status) {
                                        "filled" -> {
                                            when (data.data.bid.bidStatus) {
                                                "won" -> {
                                                    biddingStatusBg.setBackgroundResource(R.drawable.bg_bidding_deails_zig)
                                                    rejectButton.setVisibility(false)
                                                    if (startTimer(
                                                            binding.timer,
                                                            data.data.product?.orderTimer
                                                        ) == 0
                                                    ) {
                                                        startTimer(
                                                            binding.timer,
                                                            data.data.product?.secondOrderTimer
                                                        )
                                                    } else {
                                                        startTimer(
                                                            binding.timer,
                                                            data.data.product?.orderTimer
                                                        )
                                                    }

                                                    biddingStatusText.text =
                                                        getString(R.string.congrats_won_auction)

                                                    if (data.data.bid.orderPlaced == true) {
                                                        buttonProcessToDeliver.setVisibility(false)
                                                        orderDetailButton.setVisibility(true)
                                                        wonBidDesc.setVisibility(false)
                                                        timeRemainingLayer.setVisibility(false)
                                                        linearLayer2.setVisibility(true)
                                                        timeLeftLayer.setVisibility(false)
                                                    } else {
                                                        buttonProcessToDeliver.setVisibility(true)
                                                        orderDetailButton.setVisibility(false)
                                                        timeRemainingLayer.setVisibility(true)
                                                        linearLayer2.setVisibility(false)
                                                        wonBidDesc.setVisibility(true)
                                                        shippingMethodLayer.setVisibility(false)
                                                    }
                                                    winningBidAmount.text =
                                                        getString(R.string.sar) + " " + data.data.bid.myBidAmount
                                                }

                                                "pending" -> {
                                                    rejectButton.setVisibility(false)
                                                    biddingStatusBg.setBackgroundResource(R.drawable.yellow_zig_zac)
                                                    biddingStatusText.text =
                                                        getString(R.string.your_bidding_in_progress)
                                                    buttonProcessToDeliver.setVisibility(false)
                                                    orderDetailButton.setVisibility(true)
                                                    orderDetailButton.text =
                                                        getString(R.string.view_product_detail)
                                                    bidTitle.text = getString(R.string.highest_bid)
                                                    timeRemainingLayer.setVisibility(false)
                                                    wonBidDesc.setVisibility(false)
                                                    shippingMethodLayer.setVisibility(false)
                                                    winningBidAmount.text =
                                                        getString(R.string.sar) + " " + data.data.bid.highestBidAmount
                                                }

                                                "missed" -> {
                                                    rejectButton.setVisibility(false)
                                                    biddingStatusBg.setBackgroundResource(R.drawable.red_zig_zac)
                                                    biddingStatusText.text =
                                                        getString(R.string.oops_you_lost_this_auction)
                                                    buttonProcessToDeliver.setVisibility(false)
                                                    orderDetailButton.setVisibility(false)
                                                    timeLeftLayer.setVisibility(false)
                                                    lastLayer.setVisibility(false)
                                                    missed.setVisibility(true)
                                                    missedDesc.setVisibility(true)
                                                    shippingMethodLayer.setVisibility(false)
                                                    wonBidDesc.setVisibility(false)
                                                    timeRemainingLayer.setVisibility(false)
                                                }

                                                "loss" -> {
                                                    lastLayer.setVisibility(false)
                                                    rejectButton.setVisibility(false)
                                                    biddingStatusBg.setBackgroundResource(R.drawable.red_zig_zac)
                                                    biddingStatusText.text =
                                                        getString(R.string.oops_you_lost_this_auction)
                                                    buttonProcessToDeliver.setVisibility(false)
                                                    orderDetailButton.setVisibility(false)
                                                    timeRemainingLayer.setVisibility(false)
                                                    shippingMethodLayer.setVisibility(false)
                                                    timeLeftLayer.setVisibility(false)
                                                    winningBidAmount.text =
                                                        getString(R.string.sar) + " " + data.data.bid.highestBidAmount
                                                }
                                            }
                                        }

                                        "unfilled" -> {
                                            biddingStatusBg.setBackgroundResource(R.drawable.blue_zig_zac)
                                            biddingStatusText.text =
                                                getString(R.string.unfilled_seller_request)
                                            buttonProcessToDeliver.text =
                                                getString(R.string.accept)
                                            rejectButton.setVisibility(true)
                                            bidTitle.setVisibility(false)
                                            winningBidAmount.setVisibility(false)
                                            viewEBEBEB.setVisibility(false)
                                            linearLayer2.setVisibility(false)
                                        }
                                    }

                                    shippingMethod.text = data.data?.bid?.shippingMethod
                                        ?: getString(R.string.i_will_pick_by_myself)
                                    timeLeft.text = timeLeftWithoutSecond(
                                        data.data?.product?.endDate.toString(),
                                        this@BidderBiddingDetails
                                    )
                                    vegImg.setImage(data.data?.product?.imageUrl.toString())
                                    vegText.text = data.data?.product?.subCategory?.enName
                                    location.text = data.data?.product?.productLocation
                                    money.text = data.data?.product?.productPrice.toString()
                                    sellerUserName.text = data.data?.product?.userName
                                    sellerInitials.text =
                                        getInitials(data.data?.product?.name.toString())
                                    yourBidAmount.text =
                                        getString(R.string.sar) + " " + data.data?.bid?.myBidAmount.toString()
                                    yourQuantity.text =
                                        "${data.data?.product?.quantity} ${data.data?.product?.unit}"
                                    sellerId = data.data?.product?.sellerId
                                    productId = data.data?.product?.id
                                    orderId = data.data?.bid?.orderId
                                    bidId = data.data?.bid?.id

                                    productPrice = data.data?.product?.productPrice.toString()
                                    winningAmount = data.data?.bid?.myBidAmount.toString()
                                    payableAmount = data.data?.bid?.payableAmount.toString()
                                    vatAmount = data.data?.bid?.vatAmount.toString()
                                    remainingAmount = data.data?.bid?.remainingAmount
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
                                Preferences.removePreference(this@BidderBiddingDetails, "token")
                                Preferences.removePreference(
                                    this@BidderBiddingDetails,
                                    "user_details"
                                )
                                Preferences.removePreference(
                                    this@BidderBiddingDetails,
                                    "isVerified"
                                )
                                Preferences.removePreference(this@BidderBiddingDetails, "roomId")
                                val signin = Intent(this@BidderBiddingDetails, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.bidingDetailShimmer.biddingDetailMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@BidderBiddingDetails, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.bidingDetailShimmer.biddingDetailMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.bidingDetailShimmer.biddingDetailMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        Toast.makeText(this@BidderBiddingDetails, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        binding.apply {

            orderDetailButton.setOnClickListener {
                if (orderDetailButton.text == getString(R.string.view_order_details)) {
                    val toOrderDetail =
                        Intent(this@BidderBiddingDetails, BidderOrderDetailScreen::class.java)
                    toOrderDetail.putExtra(ORDER_ID, orderId)
                    startActivity(toOrderDetail)
                } else {
                    val toProductDetail =
                        Intent(this@BidderBiddingDetails, BidderProductDetails::class.java)
                    toProductDetail.putExtra(SELLER_ID, sellerId)
                    toProductDetail.putExtra(PRODUCT_ID, productId)
                    startActivity(toProductDetail)
                }
            }
            sellerDetailLayer.setOnClickListener {
                val toAboutSeller = Intent(this@BidderBiddingDetails, AboutSeller::class.java)
                toAboutSeller.putExtra(SELLER_ID, sellerId)
                startActivity(toAboutSeller)
            }
            backButton.setOnClickListener {
                finish()
            }
            buttonProcessToDeliver.setOnClickListener {
                val toProcessToDeliver =
                    Intent(this@BidderBiddingDetails, ProcessToDeliver::class.java)
                if (buttonProcessToDeliver.text == getString(R.string.accept)) {
                    toProcessToDeliver.putExtra("bidId", bidId)
                }
                toProcessToDeliver.putExtra(SELLER_ID, sellerId)
                toProcessToDeliver.putExtra(PRODUCT_ID, productId)
                toProcessToDeliver.putExtra("productPrice", productPrice)
                toProcessToDeliver.putExtra("winningAmount", winningAmount) //mybidAmount
                toProcessToDeliver.putExtra("vatAmount", vatAmount)
                toProcessToDeliver.putExtra("payableAmount", payableAmount)
                toProcessToDeliver.putExtra("remainingAmount", remainingAmount)
                toProcessToDeliver.putExtra(SUB_CATEGORY_NAME, vegText.text)
                toProcessToDeliver.putExtra(CATEGORY_NAME, categoryName)
                toProcessToDeliver.putExtra(CATEGORY_ID, categoryId)
                getContent.launch(toProcessToDeliver)
            }

            rejectButton.setOnClickListener {
                rejectOrderViewModel?.request?.bidId = bidId
                rejectOrderViewModel?.request?.token = Preferences.getStringPreference(
                    this@BidderBiddingDetails, TOKEN
                )
                rejectOrderViewModel?.request?.productId = productId
                rejectOrderViewModel?.request?.notificationId = null
                rejectOrderViewModel?.getRejectOrderRequest()
            }
        }
    }
}