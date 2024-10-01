package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityBidderOrderDetailScreenBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.CancellationReason
import com.ripenapps.greenhouse.fragment.bidderfragment.OrderReceived
import com.ripenapps.greenhouse.fragment.bidderfragment.OrderReturnReason
import com.ripenapps.greenhouse.fragment.bidderfragment.RateUsCard
import com.ripenapps.greenhouse.model.bidder.response.BidderOrderDetailResponseModel
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.fullDateAndTimeFormat
import com.ripenapps.greenhouse.utills.CommonUtils.hoursLeft
import com.ripenapps.greenhouse.utills.CommonUtils.openMap
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.CANCELLED
import com.ripenapps.greenhouse.utills.Constants.CONFIRMED
import com.ripenapps.greenhouse.utills.Constants.DELIVERED
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Constants.PACKED
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.TICKET_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidderOrderDetailViewModel

class BidderOrderDetailScreen : BaseActivity() {
    private lateinit var binding: ActivityBidderOrderDetailScreenBinding
    private var bidderOrderDetailViewModel: BidderOrderDetailViewModel? = null
    private var accountBlocked: AccountBlocked? = null
    private var callingNumber: String? = null
    private var cancellationReason: CancellationReason? = null
    private var orderReceived: OrderReceived? = null
    private var id: String? = null
    private var productId: String? = null
    private var sellerId: String? = null
    private var myOrderId: String? = null
    private var rateUsCard: RateUsCard? = null
    private var orderReturnReason: OrderReturnReason? = null
    private var localLocation: Location? = null
    private var productName: String? = null
    private var cancellationCharge: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@BidderOrderDetailScreen, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@BidderOrderDetailScreen, R.layout.activity_bidder_order_detail_screen
        )
        initListeners()
        initViewModel()
        bidderDetailApi()
    }

    private fun bidderDetailApi() {
        bidderOrderDetailViewModel?.request?.orderId = intent.getStringExtra(ORDER_ID)
        Log.d("TAG", "bidderDetailApi: ${intent.getStringExtra(ORDER_ID)}")
        bidderOrderDetailViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderOrderDetailScreen, TOKEN)
        bidderOrderDetailViewModel?.getBidderOrderDetailsRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        bidderOrderDetailViewModel =
            ViewModelProvider(this@BidderOrderDetailScreen)[BidderOrderDetailViewModel::class.java]
        binding.lifecycleOwner = this@BidderOrderDetailScreen
        bidderOrderDetailViewModel?.getBidderOrdersDetailsData()
            ?.observe(this@BidderOrderDetailScreen) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(false)

                            binding.idRefreshLayout.setVisibility(true)
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                BidderOrderDetailResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    binding.idRefreshLayout.isRefreshing = false
                                    Log.d("TAG", "orderDetailDataResponse: ${data.data}")
                                    binding.apply {

                                        productId = data.data?.orders?.productDetails?.id
                                        sellerId = data.data?.orders?.sellerId
                                        myOrderId = data.data?.orders?.orderId
                                        id = data.data?.orders?.id
                                        localLocation = data.data?.orders?.productDetails?.location
                                        productName =
                                            data.data?.orders?.productDetails?.subCategory?.enName
                                        cancellationCharge =
                                            data.data?.orders?.cancellationCharge.toString()

                                        //confirmed status
                                        if (data.data?.orders?.orderStatus == CONFIRMED) {
                                            refuncDetailLayer.setVisibility(false)
                                            boxDetailsLayout.setVisibility(false)
                                            cancleButton.setVisibility(true)
                                            pickupTime.setVisibility(false)
                                            receivedButton.setVisibility(false)
                                            dotImg.setImageResource(R.drawable.gray_dotes) //dot_img
                                            dotImg2.setImageResource(R.drawable.gray_dotes) //dot_img2
                                            packedStatusImage.setImageResource(R.drawable.ic_gray_bg_tick) //gray tick
                                            deliverdStatusImage.setImageResource(R.drawable.ic_gray_bg_tick) //gray tick

                                            confirmedDateTime.text = fullDateAndTimeFormat(
                                                data.data.orders.createdAt.toString()
                                            )

                                            packedDateTime.visibility = View.INVISIBLE
                                            deliveredDateTime.setVisibility(false)
                                        }


                                        //order cancelled
                                        if (data.data?.orders?.orderStatus == CANCELLED) { //Cancelled
                                            phoneCallLayer.setVisibility(false)
                                            boxDetailsLayout.setVisibility(false)
                                            packed.text = CANCELLED
                                            pickupTime.setVisibility(false)
                                            dotImg.setImageResource(R.drawable.green_dotes)
                                            mapView.setVisibility(false)
                                            packedDateTime.visibility = View.VISIBLE
                                            confirmedDateTime.text = fullDateAndTimeFormat(
                                                data.data.orders.createdAt.toString()
                                            )
                                            packedDateTime.text = fullDateAndTimeFormat(
                                                data.data.orders.cancelAt.toString()
                                            )
                                            packedStatusImage.setImageResource(R.drawable.ic_order_cancel)
                                            dotImg2.setVisibility(false)
                                            orderCo.setVisibility(false)
                                            refuncDetailLayer.visibility = View.VISIBLE
                                            deliveredLayer.setVisibility(false)
                                            deliverdStatusImage.setVisibility(false)
                                            cancellationCharges.text =
                                                getString(R.string.sar) + " " + data.data.orders.cancellationCharge.toString()
                                            totalRefund.text =
                                                getString(R.string.sar) + " " + data.data.orders.refundAmount.toString()
                                        }


                                        // return statuses
                                        if (data.data?.orders?.orderStatus == "Returned") {
                                            buttonLayer.setVisibility(false)
                                            raingLayer.setVisibility(false)
                                            refuncDetailLayer.setVisibility(false)
                                            boxDetailsLayout.setVisibility(true)
                                            phoneCallLayer.setVisibility(false)
                                            mapView.setVisibility(false)
                                            pickupTime.setVisibility(false)
                                            boxLength.text = data.data.orders.boxLength.toString()
                                            boxHeight.text = data.data.orders.boxHeight.toString()
                                            boxWidth.text = data.data.orders.boxWidth.toString()
                                            boxCount.text = data.data.orders.boxNumber.toString()
                                            if (data.data.orders.returnOrderStatus == "pending") {
                                                raingLayer.setVisibility(false)
                                                orderConfirmedStatus.setImageResource(R.drawable.ic_green_bg_tick)
                                                orderReceive.text =
                                                    getString(R.string.return_requested)
                                                confirmedDateTime.text = fullDateAndTimeFormat(
                                                    data.data.orders.returnAt.toString()
                                                )
                                                dotImg.setImageResource(R.drawable.gray_dotes)
                                                packedStatusImage.setImageResource(R.drawable.ic_gray_bg_tick)
                                                packed.text = getString(R.string.return_accepted)
                                                packedDateTime.text = ""
                                                delivered.text = getString(R.string.out_for_pickup)
                                                deliveredDateTime.text = ""
                                                deliverdStatusImage.setImageResource(R.drawable.ic_gray_bg_tick)
                                                dotImg3.setVisibility(true)
                                                pickupSuccessfulLayer.setVisibility(true)
                                                pickedStatusImage.setVisibility(true)
                                                cancelOrderPacked.text =
                                                    getString(R.string.return_desc)
                                                cancelOrderPacked.setTextColor(
                                                    ContextCompat.getColor(
                                                        this@BidderOrderDetailScreen,
                                                        R.color.greenhousetheme
                                                    )
                                                )
                                            }

                                            if (data.data.orders.orderStatus == "returnAccept") {
                                                raingLayer.setVisibility(false)
                                                orderConfirmedStatus.setImageResource(R.drawable.ic_green_bg_tick)
                                                orderReceive.text =
                                                    getString(R.string.return_requested)
                                                confirmedDateTime.text = fullDateAndTimeFormat(
                                                    data.data.orders.returnAcceptAt.toString()
                                                )
                                                dotImg.setImageResource(R.drawable.green_dotes)
                                                packedStatusImage.setImageResource(R.drawable.ic_green_bg_tick)
                                                packed.text = getString(R.string.return_accepted)
                                                dotImg3.setVisibility(true)
                                                pickupSuccessfulLayer.setVisibility(true)
                                                pickedStatusImage.setVisibility(true)
                                            }

                                            if (data.data.orders.orderStatus == "Pickup") {
                                                raingLayer.setVisibility(false)
                                                orderConfirmedStatus.setImageResource(R.drawable.ic_green_bg_tick)
                                                orderReceive.text =
                                                    getString(R.string.return_requested)
                                                confirmedDateTime.text = fullDateAndTimeFormat(
                                                    data.data.orders.returnAcceptAt.toString()
                                                )
                                                dotImg.setImageResource(R.drawable.green_dotes)
                                                packedStatusImage.setImageResource(R.drawable.ic_green_bg_tick)
                                                packed.text = getString(R.string.return_accepted)

                                            }
                                        }

                                        //packed status
                                        if (data.data?.orders?.orderStatus == PACKED) {
                                            confirmedDateTime.text = fullDateAndTimeFormat(
                                                data.data.orders.createdAt.toString()
                                            )
                                            packedDateTime.setVisibility(true)
                                            packedDateTime.text =
                                                fullDateAndTimeFormat(data.data.orders.packedAt.toString())
                                            timeLeft.text =
                                                hoursLeft(data.data.orders.selfPickUpTimer.toString())
                                            cancelOrderPacked.setVisibility(false)
                                            refuncDetailLayer.setVisibility(false)
                                            boxDetailsLayout.setVisibility(true)
                                            boxLength.text = data.data.orders.boxLength.toString()
                                            boxHeight.text = data.data.orders.boxHeight.toString()
                                            boxWidth.text = data.data.orders.boxWidth.toString()
                                            boxCount.text = data.data.orders.boxNumber.toString()
                                            receivedButton.setVisibility(true)
                                            receivedButton.setTextColor(
                                                ContextCompat.getColor(
                                                    this@BidderOrderDetailScreen,
                                                    R.color.greenhousetheme
                                                )
                                            )
                                            receivedButton.setBackgroundColor(
                                                ContextCompat.getColor(
                                                    this@BidderOrderDetailScreen, R.color.white
                                                )
                                            )
                                            dotImg.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.green_dotes
                                                )
                                            )
                                            packedStatusImage.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.ic_green_bg_tick
                                                )
                                            )
                                            cancleButton.setVisibility(false)
                                        }

                                        //Delivered status
                                        if (data.data?.orders?.orderStatus == DELIVERED) {
                                            confirmedDateTime.text = fullDateAndTimeFormat(
                                                data.data.orders.createdAt.toString()
                                            )
                                            packedDateTime.text =
                                                fullDateAndTimeFormat(data.data.orders.packedAt.toString())
                                            deliveredDateTime.text =
                                                fullDateAndTimeFormat(data.data.orders.deliveryAt.toString())
                                            cancleButton.setVisibility(false)
                                            boxDetailsLayout.setVisibility(true)
                                            refuncDetailLayer.setVisibility(false)
                                            phoneCallLayer.setVisibility(false)
                                            mapView.setVisibility(false)
                                            pickupTime.setVisibility(false)
                                            boxLength.text = data.data.orders.boxLength.toString()
                                            boxHeight.text = data.data.orders.boxHeight.toString()
                                            boxWidth.text = data.data.orders.boxWidth.toString()
                                            boxCount.text = data.data.orders.boxNumber.toString()
                                            orderConfirmedStatus.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.ic_green_bg_tick
                                                )
                                            )
                                            dotImg.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.green_dotes
                                                )
                                            )
                                            packedStatusImage.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.ic_green_bg_tick
                                                )
                                            )
                                            deliverdStatusImage.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.ic_green_bg_tick
                                                )
                                            )
                                            dotImg2.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BidderOrderDetailScreen,
                                                    R.drawable.green_dotes
                                                )
                                            )
                                            cancleButton.text = getString(R.string.returned_order)
                                            receivedButton.text = getString(R.string.rate_us)
                                            receivedButton.setTextColor(
                                                ContextCompat.getColor(
                                                    this@BidderOrderDetailScreen, R.color.black
                                                )
                                            )
                                            receivedButton.setStrokeColorResource(R.color.gray)
                                            cancelOrderPacked.setVisibility(false)
                                        }

                                        //rating layer logic
                                        if (data.data?.orders?.rating?.isNotEmpty() == true && data.data.orders.orderStatus != "Returned") {
                                            receivedButton.setTextColor(
                                                ContextCompat.getColor(
                                                    this@BidderOrderDetailScreen,
                                                    R.color.inactive_background
                                                )
                                            )

                                            receivedButton.isEnabled = false
                                            raingLayer.setVisibility(true)
                                            ratingStar.text =
                                                getString(R.string.you_rated) + data.data.orders.rating[0].toString()
                                        } else {
                                            raingLayer.setVisibility(false)
                                        }

                                        //common views
                                        vegImg.setImage(
                                            data.data?.orders?.productDetails?.imageUrl ?: ""
                                        )
                                        vegText.text =
                                            data.data?.orders?.productDetails?.subCategory?.enName
                                        location.text =
                                            data.data?.orders?.productDetails?.productLocation
                                        productWeight.text =
                                            "${data.data?.orders?.productDetails?.quantity} ${data.data?.orders?.productDetails?.unit}"
                                        productPrice.text =
                                            data.data?.orders?.productDetails?.productPrice.toString()
                                        nameInitials.text =
                                            CommonUtils.getInitials(data.data?.orders?.productDetails?.userName.toString())
                                        sellerUserName.text =
                                            data.data?.orders?.productDetails?.userName.toString()
                                        orderId.text =
                                            getString(R.string.order_id) + "${data.data?.orders?.orderId}"
                                        shippingMethodType.text =
                                            if (data.data?.orders?.shippingMethod == "myself") {
                                                getString(R.string.i_will_pick_by_myself)
                                            } else {
                                                data.data?.orders?.shippingMethod
                                            }
                                        address.text =
                                            data.data?.orders?.productDetails?.productLocation
                                        callingNumber = data.data?.orders?.productDetails?.mobile
                                        number.text = data.data?.orders?.productDetails?.mobile

//                                        subtotalPrice.text =
//                                            getString(R.string.sar) + " " + data.data?.orders?.productDetails?.productPrice.toString() //need to be highest bid amount of that particular user
                                        deliveryFeePrice.text =
                                            getString(R.string.sar) + " " + 0.0 //data.data?.orders?.deliveryFee.toString() // required only when shipping method is required
                                        vatPrice.text =
                                            getString(R.string.sar) + " " + data.data?.orders?.vatAmount.toString() //key added
                                        amountPaidPrice.text =
                                            getString(R.string.sar) + " " + data.data?.orders?.payableAmount.toString() //calculation mistake at backend

                                        val amount = data.data?.orders?.amount ?: 0.0
                                        val vatAmount = data.data?.orders?.vatAmount ?: 0.0
                                        val subtotalPrice = amount - vatAmount
                                        subtotalPriceText.text =
                                            getString(R.string.sar) + " " + subtotalPrice.toString()
                                    }
                                }

                                402 -> {
                                    if (accountBlocked?.isAdded != true) {
                                        accountBlocked = AccountBlocked(data.message.toString())
                                        accountBlocked?.show(
                                            supportFragmentManager, accountBlocked?.tag/**/
                                        )
                                    }
                                }

                                403, 401 -> {
                                    Preferences.removePreference(
                                        this@BidderOrderDetailScreen, "token"
                                    )
                                    Preferences.removePreference(
                                        this@BidderOrderDetailScreen, "user_details"
                                    )
                                    Preferences.removePreference(
                                        this@BidderOrderDetailScreen, "isVerified"
                                    )
                                    Preferences.removePreference(
                                        this@BidderOrderDetailScreen, "roomId"
                                    )
                                    val signin =
                                        Intent(this@BidderOrderDetailScreen, SignIn::class.java)
                                    startActivity(signin)
                                    finishAffinity()
                                }

                                else -> {
                                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(
                                        false
                                    )
                                    binding.idRefreshLayout.setVisibility(true)
                                    Toast.makeText(
                                        this@BidderOrderDetailScreen,
                                        data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.LOADING -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                        }
                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                            Toast.makeText(
                                this@BidderOrderDetailScreen, it.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                    binding.idRefreshLayout.setVisibility(false)
                    Log.d("error", e.message.toString())
                }
            }
    }

    private fun initListeners() {
        binding.apply {
            idRefreshLayout.setOnRefreshListener {
                bidderDetailApi()
            }
            mapView.setOnClickListener {
                val latitude = localLocation?.coordinates?.getOrNull(1) ?: 0.0
                val longitude = localLocation?.coordinates?.getOrNull(0) ?: 0.0

                if (latitude != 0.0 || longitude != 0.0) {
                    openMap(this@BidderOrderDetailScreen, latitude, longitude)
                } else {
                    Toast.makeText(
                        this@BidderOrderDetailScreen,
                        "Invalid location coordinates",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            backButton.setOnClickListener {
                finish()
            }

            callButton.setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$callingNumber")
                startActivity(dialIntent)
            }

            cancleButton.setOnClickListener {
                if (cancleButton.text == getString(R.string.cancel_order)) {
                    if (cancellationReason?.isAdded != true) {
                        cancellationReason = CancellationReason(
                            id.toString(), productName, cancellationCharge
                        ) { bidderDetailApi() }
                        cancellationReason?.show(supportFragmentManager, cancellationReason?.tag)
                    }
                } else {
                    if (orderReturnReason?.isAdded != true) {
                        orderReturnReason = OrderReturnReason(id.toString()) { bidderDetailApi() }
                        orderReturnReason?.show(supportFragmentManager, orderReturnReason?.tag)
                    }
                }
            }

            receivedButton.setOnClickListener {
                if (receivedButton.text == getString(R.string.order_received)) {
                    if (orderReceived?.isAdded != true) {
                        orderReceived = OrderReceived(id.toString()) { bidderDetailApi() }
                        orderReceived?.show(supportFragmentManager, orderReceived?.tag)
                    }
                } else {
                    if (rateUsCard?.isAdded != true) {
                        rateUsCard = RateUsCard(
                            sellerId.toString(), id.toString()
                        ) { bidderDetailApi() }
                        rateUsCard?.show(supportFragmentManager, rateUsCard?.tag)
                    }
                }
            }

            helpButton.setOnClickListener {
                val toOrderDetailHelp =
                    Intent(this@BidderOrderDetailScreen, OrderDetailHelp::class.java)
                toOrderDetailHelp.putExtra(TICKET_ID, myOrderId)
                startActivity(toOrderDetailHelp)
            }

            sellerDetailLayer.setOnClickListener {
                val toAboutSeller = Intent(this@BidderOrderDetailScreen, AboutSeller::class.java)
                toAboutSeller.putExtra(SELLER_ID, sellerId)
                startActivity(toAboutSeller)
            }
        }
    }
}