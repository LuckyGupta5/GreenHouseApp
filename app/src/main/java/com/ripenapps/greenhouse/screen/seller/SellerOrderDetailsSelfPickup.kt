package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySellerOrderDetailsSelfPickupBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.sellerfragemnt.AddPackageConfirmed
import com.ripenapps.greenhouse.fragment.sellerfragemnt.AuctionTime
import com.ripenapps.greenhouse.fragment.sellerfragemnt.EndAuctionFragment
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerMarkAsReceivedFragment
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerOrderCancelSelectOne
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerOrderDetailsAddPackageDetails
import com.ripenapps.greenhouse.model.seller.response.SellerDownloadInvoiceResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerGetMyOrdersDetailsResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerSecondHighestBidderResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.OrderDetailHelp
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.fullDateAndTimeFormat
import com.ripenapps.greenhouse.utills.CommonUtils.hoursLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Constants.TICKET_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.SellerDownloadInvoiceViewModel
import com.ripenapps.greenhouse.view_models.SellerGetMyOrderDetailsViewModel
import com.ripenapps.greenhouse.view_models.SellerSecondHighestBidderViewModel

class SellerOrderDetailsSelfPickup : BaseActivity() {
    private lateinit var binding: ActivitySellerOrderDetailsSelfPickupBinding
    private var orderDetailsViewModel: SellerGetMyOrderDetailsViewModel? = null
    private var downloadInvoiceViewModel: SellerDownloadInvoiceViewModel? = null
    private var addPackage: SellerOrderDetailsAddPackageDetails? = null
    var accountBlocked: AccountBlocked? = null
    var subCategoryId: String? = null
    private var callingNumber: String? = null
    private var orderID: String? = null
    private var orderHelpId: String? = null
    private var status: String? = null
    private var cancelled: String? = null
    private var deliverTime: String? = null
    private var returnedStatus: String? = null
    private var productId: String? = null
    private var selfPackTime: String? = null
    private var deliveredSelfTime: String? = null
    private var performAction = -1
    private var markAsReceived: SellerMarkAsReceivedFragment? = null
    private var sellerSecondHighestViewModel: SellerSecondHighestBidderViewModel? = null
    private var bidderId: String? = null
    @SuppressLint("SetTextI18n", "StringFormatInvalid")

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@SellerOrderDetailsSelfPickup, R.color.status_bar)
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@SellerOrderDetailsSelfPickup, R.color.status_bar)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_order_details_self_pickup)

        if (Preferences.getStringPreference(
                this@SellerOrderDetailsSelfPickup, "language"
            ) == "ar"
        ) {
            binding.backwardImage.rotation = 180f
        }
        initViewModel()
        cancelFirstOrderViewModel()
        downloadInvoiceViewModel()
        hitApi()
        initListeners()

    }

    private fun hitApi() {
        orderDetailsViewModel?.request?.orderId = intent.getStringExtra(ORDER_ID)
        orderDetailsViewModel?.request?.productId = productId
        orderDetailsViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerOrderDetailsSelfPickup, TOKEN)
        orderDetailsViewModel?.getSellerMyOrdersDetailsRequest()
    }

    var dataModel: SellerGetMyOrdersDetailsResponseModel.Data.Orders? = null


    private fun initViewModel() {
        orderDetailsViewModel =
            ViewModelProvider(this@SellerOrderDetailsSelfPickup)[SellerGetMyOrderDetailsViewModel::class.java]
        binding.lifecycleOwner = this@SellerOrderDetailsSelfPickup
        orderDetailsViewModel?.getSellerMyOrdersDetailsData()
            ?.observe(this@SellerOrderDetailsSelfPickup) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(false)
                            binding.mainScrollLayout.setVisibility(true)
                            ProcessDialog.dismissDialog()
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                SellerGetMyOrdersDetailsResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    Log.d("TAG", "orderDetailResponse: ${Gson().toJson(data.data)}")
                                    dataModel = data?.data?.orders
                                    status = data?.data?.orders?.sellerOrderStatus
                                    orderID = data?.data?.orders?._id
                                    orderHelpId = data?.data?.orders?.orderId
                                    productId = data?.data?.orders?.productId
                                    bidderId = data?.data?.orders?.productDetails?.bidderId


                                    Log.d("inside", "initViewModel: $status $orderID")
                                    if (!data?.data?.orders?.cancelAt.isNullOrEmpty()) {
                                        cancelled =
                                            fullDateAndTimeFormat(data?.data?.orders?.cancelAt.toString())
                                    }
                                    if (!data?.data?.orders?.deliveryAt.isNullOrEmpty()) {
                                        deliverTime =
                                            fullDateAndTimeFormat(data?.data?.orders?.deliveryAt.toString())
                                    }
                                    returnedStatus = data?.data?.orders?.returnOrderStatus
                                    initMapClick(
                                        data.data?.orders?.productDetails?.location?.coordinates?.getOrNull(
                                            0
                                        ),
                                        data.data?.orders?.productDetails?.location?.coordinates?.getOrNull(
                                            1
                                        )
                                    )

                                    binding.apply {
                                        boxLengthNumber.text = data?.data?.orders?.boxLength
                                        boxHeightNumber.text = data?.data?.orders?.boxHeight
                                        boxWidthNumber.text = data?.data?.orders?.boxWidth
                                        boxCountNumber.text = data?.data?.orders?.boxNumber
                                        vegImg.setImage(
                                            data.data?.orders?.productDetails?.imageUrl ?: ""
                                        )
                                        vegText.text =
                                            data?.data?.orders?.productDetails?.subCategory?.enName
                                        location.text =
                                            data?.data?.orders?.productDetails?.productLocation
                                        productPrice.text =
                                            data?.data?.orders?.productDetails?.productPrice.toString()
                                        val quantity1 =
                                            data.data?.orders?.productDetails?.quantity ?: ""
                                        val unit =
                                            data.data?.orders?.productDetails?.unit?.lowercase()
                                                ?: ""
                                        quantity.text = "$quantity1 $unit"
                                        bidcount.text = data?.data?.orders?.bidCount.toString()
                                        orderId.text = "ORDER ID# ${data?.data?.orders?.orderId}"
                                        val orderId = data?.data?.orders?.orderId ?: "Unknown"
                                        val orderIdText = getString(R.string.order_id, orderId)
                                        returnOrderId.text = orderIdText
                                        if (!data?.data?.orders?.productDetails?.createdAt.isNullOrEmpty()) {
                                            orderReceiveTime.text =
                                                fullDateAndTimeFormat(data?.data?.orders?.productDetails?.createdAt.toString())
                                        }
                                        if (!data?.data?.orders?.returnAt.isNullOrEmpty()) {
                                            returnOrderReceiveTime.text =
                                                fullDateAndTimeFormat(data?.data?.orders?.returnAt.toString())
                                        }
                                        if (!data?.data?.orders?.packedAt.isNullOrEmpty()) {
                                            packedTime.text =
                                                fullDateAndTimeFormat(data?.data?.orders?.packedAt.toString())
                                        }
                                        if (!data?.data?.orders?.packedAt.isNullOrEmpty()) {
                                            returnPackedTime.text =
                                                fullDateAndTimeFormat(data?.data?.orders?.packedAt.toString())
                                        }
                                        pickupOrderUserId.text =
                                            if (data.data?.orders?.shippingMethod == "myself") {
                                                data.data.orders.productDetails?.userName + " " + getString(
                                                    R.string.will_pick_up
                                                )
                                            } else {
                                                data.data?.orders?.productDetails?.userName + " " + data.data?.orders?.shippingMethod
                                            }
                                        Log.d(
                                            "TAG", "shipping:" + data.data?.orders?.shippingMethod
                                        )
                                        address.text =
                                            data?.data?.orders?.productDetails?.productLocation
                                        callingNumber = data?.data?.orders?.productDetails?.mobile
                                        numberText.text = data?.data?.orders?.productDetails?.mobile
                                        if (data.data?.orders?.commissionAmount == null)
                                            commissionPrice.text = getString(R.string.sar) + " " + 0.0
                                        else {
                                            commissionPrice.text = "${getString(R.string.sar)} ${data.data.orders.commissionAmount}"
                                        }
                                        if (data.data?.orders?.vatAmount == null)
                                            vatPrice.text = getString(R.string.sar) + " " + 0.0
                                        else {
                                            vatPrice.text =
                                                getString(R.string.sar) + " " + data.data.orders.vatAmount
                                        }

                                        // Assuming that amount and vatAmount are Strings in your data model
//                                        val amountString = data.data?.orders?.amount ?: "0.0"
//                                        val vatAmountString = data.data?.orders?.vatAmount ?: "0.0"
//
//                                        val amount = amountString.toDoubleOrNull() ?: 0.0
//                                        val vatAmount = vatAmountString.toDoubleOrNull() ?: 0.0
//                                        val youEarned = amount - vatAmount

                                        val amount = data?.data?.orders?.amount ?: 0f
                                        val vatAmount = data.data?.orders?.vatAmount ?: 0f
                                        val commissionAmount = data.data?.orders?.commissionAmount ?: 0f

                                        youEarnedPrice.text =
                                            getString(R.string.sar) + " " + (amount - vatAmount - commissionAmount).toString()

                                        if (status?.lowercase() == "received") {
                                            deliveredSelfTime =
                                                hoursLeft(data.data?.orders?.packTimer.toString())

                                        } else if (status?.lowercase() == "packed") {
                                            val time =
                                                hoursLeft(data.data?.orders?.selfPickUpTimer.toString())
                                            Log.d("TAG", "initViewModel: $time $deliveredSelfTime")
                                            selfPackTime =
                                                hoursLeft(data.data?.orders?.selfPickUpTimer.toString())
                                        }
                                        setResult(Activity.RESULT_OK, Intent().apply {
                                            putExtra("status", status)
                                            putExtra("id", orderID)//
                                            putExtra("packed", deliveredSelfTime)
                                            putExtra("deliver", selfPackTime)
                                        })
                                        Log.d("TAG", "status:$status")
                                        cancelledReasonText.text =
                                            data.data?.orders?.cancelReason.toString()
                                        if (status?.lowercase() == "received" || status?.lowercase() == "delivered" || status?.lowercase() == "packed" || status?.lowercase() == "cancelled") {
                                            changeOrderStatus(
                                                status?.lowercase() ?: "received",
                                                data.data?.orders?.isAssign ?: 0
                                            )
                                            binding.orderReturnStatusLayout.setVisibility(false)
                                            binding.orderStatusLayout.setVisibility(true)
                                        } else if (returnedStatus?.lowercase() == "pending" || returnedStatus?.lowercase() == "returnReceived" || returnedStatus?.lowercase() == "orderPicked" || returnedStatus?.lowercase() == "packageReceived") {
                                            returnChangeOrderStatus(
                                                returnedStatus?.lowercase() ?: "pending"
                                            )
                                            binding.orderReturnStatusLayout.setVisibility(true)
                                            binding.orderStatusLayout.setVisibility(false)
                                        }


                                        if (!data.data?.orders?.deliveryAt.isNullOrEmpty()) {
                                            deliveredTime.text =
                                                fullDateAndTimeFormat(data.data?.orders?.deliveryAt.toString())
                                        }
                                        if (!data.data?.orders?.deliveryAt.isNullOrEmpty()) {
                                            returnDeliveredTime.text =
                                                fullDateAndTimeFormat(data.data?.orders?.returnAt.toString())
                                        }
                                        Log.d("TAG", "initViewModel: $returnedStatus")

//                                        shippingCostTotal =
//                                            (data?.data?.orders?.getOrNull(0)?.productDetails?.productPrice
//                                                ?: 0.0).toString()
//                                        shippingCostPrice.text =
//                                            (shippingCostTotal + youEarnedPrice + vatPrice)

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
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "token"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "user_details"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "isVerified"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "roomId"
                                    )
                                    val signIn = Intent(
                                        this@SellerOrderDetailsSelfPickup, SignIn::class.java
                                    )
                                    startActivity(signIn)
                                    finishAffinity()
                                }

                                else -> {
                                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(
                                        false
                                    )

                                    Toast.makeText(
                                        this@SellerOrderDetailsSelfPickup,
                                        data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                        }

                        Status.LOADING -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                            binding.mainScrollLayout.setVisibility(false)
                        }

                        Status.ERROR -> {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                            Log.e("TAG", "initViewModel: ${it.message}")
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                        }
                    }
                } catch (e: Exception) {
                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(false)
                    Log.d("error", e.message.toString())
                }
            }
    }

    private fun handleAssignStatus(isAssign: Int) {
        when (isAssign) {
            0 -> binding.waitingSecondOne.setVisibility(false)
            1 -> {
                binding.apply {
                    waitingSecondOne.setVisibility(true)
                    receivedContainer.setVisibility(false)
                    orderReturnStatusLayout.setVisibility(false)
                    selectOne.setVisibility(false)
                    btnLayout.setVisibility(false)
                }
            }

            2 -> {
                binding.apply {
                    waitingSecondOne.setVisibility(false)
                    receivedContainer.setVisibility(true)
                    orderReturnStatusLayout.setVisibility(true)
                    selectOne.setVisibility(true)
                    select1.setTextColor(
                        ContextCompat.getColor(
                            this@SellerOrderDetailsSelfPickup, R.color.light_gray
                        )
                    )
                    select1.isEnabled = false
                    select1.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@SellerOrderDetailsSelfPickup, R.drawable.ic_radio_button_unselected
                        ), null, null, null
                    )
                }
            }
        }
    }

    private fun initMapClick(latitude: Double?, longitude: Double?) {
        binding.mapView.setOnClickListener {
            CommonUtils.openMap(
                this@SellerOrderDetailsSelfPickup,
                latitude = latitude ?: 0.0,
                longitude = longitude ?: 0.0
            )
        }
    }

    private fun cancelFirstOrderViewModel() {
        sellerSecondHighestViewModel =
            ViewModelProvider(this@SellerOrderDetailsSelfPickup)[SellerSecondHighestBidderViewModel::class.java]
        binding.lifecycleOwner = this@SellerOrderDetailsSelfPickup
        sellerSecondHighestViewModel?.getSellerSecondHighestData()
            ?.observe(this@SellerOrderDetailsSelfPickup) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(false)
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                SellerSecondHighestBidderResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    SellerOrderCancelSelectOne().show(
                                        supportFragmentManager, SellerOrderCancelSelectOne().tag
                                    )
                                }

                                402 -> {
                                    if (accountBlocked?.isAdded != true) {
                                        accountBlocked = AccountBlocked(data.message)
                                        accountBlocked?.show(
                                            supportFragmentManager, accountBlocked?.tag
                                        )
                                    }
                                }

                                403, 401 -> {
                                    val signIn = Intent(this, SignIn::class.java)
                                    startActivity(signIn)
                                    finishAffinity()
                                }

                                else -> {
                                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(
                                        false
                                    )
                                    Toast.makeText(
                                        this, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                        }

                        Status.LOADING -> {
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

                        }
                    }
                } catch (e: Exception) {
                    binding.orderDetailShimmer.orderDetailMainShimmer.setVisibility(true)

                    Log.d("error", e.message.toString())
                }
            }
    }

    private fun downloadInvoiceViewModel() {
        downloadInvoiceViewModel =
            ViewModelProvider(this@SellerOrderDetailsSelfPickup)[SellerDownloadInvoiceViewModel::class.java]
        binding.lifecycleOwner = this@SellerOrderDetailsSelfPickup
        downloadInvoiceViewModel?.downloadInvoiceData()
            ?.observe(this@SellerOrderDetailsSelfPickup) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            ProcessDialog.dismissDialog()
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                SellerDownloadInvoiceResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(data.data.url)))
                                    ProcessDialog.dismissDialog()
                                }

                                403, 401 -> {
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "token"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "user_details"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "isVerified"
                                    )
                                    Preferences.removePreference(
                                        this@SellerOrderDetailsSelfPickup, "roomId"
                                    )
                                    val signIn = Intent(
                                        this@SellerOrderDetailsSelfPickup, SignIn::class.java
                                    )
                                    startActivity(signIn)
                                    finishAffinity()
                                }

                                402 -> {
                                    if (accountBlocked?.isAdded != true) {
                                        accountBlocked = AccountBlocked(data.message)
                                        accountBlocked?.show(
                                            supportFragmentManager, accountBlocked?.tag
                                        )
                                    }
                                }

                                else -> {
                                    ProcessDialog.dismissDialog()
                                    Toast.makeText(
                                        this@SellerOrderDetailsSelfPickup,
                                        data.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                        }

                        Status.LOADING -> {
                            ProcessDialog.startDialog(this@SellerOrderDetailsSelfPickup)
                        }

                        Status.ERROR -> {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                            Log.e("TAG", "initViewModel: ${it.message}")
                            ProcessDialog.dismissDialog()
                            Toast.makeText(
                                this@SellerOrderDetailsSelfPickup,
                                it.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    private fun initListeners() {
        binding.selfPack.setOnClickListener {
            addPackage =
                SellerOrderDetailsAddPackageDetails(orderID) { length, width, height, boxCount ->
                    val addPackage = AddPackageConfirmed()
                    binding.boxDetailLayout.setVisibility(true)
                    orderDetailsViewModel?.getSellerMyOrdersDetailsRequest()
//                        binding.boxLengthNumber.text = length.toString()
//                        binding.boxHeightNumber.text = height.toString()
//                        binding.boxWidthNumber.text = width.toString()
//                        binding.boxCountNumber.text = boxCount.toString()
                    addPackage.show(supportFragmentManager, addPackage.tag)
                }
            if (!isFinishing && addPackage?.isVisible != true) {
                addPackage?.show(supportFragmentManager, addPackage?.tag)
            }
        }
        binding.callButton.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$callingNumber")
            startActivity(dialIntent)
        }
        binding.backwardImage.setOnClickListener {
            finish()
        }

        binding.helpLayout.setOnClickListener {
            val help = Intent(this, OrderDetailHelp::class.java)
            help.putExtra(TICKET_ID, orderHelpId)
            startActivity(help)
        }
        binding.btnLayout.setOnClickListener {
            when (performAction) {
                0 -> {
//                       hitApi()
                    sellerSecondHighestViewModel?.request?.token = Preferences.getStringPreference(
                        this@SellerOrderDetailsSelfPickup,
                        TOKEN
                    )
                    sellerSecondHighestViewModel?.request?.productId =
                        productId  //66702ec617961c363525a51e
                    sellerSecondHighestViewModel?.request?.bidderId = bidderId
                    Log.d(
                        "request",
                        "SecondHighestOrder: ${Gson().toJson(sellerSecondHighestViewModel?.request)}"
                    )
                    sellerSecondHighestViewModel?.getSellerSecondHighestRequest()
                }

                1 -> {
                    val auctionTime = AuctionTime(
                        isFromOrder = true,
                        productId = productId,
                        isFromEdit = false,
                        successCallBack = ::handleCancel,
                        isFinish = true
                    )
                    auctionTime.show(supportFragmentManager, auctionTime.tag)
                }

                2 -> {
                    val endAuction = EndAuctionFragment(productId, ::handleCancel)
                    endAuction.show(supportFragmentManager, endAuction.tag)
                }
            }
        }
        binding.downloadInvoiceBtn.setOnClickListener {
            downloadInvoiceViewModel?.request?.token =
                Preferences.getStringPreference(this@SellerOrderDetailsSelfPickup, TOKEN)
            downloadInvoiceViewModel?.request?.orderId = orderID
            downloadInvoiceViewModel?.request?.productId = productId
            downloadInvoiceViewModel?.downloadInvoiceRequest()
        }
    }

    private fun handleCancel() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("status", "cancelled")
            putExtra("isUpdate", "update")
            putExtra("id", orderID)//66700ff79d32459f0b7fec8b
        })
        finish()
    }

    private fun handleSuccess() {
        hitApi()
    }

    private fun changeOrderStatus(status: String, isAssign: Int) {
        when (status) {
            "received" -> {
                orderReceivedStatus()
            }

            "packed" -> {
                orderPackedStatus()
            }

            "delivered" -> {
                orderDeliveredStatus()
            }

            "cancelled" -> {
                orderCancelledStatus(isAssign)
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    private fun orderReceivedStatus() {
        binding.circleImg.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.ic_statuc_check
            )
        )
        binding.statusDotImg.setVisibility(true)
        binding.dotImg.setVisibility(true)
        binding.apply {
            cancelledByUser.setVisibility(false)
            viewCancelled.setVisibility(false)
            cancelledReason.setVisibility(false)
            selectOne.setVisibility(false)
            btnLayout.setVisibility(false)
            cancelledTime.setVisibility(false)
            deliveredTime.setVisibility(false)
            generateLabel.setVisibility(false)
            boxDetailLayout.setVisibility(false)
            packedTime.setVisibility(false)
            orderStatusLayout.setVisibility(true)
            time.setVisibility(true)
            selfPack.setVisibility(true)
            time.text = deliveredSelfTime
            generateDownloadLayout.setVisibility(true)
            orderReturnStatusLayout.setVisibility(false)
            downloadInvoiceBtn.setVisibility(false)
            binding.waitingSecondOne.setVisibility(false)
        }
    }

    private fun orderPackedStatus() {
        binding.statusDotImg.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        binding.statusDotImg2.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        binding.circleImage.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_statuc_check)
        )
        binding.statusDotImg.setVisibility(true)
        orderReceivedStatus()
        binding.statusDotImg2.setVisibility(true)
        binding.apply {
            cancelledByUser.setVisibility(false)
            viewCancelled.setVisibility(false)
            cancelledReason.setVisibility(false)
            selectOne.setVisibility(false)
            packedTime.setVisibility(true)
            btnLayout.setVisibility(false)
            cancelledTime.setVisibility(false)
            selfPack.setVisibility(false)
            deliveredTime.setVisibility(false)
            time.text = selfPackTime
            time.setVisibility(true)
            boxDetailLayout.setVisibility(true)
            if (status == "myself") {
                generateLabel.setVisibility(false)
            } else {
                generateLabel.setVisibility(false)
            }
            boxDetailLayout.setVisibility(true)
            orderStatusLayout.setVisibility(true)
            deliveredTime.setVisibility(false)
            generateDownloadLayout.setVisibility(false)
            orderReturnStatusLayout.setVisibility(false)
            downloadInvoiceBtn.setVisibility(false)
            binding.waitingSecondOne.setVisibility(false)
        }
    }

    private fun orderDeliveredStatus() {
        orderReceivedStatus()
        orderPackedStatus()
        binding.waitingSecondOne.setVisibility(false)
        binding.checkDeliver.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.ic_statuc_check
            )
        )
        binding.statusDotImg2.setVisibility(true)
        binding.dotImg2.setVisibility(true)
        binding.statusDotImg2.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        binding.apply {
            cancelledByUser.setVisibility(false)
            viewCancelled.setVisibility(false)
            cancelledReason.setVisibility(false)
            selectOne.setVisibility(false)
            btnLayout.setVisibility(false)
            cancelledTime.setVisibility(false)
            time.setVisibility(false) //false
            hours.setVisibility(false)
            mapView.setVisibility(false)
            phoneLayer.setVisibility(false)
            deliveredTime.setVisibility(true)
            boxDetailLayout.setVisibility(true)
            pickupOrderUserId.setVisibility(true)
            selfPack.setVisibility(false)
            orderReturnStatusLayout.setVisibility(false)
            orderStatusLayout.setVisibility(true)
            generateLabel.setVisibility(false)
            downloadInvoiceBtn.setVisibility(true)
            generateDownloadLayout.setVisibility(true)
            if (deliverTime == "null") {
                deliveredTime.setVisibility(false)
            } else {
                deliveredTime.setVisibility(true)

            }


        }
    }

    private fun orderCancelledStatus(isAssign: Int) {
        binding.apply {
            statusDotImg.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SellerOrderDetailsSelfPickup, R.drawable.ic_order_packed_check
                )
            )
            circleImage.setImageResource(R.drawable.ic_order_cancel)
            statusDotImg.setVisibility(false)
            orderReceivedStatus()

            statusDotImg.setVisibility(false)
            cancelledByUser.setVisibility(true)
            viewCancelled.setVisibility(true)
            cancelledReason.setVisibility(true)
            selectOne.setVisibility(true)
            boxDetailLayout.setVisibility(false)
            btnLayout.setVisibility(true)
            cancelledTime.setVisibility(true)
            cancelledReasonText.setVisibility(true)
            waitingSecondOne.setVisibility(true)
            packed.text = getString(R.string.order_cancelled)
            shippingDetailsLayout.setVisibility(false)
            priceDetailsLayout.setVisibility(false)
            checkDeliver.setVisibility(false)
            dotImg2.setVisibility(false)
            delivered.setVisibility(false)
            deliveredTime.setVisibility(false)
            selfPack.setVisibility(false)
            boxDetailLayout.setVisibility(false)
            cancelledTime.text = cancelled
            orderStatusLayout.setVisibility(true)
            generateLabel.setVisibility(false)
            generateDownloadLayout.setVisibility(false)
            orderReturnStatusLayout.setVisibility(false)
            downloadInvoiceBtn.setVisibility(false)

            if (cancelled == "null") {
                cancelledTime.setVisibility(false)
            } else {
                cancelledTime.setVisibility(true)
            }
            select1.setOnClickListener {
                select1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                select2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                select3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                //open bottomSheet
                performAction = 0
            }

            select2.setOnClickListener {
                select1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                select2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                select3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                performAction = 1

            }

            select3.setOnClickListener {
                select1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                select2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_unselected, 0, 0, 0
                )
                select3.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_radio_button_selected, 0, 0, 0
                )
                performAction = 2
            }

        }
        handleAssignStatus(isAssign)
    }


    // Return Flow
    @SuppressLint("SetTextI18n")
    private fun orderReturnedStatus() {
        binding.apply {
            orderReceive.text = getString(R.string.return_received)
            binding.circleImg.setImageDrawable(
                ContextCompat.getDrawable(
                    this@SellerOrderDetailsSelfPickup, R.drawable.ic_statuc_check
                )
            )
            binding.statusDotImg.setVisibility(true)
            binding.dotImg.setVisibility(true)
            binding.apply {
                cancelledByUser.setVisibility(false)
                viewCancelled.setVisibility(false)
                cancelledReason.setVisibility(false)
                selectOne.setVisibility(false)
                btnLayout.setVisibility(false)
                cancelledTime.setVisibility(false)
                deliveredTime.setVisibility(false)
                generateLabel.setVisibility(false)
                boxDetailLayout.setVisibility(true)
                generateDownloadLayout.setVisibility(false)
                downloadInvoiceBtn.setVisibility(false)
            }
        }
    }


    private fun returnChangeOrderStatus(status: String) {
        when (status) {
            "pending" -> {
                binding.orderReturnStatusLayout.setVisibility(true)
                binding.returnOrderReceiveTime.setVisibility(true)
                binding.returnPackedTime.setVisibility(false)
                binding.returnDeliveredTime.setVisibility(false)
                binding.generateDownloadLayout.setVisibility(false)
                binding.downloadInvoiceBtn.setVisibility(false)
                binding.callButton.setVisibility(false)
                binding.mapView.setVisibility(false)
                binding.hours.setVisibility(false)
                binding.time.setVisibility(false)  //false
                binding.boxDetailLayout.setVisibility(true)
                binding.downloadInvoiceBtn.setVisibility(false)
                binding.phoneLayer.setVisibility(false)
                binding.returnDotImg.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
                )
            }
            "returnReceived" -> {
                orderReturnReceivedStatus()
            }

            "orderPicked" -> {
                orderReturnOrderPicked()
            }

            "packageReceived" -> {
                orderReturnPackedReceivedStatus()
            }
        }
    }

    private fun orderReturnReceivedStatus() {
        binding.returnCircleImg.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_statuc_check)
        )
        binding.returnDotImg.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        binding.returnStatusDotImg2.setVisibility(true)
        binding.returnDotImg.setVisibility(false)
        binding.returnIcPackPending.setVisibility(true)
        binding.returnStatusDotImg.setVisibility(true)
        binding.downloadInvoiceBtn.setVisibility(false)
        binding.returnDotImg.setVisibility(false)
        binding.apply {
            orderStatusLayout.setVisibility(false)
            returnDeliveredTime.setVisibility(false)
            returnPackedTime.setVisibility(false)
            returnOrderReceiveTime.setVisibility(true)
            orderReturnStatusLayout.setVisibility(true)
            generateDownloadLayout.setVisibility(false)
            downloadInvoiceBtn.setVisibility(false)
            time.setVisibility(false)
            hours.setVisibility(false)
            phoneLayer.setVisibility(false)
            boxDetailLayout.setVisibility(true)
            selectOne.setVisibility(false)
            waitingSecondOne.setVisibility(false)

        }
    }

    private fun orderReturnOrderPicked() {
        binding.returnStatusDotImg.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        binding.returnCircleImage.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_statuc_check)
        )
        binding.returnStatusDotImg.setVisibility(false)
        orderReturnReceivedStatus()
        binding.orderStatusLayout.setVisibility(false)
        binding.orderReturnStatusLayout.setVisibility(true)
        binding.returnDeliveredTime.setVisibility(false)
        binding.returnPackedTime.setVisibility(true)
        binding.returnStatusDotImg2.setVisibility(true)
        binding.generateDownloadLayout.setVisibility(false)
        binding.downloadInvoiceBtn.setVisibility(false)
        binding.markAsReceived.setVisibility(true)
        binding.apply {
            time.setVisibility(false)
            hours.setVisibility(false)
            phoneLayer.setVisibility(false)
            boxDetailLayout.setVisibility(true)
            selectOne.setVisibility(false)
            waitingSecondOne.setVisibility(false)
        }
        binding.markAsReceived.setOnClickListener {
            if (markAsReceived?.isAdded != true) {
                markAsReceived = SellerMarkAsReceivedFragment()
                markAsReceived?.show(supportFragmentManager, markAsReceived?.tag)
            }
        }
    }

    private fun orderReturnPackedReceivedStatus() {
        orderReturnReceivedStatus()
        orderReturnOrderPicked()
        binding.returnCheckDeliver.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.ic_statuc_check
            )
        )
        binding.returnDotImg2.setVisibility(true)
        binding.orderStatusLayout.setVisibility(false)
        binding.orderReturnStatusLayout.setVisibility(true)
        binding.returnDeliveredTime.setVisibility(true)
        binding.downloadInvoiceBtn.setVisibility(false)
        binding.returnPackedTime.setVisibility(true)
        binding.returnStatusDotImg2.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_order_packed_check)
        )
        if (deliverTime == "null") {
            binding.returnDeliveredTime.setVisibility(false)
        } else {
            binding.returnDeliveredTime.setVisibility(true)
        }
        binding.generateDownloadLayout.setVisibility(false)
        binding.downloadInvoiceBtn.setVisibility(false)
        binding.apply {
            time.setVisibility(false)
            hours.setVisibility(false)
            phoneLayer.setVisibility(false)
            boxDetailLayout.setVisibility(true)
            selectOne.setVisibility(false)
            waitingSecondOne.setVisibility(false)
        }
    }
}
