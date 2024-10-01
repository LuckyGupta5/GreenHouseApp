package com.ripenapps.greenhouse.screen.seller

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
import com.ripenapps.greenhouse.adapter.seller.RecyclerBiddingHistory
import com.ripenapps.greenhouse.databinding.ActivityBiddingDetailsSellerBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.HistoryModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.sellerfragemnt.AuctionTime
import com.ripenapps.greenhouse.model.product_details.Product
import com.ripenapps.greenhouse.model.seller.response.BidHighestBiddingHistoryResponseModel
import com.ripenapps.greenhouse.model.seller.response.ProductDetailsResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.fullDateAndTimeFormat
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeftWithoutSecond
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.PreEntity
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidHighestBiddingHistoryViewModel
import com.ripenapps.greenhouse.view_models.ProductDetailsViewModel

class BiddingDetails : BaseActivity() {
    private lateinit var biddingDetailsBinding: ActivityBiddingDetailsSellerBinding
    private val arrHistorySeller: ArrayList<HistoryModel> = arrayListOf()
    private lateinit var productDetailViewModel: ProductDetailsViewModel
    private var productDetails: Product? = null
    var accountBlocked: AccountBlocked? = null
    var subCategoryId: String? = null
    var endDate: String? = null
    private var biddingCount: String? = null
    private var bidHighestBiddingHistoryModelViewModel: BidHighestBiddingHistoryViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@BiddingDetails, R.color.status_bar)
        super.onCreate(savedInstanceState)

        biddingDetailsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bidding_details_seller)


        if (Preferences.getStringPreference(this@BiddingDetails, "language") == "ar") {
            biddingDetailsBinding.backwardImage.rotation = 180f
        }

        initListeners()
        initViewModel()
        initHistoryModel()
        historyApi()
        detailApi()
    }

    private fun detailApi() {
        productDetailViewModel.request.productId = intent?.getStringExtra(PRODUCT_ID)
        productDetailViewModel.request.token =
            Preferences.getStringPreference(this@BiddingDetails, TOKEN)
        productDetailViewModel.getProductDetailRequest()
    }

    private fun historyApi() {
        bidHighestBiddingHistoryModelViewModel?.request?.productId =
            intent?.getStringExtra(PRODUCT_ID)
        bidHighestBiddingHistoryModelViewModel?.request?.token =
            Preferences.getStringPreference(this@BiddingDetails, TOKEN)
        bidHighestBiddingHistoryModelViewModel?.getHighestBiddingRequest()
    }

    private fun initHistoryModel() {
        bidHighestBiddingHistoryModelViewModel?.request?.productId =
            Preferences.getStringPreference(this@BiddingDetails, "")
        bidHighestBiddingHistoryModelViewModel =
            ViewModelProvider(this@BiddingDetails)[BidHighestBiddingHistoryViewModel::class.java]
        biddingDetailsBinding.lifecycleOwner = this@BiddingDetails
        bidHighestBiddingHistoryModelViewModel?.getBidHighestBidHistoryData()
            ?.observe(this@BiddingDetails) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(false)
                            biddingDetailsBinding.scrollLayout.setVisibility(true)
                            ProcessDialog.dismissDialog()
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                BidHighestBiddingHistoryResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {

                                    biddingCount = data?.data?.count.toString()
                                    Log.d(
                                        "TAG", "initHistoryModel: " + data?.data?.count.toString()
                                    )
                                    if (data.data?.highestBid?.isEmpty() == true) {
                                        biddingDetailsBinding.apply {
                                            biddingHistory.setVisibility(false)
                                            headingPlaceHolder.setVisibility(true)

                                        }
                                    } else {
                                        biddingDetailsBinding.apply {
                                            biddingHistory.setVisibility(true)
                                            headingPlaceHolder.setVisibility(false)
                                        }
                                    }

                                    data.data?.highestBid?.forEachIndexed { _, history ->
                                        arrHistorySeller.add(
                                            HistoryModel(
                                                username = history.userName ?: "",
                                                bidPrice = getString(R.string.sar) + " " + history.amount.toString(),
                                                time = fullDateAndTimeFormat(history.updatedAt.toString()),
                                                cardViewText = getInitials(history.userName.toString())
                                            )
                                        )
                                    }
                                    val recyclerHistory = RecyclerBiddingHistory()
                                    biddingDetailsBinding.biddingHistory.adapter = recyclerHistory
                                    recyclerHistory.addItems(arrHistorySeller)
                                    Log.d(
                                        PreEntity.TAG, "initHistoryModel: " + data.data?.highestBid
                                    )
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
                                    val signin = Intent(this@BiddingDetails, SignIn::class.java)
                                    startActivity(signin)
                                    finishAffinity()
                                }

                                else -> {
                                    biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(false)
                                    biddingDetailsBinding.scrollLayout.setVisibility(true)
                                    Toast.makeText(
                                        this@BiddingDetails, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.LOADING -> {
                            biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(true)
                            biddingDetailsBinding.scrollLayout.setVisibility(false)
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(true)
                            biddingDetailsBinding.scrollLayout.setVisibility(false)
                            Toast.makeText(this@BiddingDetails, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        productDetailViewModel =
            ViewModelProvider(this@BiddingDetails)[ProductDetailsViewModel::class.java]
        biddingDetailsBinding.lifecycleOwner = this@BiddingDetails
        productDetailViewModel.getProductDetailData().observe(this@BiddingDetails) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(false)
                        biddingDetailsBinding.btnChangeTime.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ProductDetailsResponseModel::class.java
                        )
                        Log.d("TAG", "sellerBiddingDetailsResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {

                                productDetails = data.data?.product
                                Log.d("TAG", "productDetailsData: $productDetails")
                                Preferences.setStringPreference(
                                    this@BiddingDetails, PRODUCT_ID, data.data?.product?.id
                                )
                                subCategoryId = data.data?.product?.subCategoryId?.id
                                endDate = data.data?.product?.endDate
                                biddingDetailsBinding.apply {
                                    vegImg.setImage(
                                        data.data?.product?.imageUrl?.getOrNull(0).toString()
                                    )
                                    subCategoryText.text = data?.data?.product?.subCategoryId?.enName
                                    location.text = data?.data?.product?.productLocation
                                    price.text =
                                        getString(R.string.sar) + " " + data?.data?.product?.price.toString()
                                    timeDuration.text = timeLeftWithoutSecond(
                                        data.data?.product?.endDate.toString(), this@BiddingDetails
                                    )
                                    val quantity1 = data.data?.product?.quantity ?: ""
                                    val unit = data.data?.product?.unit?.lowercase() ?: ""
                                    quantity.text = "$quantity1 $unit"
                                    bidCountNumber.text = biddingCount

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
                                val signin = Intent(this@BiddingDetails, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }
                            else -> {
                                biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(false)
                                biddingDetailsBinding.btnChangeTime.setVisibility(true)
                                biddingDetailsBinding.scrollLayout.setVisibility(true)

                                Toast.makeText(
                                    this@BiddingDetails, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }

                    Status.LOADING -> {
                        biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(true)
                        biddingDetailsBinding.btnChangeTime.setVisibility(false)
                        biddingDetailsBinding.scrollLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(true)
                        biddingDetailsBinding.btnChangeTime.setVisibility(false)
                        biddingDetailsBinding.scrollLayout.setVisibility(false)
                        Toast.makeText(this@BiddingDetails, it.message, Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
                biddingDetailsBinding.sellerDetailMainShimmer.sellerDetailMainShimmer.setVisibility(true)
                biddingDetailsBinding.btnChangeTime.setVisibility(false)
                biddingDetailsBinding.scrollLayout.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        biddingDetailsBinding.btnChangeTime.setOnClickListener {
            val auctionTime = AuctionTime(
                productDetails,
                isFromEdit = false,
                isFromOrder = false,
                successCallBack = ::handleSuccess,
                isFinish = true
            )
            auctionTime.show(supportFragmentManager, auctionTime.tag)
        }
        biddingDetailsBinding.backwardImage.setOnClickListener {
            finish()
        }

    }

    private fun handleSuccess() {
        ProcessDialog.dismissDialog()
        productDetailViewModel.request.token =
            Preferences.getStringPreference(this@BiddingDetails, TOKEN)
        productDetailViewModel.getProductDetailRequest()
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("status", "update")
        })
    }
}
