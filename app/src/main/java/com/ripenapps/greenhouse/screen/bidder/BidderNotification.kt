package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerNotificationAdapter
import com.ripenapps.greenhouse.databinding.ActivityBidderNotificationBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.NotificationModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.BidderBidDetailsResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetRoomIdResponse
import com.ripenapps.greenhouse.model.commonModels.notification.GetNotificationListResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.sendMessageTime
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.BidderBidDetailsViewModel
import com.ripenapps.greenhouse.view_models.GetNotificationList
import com.ripenapps.greenhouse.view_models.RejectOrderViewModel

class BidderNotification : BaseActivity() {
    private lateinit var binding: ActivityBidderNotificationBinding
    private var recyclerAdapter: RecyclerNotificationAdapter? = null
    private var notificationViewModel: GetNotificationList? = null
    private var bidderBidDetailViewModel: BidderBidDetailsViewModel? = null
    private var rejectOrderViewModel: RejectOrderViewModel? = null
    private var isPaging: Boolean = false
    var accountBlocked: AccountBlocked? = null
    var type = ""
    private var token: String? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@BidderNotification, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bidder_notification)
        token = Preferences.getStringPreference(this@BidderNotification, TOKEN)

        initListeners()
        initViewModel()
        initStartActivityInstance()
        initPagination()

        if (Preferences.getStringPreference(this@BidderNotification, "language").equals("ar")) {
            binding.backButton.rotation = 180f
        }
        notificationApi()

    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        notificationApi()
                    }
                }
            }
    }

    private fun notificationApi() {
        notificationViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderNotification, TOKEN)
        notificationViewModel?.getNotificationRequest()
    }

    private fun biddingDetailsApi(id: String, type: String, notificationId: String) {
        initBiddingDetailVM(id, type, notificationId)
        bidderBidDetailViewModel?.request?.bidId = id
        bidderBidDetailViewModel?.request?.token = token
        bidderBidDetailViewModel?.getBidderBidDeatilRequest()
    }


    private fun initRejectOrderViewModel(bidId: String) {
        rejectOrderViewModel =
            ViewModelProvider(this@BidderNotification)[RejectOrderViewModel::class.java]
        binding.lifecycleOwner = this@BidderNotification
        rejectOrderViewModel?.getRejectOrderData()?.observe(this@BidderNotification) {
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
                                recyclerAdapter?.apply {
                                    val position =
                                        arrNotification.indexOfFirst { it.notificationBidId == bidId }
                                    if (position != -1) {
                                        arrNotification.getOrNull(position)?.buttonKey = 2
                                        notifyItemChanged(position)
                                    } else {
                                        showToast("Something went wrong,Try refreshing the list.")
                                    }
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
                                Preferences.removePreference(this@BidderNotification, "token")
                                Preferences.removePreference(
                                    this@BidderNotification,
                                    "user_details"
                                )
                                Preferences.removePreference(this@BidderNotification, "isVerified")
                                Preferences.removePreference(this@BidderNotification, "roomId")
                                val signin = Intent(this@BidderNotification, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@BidderNotification, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@BidderNotification)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@BidderNotification, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initBiddingDetailVM(bidId: String, type: String, notificationId: String) {
        bidderBidDetailViewModel =
            ViewModelProvider(this@BidderNotification)[BidderBidDetailsViewModel::class.java]
        bidderBidDetailViewModel?.getBidderBidDeatilData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
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
                                ProcessDialog.dismissDialog()
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("status", "update")
                                })
                                if (this.type == "reject") {
                                    rejectApi(
                                        bidId, data.data?.product?.id.toString(), notificationId
                                    )
                                } else {
                                    val toProcessToDeliver = Intent(
                                        this@BidderNotification, ProcessToDeliver::class.java
                                    )
                                    toProcessToDeliver.putExtra("notificationId", notificationId)
                                    toProcessToDeliver.putExtra("bidId", bidId)
                                    toProcessToDeliver.putExtra(
                                        Constants.SELLER_ID, data.data?.product?.sellerId
                                    )
                                    toProcessToDeliver.putExtra(
                                        Constants.PRODUCT_ID, data.data?.product?.id
                                    )
                                    toProcessToDeliver.putExtra(
                                        "productPrice", data.data?.product?.productPrice.toString()
                                    )
                                    toProcessToDeliver.putExtra(
                                        "winningAmount", data.data?.bid?.myBidAmount.toString()
                                    )
                                    toProcessToDeliver.putExtra(
                                        "vatAmount", data.data?.bid?.vatAmount.toString()
                                    )
                                    toProcessToDeliver.putExtra(
                                        "payableAmount", data.data?.bid?.payableAmount.toString()
                                    )
                                    toProcessToDeliver.putExtra(
                                        "remainingAmount", data.data?.bid?.remainingAmount
                                    )
                                    toProcessToDeliver.putExtra(
                                        Constants.SUB_CATEGORY_NAME,
                                        data.data?.product?.subCategory?.enName
                                    )
                                    toProcessToDeliver.putExtra(
                                        Constants.CATEGORY_NAME,
                                        data.data?.product?.categoryId?.enName
                                    )
                                    toProcessToDeliver.putExtra(
                                        Constants.CATEGORY_ID, data.data?.product?.categoryId?.id
                                    )
                                    getContent.launch(toProcessToDeliver)
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
                                Preferences.removePreference(this@BidderNotification, "token")
                                Preferences.removePreference(
                                    this@BidderNotification,
                                    "user_details"
                                )
                                Preferences.removePreference(this@BidderNotification, "isVerified")
                                Preferences.removePreference(this@BidderNotification, "roomId")
                                val signin = Intent(this@BidderNotification, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(
                                    this@BidderNotification, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@BidderNotification)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@BidderNotification, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initViewModel() {
        notificationViewModel =
            ViewModelProvider(this@BidderNotification)[GetNotificationList::class.java]
        binding.lifecycleOwner = this@BidderNotification
        notificationViewModel?.getNotificationData()?.observe(this@BidderNotification) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.notificationShimmer.notificationMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetNotificationListResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "NotificationResponse: ${data.data}")
                                binding.idRefreshLayout.isRefreshing = false
                                notificationViewModel?.isLastPage = (data.data?.notification?.size
                                    ?: 0) < (notificationViewModel?.limit?.toInt()!!)
                                notificationViewModel?.isDataLoading = false

                                if (data.data?.notification?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        notificationRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        notificationRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }

                                val arrNotification: MutableList<NotificationModel> =
                                    mutableListOf()
                                data.data?.notification?.forEachIndexed { _, notification ->
                                    arrNotification.add(
                                        NotificationModel(
                                            notificationId = notification?._id,
                                            notification = notification?.title
                                                ?: "Green House Application",
                                            message = notification?.message ?: "",
                                            time = sendMessageTime(
                                                notification?.createdAt ?: ""
                                            ),
                                            originalTime = notification?.createdAt,
                                            buttonKey = notification?.secondHighestmsg,
                                            notificationBidId = notification?.bidId
                                        )
                                    )
                                }

                                if (recyclerAdapter == null || recyclerAdapter?.arrNotification?.isEmpty() == true) {
                                    recyclerAdapter =
                                        RecyclerNotificationAdapter(::handlerItemClick)
                                    binding.notificationRecycler.adapter = recyclerAdapter
                                    recyclerAdapter?.addItems(arrNotification)
                                } else {
                                    recyclerAdapter?.updateItemList(arrNotification)
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
                                Preferences.removePreference(this@BidderNotification, "token")
                                Preferences.removePreference(
                                    this@BidderNotification,
                                    "user_details"
                                )
                                Preferences.removePreference(this@BidderNotification, "isVerified")
                                Preferences.removePreference(this@BidderNotification, "roomId")
                                val signin = Intent(this@BidderNotification, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.notificationShimmer.notificationMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@BidderNotification, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            binding.notificationShimmer.notificationMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.notificationShimmer.notificationMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@BidderNotification, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun rejectApi(bidId: String, productId: String, notificationId: String) {
        initRejectOrderViewModel(bidId)
        rejectOrderViewModel?.request?.bidId = bidId
        rejectOrderViewModel?.request?.token = token
        rejectOrderViewModel?.request?.productId = productId
        rejectOrderViewModel?.request?.notificationId = notificationId
        rejectOrderViewModel?.getRejectOrderRequest()
    }

    private fun handlerItemClick(dataModel: NotificationModel, type: String) {
        this.type = type
        biddingDetailsApi(
            dataModel.notificationBidId ?: "", type, dataModel.notificationId.toString()
        )
    }

    private fun initListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            idRefreshLayout.setOnRefreshListener {
                notificationViewModel?.page = 1
                notificationViewModel?.request?.page = notificationViewModel?.page.toString()
                notificationViewModel?.limit = notificationViewModel?.limit!!
                notificationViewModel?.request?.token = token
                recyclerAdapter?.clearData()
                notificationViewModel?.getNotificationRequest()
            }
        }
    }

    private fun initPagination() {
        binding.notificationRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!notificationViewModel?.isLastPage!! && !notificationViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    notificationViewModel?.page = notificationViewModel?.page!! + 1
                    notificationViewModel?.limit = notificationViewModel?.limit!!
                    notificationViewModel?.isDataLoading = true
//                        binding.loader.setVisibility(notificationViewModel?.limit != 0f)
                    notificationViewModel?.request?.token = token
                    notificationViewModel?.request?.apply {
                        this.page = notificationViewModel?.page?.toString()
                        this.limit = notificationViewModel?.limit?.toString()
                    }
                    notificationViewModel?.getNotificationRequest()
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}