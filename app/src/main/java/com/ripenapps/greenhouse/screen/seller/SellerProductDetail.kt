package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerBiddingHistory
import com.ripenapps.greenhouse.adapter.seller.RecyclerImageSliderAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerProductDetailBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.HistoryModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.sellerfragemnt.AuctionTime
import com.ripenapps.greenhouse.fragment.sellerfragemnt.MarkAsFeature
import com.ripenapps.greenhouse.fragment.sellerfragemnt.RemoveProduct
import com.ripenapps.greenhouse.model.product_details.Product
import com.ripenapps.greenhouse.model.seller.response.BidHighestBiddingHistoryResponseModel
import com.ripenapps.greenhouse.model.seller.response.ProductDetailsResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.convertDateTime
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.CommonUtils.isEndDateInRange
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.PreEntity.TAG
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidHighestBiddingHistoryViewModel
import com.ripenapps.greenhouse.view_models.ProductDetailsViewModel

@Suppress("DEPRECATION")
class SellerProductDetail : BaseActivity() {
    private lateinit var sellerProductDetailBinding: ActivitySellerProductDetailBinding
    private lateinit var productDetailViewModel: ProductDetailsViewModel
    private var productDetails: Product? = null
    private var accountBlocked: AccountBlocked? = null
    private var subCategoryId: String? = null
    private var productId: String? = null
    private var endDate: String? = null
    private var startDate: String? = null
    private var markAsFeature: MarkAsFeature? = null
    private var recyclerHistory: RecyclerBiddingHistory? = null
    private var bidHighestBiddingHistoryModelViewModel: BidHighestBiddingHistoryViewModel? = null
    private var isPaging: Boolean = false
    private var popupMenu: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        sellerProductDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_product_detail)

        if (Preferences.getStringPreference(this@SellerProductDetail, "language") == "ar") {
            sellerProductDetailBinding.backbtn.rotation = 180f
        }

        initPagination()
        initListeners()
        initViewModel()
        initHistoryModel()
        productDetailViewModel.productId = intent?.getStringExtra(PRODUCT_ID) ?: ""
        hitProductDetailApi()
        historyApi()
    }

    private fun hitProductDetailApi() {
        productDetailViewModel.request.productId = productDetailViewModel.productId
        productDetailViewModel.request.token =
            Preferences.getStringPreference(this@SellerProductDetail, TOKEN)
        productDetailViewModel.getProductDetailRequest()
    }

    private fun historyApi() {
        bidHighestBiddingHistoryModelViewModel?.request?.productId =
            intent?.getStringExtra(PRODUCT_ID)
        bidHighestBiddingHistoryModelViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerProductDetail, TOKEN)
        bidHighestBiddingHistoryModelViewModel?.getHighestBiddingRequest()
    }

    private fun initHistoryModel() {
        bidHighestBiddingHistoryModelViewModel?.request?.productId =
            Preferences.getStringPreference(this@SellerProductDetail, "")
        bidHighestBiddingHistoryModelViewModel =
            ViewModelProvider(this@SellerProductDetail)[BidHighestBiddingHistoryViewModel::class.java]
        sellerProductDetailBinding.lifecycleOwner = this@SellerProductDetail
        bidHighestBiddingHistoryModelViewModel?.getBidHighestBidHistoryData()
            ?.observe(this@SellerProductDetail) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            sellerProductDetailBinding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(false)
                                scrollView.setVisibility(true)
                            }
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                BidHighestBiddingHistoryResponseModel::class.java
                            )
                            sellerProductDetailBinding.idProgress.setVisibility(false)
                            when (data.status) {
                                200 -> {
                                    Log.d(TAG, "bidHistory: ${Gson().toJson(data.data)}")
                                    ProcessDialog.dismissDialog()
                                    bidHighestBiddingHistoryModelViewModel?.isLastPage =
                                        (data.data?.highestBid?.size
                                            ?: 0) < (bidHighestBiddingHistoryModelViewModel?.limit?.toInt()!!)
                                    ProcessDialog.dismissDialog()
                                    bidHighestBiddingHistoryModelViewModel?.isDataLoading = false
                                    if (data.data?.highestBid?.isEmpty() == true) {
                                        if (isPaging) return@observe
                                        sellerProductDetailBinding.apply {
                                            recyclerBiddingHistory.setVisibility(false)
                                            placeHolder.setVisibility(true)
                                            headingPlaceHolder.setVisibility(true)
                                        }
                                    } else {
                                        sellerProductDetailBinding.apply {
                                            recyclerBiddingHistory.setVisibility(true)
                                            placeHolder.setVisibility(false)
                                            headingPlaceHolder.setVisibility(false)
                                        }
                                    }
                                    val arrHistorySeller: ArrayList<HistoryModel> = arrayListOf()
                                    data.data?.highestBid?.forEachIndexed { _, history ->
                                        arrHistorySeller.add(
                                            HistoryModel(
                                                username = history.userName ?: "",
                                                bidPrice = getString(R.string.sar) + " " + history.amount.toString(),
                                                time = convertDateTime(history.bidCreatedDate.toString()),
                                                cardViewText = getInitials(history.userName.toString())
                                            )
                                        )
                                    }

                                    if (recyclerHistory == null || recyclerHistory?.arrHistory?.isEmpty() == true) {
                                        recyclerHistory = RecyclerBiddingHistory(
                                        )
                                        sellerProductDetailBinding.recyclerBiddingHistory.adapter =
                                            recyclerHistory
                                        recyclerHistory?.addItems(arrHistorySeller)
                                    } else {
                                        recyclerHistory?.updateItemList(arrHistorySeller)
                                    }

                                    sellerProductDetailBinding.id3dot.setOnClickListener { view ->
                                        showPopupMenu(view, arrHistorySeller.size)
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
                                    Preferences.removePreference(this@SellerProductDetail, "token")
                                    Preferences.removePreference(
                                        this@SellerProductDetail, "user_details"
                                    )
                                    Preferences.removePreference(
                                        this@SellerProductDetail, "isVerified"
                                    )
                                    Preferences.removePreference(this@SellerProductDetail, "roomId")
                                    val signin =
                                        Intent(this@SellerProductDetail, SignIn::class.java)
                                    startActivity(signin)
                                    finishAffinity()
                                }

                                else -> {
                                    ProcessDialog.dismissDialog()
                                    Toast.makeText(
                                        this@SellerProductDetail, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.LOADING -> {
                            if (!isPaging) sellerProductDetailBinding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                        }

                        Status.ERROR -> {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                            Log.e("TAG", "initViewModel: ${it.message}")
                            ProcessDialog.dismissDialog()
                            sellerProductDetailBinding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                            Toast.makeText(this@SellerProductDetail, it.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    sellerProductDetailBinding.apply {
                        productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                        scrollView.setVisibility(false)
                    }
                    Log.d("error", e.message.toString())
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        if (!this::productDetailViewModel.isInitialized) {
            productDetailViewModel =
                ViewModelProvider(this@SellerProductDetail)[ProductDetailsViewModel::class.java]
            sellerProductDetailBinding.lifecycleOwner = this@SellerProductDetail
        }
        productDetailViewModel.getProductDetailData().observe(this@SellerProductDetail) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        sellerProductDetailBinding.apply {
                            productDetailShimmer.productDetailMainShimmer.setVisibility(false)
                            scrollView.setVisibility(true)
                            markAsFeaturedButton.setVisibility(true)
                        }
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ProductDetailsResponseModel::class.java
                        )
                        Log.d("TAG", "sellerProductDetailsResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                productDetails = data.data?.product
                                Log.d("TAG", "productDetailsData: $productDetails")
                                Preferences.setStringPreference(
                                    this@SellerProductDetail, PRODUCT_ID, data.data?.product?.id
                                )
                                productId = data.data?.product?.id
                                subCategoryId = data.data?.product?.subCategoryId?.id
                                endDate = data.data?.product?.endDate
                                startDate = data.data?.product?.startDate
                                Log.d(
                                    TAG,
                                    "markFeature: $productId $subCategoryId $endDate $startDate "
                                )

                                sellerProductDetailBinding.apply {
                                    subCategoryName.text = data.data?.product?.subCategoryId?.enName
                                    location.text = data.data?.product?.productLocation
                                    productPrice.text =
                                        getString(R.string.sar) + " " + data.data?.product?.price
                                    description.text = data.data?.product?.description
                                    val quantity = data.data?.product?.quantity ?: ""
                                    isFeatured.setVisibility(
                                        data?.data?.product?.isFeatured ?: false
                                    )
                                    val unit = data.data?.product?.unit?.lowercase() ?: ""
                                    productQuantity.text = "$quantity $unit"
                                    val timeLeft = timeLeft(
                                        data.data?.product?.endDate.toString(),
                                        this@SellerProductDetail
                                    )

                                    val isExpired =
                                        timeLeft?.trim()?.lowercase() == getString(R.string.expired)
                                    Log.d(
                                        TAG, "initViewModel: ${
                                            isEndDateInRange(
                                                endDate ?: "", startDate ?: ""
                                            )
                                        }"
                                    )
                                    if (isEndDateInRange(endDate ?: "", startDate ?: "")) {
                                        markAsFeaturedButton.setVisibility(false)
                                    }
                                    if (isExpired) {
                                        timeDuration.text = getString(R.string.product_expired)
                                        markAsFeaturedButton.setVisibility(false)
                                    } else {
                                        timeDuration.text = timeLeft
                                        markAsFeaturedButton.setVisibility(true)
                                    }
                                    val recyclerAdapter = RecyclerImageSliderAdapter(
                                        this@SellerProductDetail,
                                        data.data?.product?.imageUrl as MutableList<String>
                                    )
                                    sellerProductDetailBinding.productImage.adapter =
                                        recyclerAdapter
                                    val helper: SnapHelper = LinearSnapHelper()
                                    helper.attachToRecyclerView(productImage)
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
                                Preferences.removePreference(this@SellerProductDetail, "token")
                                Preferences.removePreference(
                                    this@SellerProductDetail, "user_details"
                                )
                                Preferences.removePreference(this@SellerProductDetail, "isVerified")
                                Preferences.removePreference(this@SellerProductDetail, "roomId")
                                val signin = Intent(this@SellerProductDetail, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@SellerProductDetail, data.message, Toast.LENGTH_SHORT
                                ).show()
                                sellerProductDetailBinding.apply {
                                    productDetailShimmer.productDetailMainShimmer.setVisibility(
                                        false
                                    )
                                    scrollView.setVisibility(true)
                                    markAsFeaturedButton.setVisibility(true)
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                        sellerProductDetailBinding.apply {
                            productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                            scrollView.setVisibility(false)
                            markAsFeaturedButton.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        sellerProductDetailBinding.apply {
                            productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                            scrollView.setVisibility(false)
                            markAsFeaturedButton.setVisibility(false)

                        }
                        Toast.makeText(this@SellerProductDetail, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        sellerProductDetailBinding.apply {
            shareProduct.setOnClickListener {
                shareProduct.isEnabled = false
                Handler().postDelayed({
                    shareProduct.isEnabled = true
                }, 2000)

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "http://13.235.137.221:6002/api/v1/seller/product/details/id?productId=${
                            intent?.getStringExtra(
                                PRODUCT_ID
                            )
                        }"
                    )
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            backbtn.setOnClickListener {
                finish()
            }

            markAsFeaturedButton.setOnClickListener {
                val currentDate =
                    if (CommonUtils.isStartDateStringBeforeCurrentDate(startDate ?: "")) {
                        CommonUtils.getCurrentDateInUTCString()
                    } else {
                        startDate
                    }

                if (markAsFeature?.isAdded != true) {
                    markAsFeature = MarkAsFeature(productId, subCategoryId, endDate, startDate)
                    markAsFeature?.show(supportFragmentManager, markAsFeature?.tag)
                }
            }
        }
    }
    private fun showPopupMenu(view: View, size: Int) {
        popupMenu = PopupMenu(this, view, Gravity.END)
        popupMenu?.menuInflater?.inflate(R.menu.seller_item_product_detail, popupMenu?.menu)

        if (size > 0) popupMenu?.menu?.getItem(1)?.setVisible(false)
        else popupMenu?.menu?.getItem(1)?.setVisible(true)

        popupMenu?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_bid_time -> {
                    val editAuctionTime = AuctionTime(
                        productDetails,
                        isFromEdit = true,
                        successCallBack = ::handleSuccess,
                        isFinish = true
                    )
                    Log.d("TAG", "showPopupMenu: $productDetails")
                    editAuctionTime.show(supportFragmentManager, editAuctionTime.tag)
                    true
                }

                R.id.remove_product -> {
                    val removeProduct =
                        RemoveProduct(productDetailViewModel.productId, ::handleSuccess)
                    removeProduct.show(supportFragmentManager, removeProduct.tag)
                    true
                }

                else -> false
            }
        }

        if (popupMenu?.menu?.hasVisibleItems() == true) {
            popupMenu?.show()
        }
    }

    private fun handleSuccess() {
        ProcessDialog.dismissDialog()
        hitProductDetailApi()
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("status", "update")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.seller_item_product_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return when (item.itemId) {
            R.id.edit_bid_time -> true
            R.id.remove_product -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initPagination() {
        sellerProductDetailBinding.recyclerBiddingHistory.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidHighestBiddingHistoryModelViewModel?.isLastPage!! && !bidHighestBiddingHistoryModelViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidHighestBiddingHistoryModelViewModel?.page =
                        bidHighestBiddingHistoryModelViewModel?.page!! + 1
                    bidHighestBiddingHistoryModelViewModel?.limit =
                        bidHighestBiddingHistoryModelViewModel?.limit!!
                    bidHighestBiddingHistoryModelViewModel?.isDataLoading = true
                    bidHighestBiddingHistoryModelViewModel?.request?.token =
                        Preferences.getStringPreference(this@SellerProductDetail, TOKEN)
                    bidHighestBiddingHistoryModelViewModel?.request?.apply {
                        this.page = bidHighestBiddingHistoryModelViewModel?.page?.toString()
                        this.limit = bidHighestBiddingHistoryModelViewModel?.limit?.toString()
                    }
                    bidHighestBiddingHistoryModelViewModel?.getHighestBiddingRequest()
                    sellerProductDetailBinding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}
