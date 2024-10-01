package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerBidInProgressAdapter
import com.ripenapps.greenhouse.databinding.ActivityBidInProgressBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.BidProgressModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.BidderBidInProgressResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeftWithoutSecond
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidderBidInProgressViewModel

class BidInProgress : BaseActivity(), RecyclerBidInProgressAdapter.onClickListner {
    private lateinit var binding: ActivityBidInProgressBinding
    private var bidInProgressViewModel: BidderBidInProgressViewModel? = null
    private val arrBidInProgress: ArrayList<BidProgressModel> = arrayListOf()
    private var recyclerAdapter: RecyclerBidInProgressAdapter? = null
    var accountBlocked: AccountBlocked? = null
    private var isPaging: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@BidInProgress, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bid_in_progress)

        if (Preferences.getStringPreference(this@BidInProgress, "language").equals("ar")) {
            binding.closeBidInProgress.rotation = 180f
        }

        initPagination()
        initListeners()
        initViewModel()
        bidInProgressApi()
        pullToRefresh()
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            bidInProgressViewModel?.request?.token =
                Preferences.getStringPreference(this@BidInProgress, TOKEN)
            bidInProgressViewModel?.page = 1
            bidInProgressViewModel?.request?.page = bidInProgressViewModel?.page.toString()
            bidInProgressViewModel?.limit = bidInProgressViewModel?.limit!!
            bidInProgressViewModel?.request?.token =
                Preferences.getStringPreference(this, TOKEN)
            recyclerAdapter?.clearData()
            bidInProgressViewModel?.getBidInProgressRequest()
        }
    }

    private fun bidInProgressApi() {
        bidInProgressViewModel?.request?.token =
            Preferences.getStringPreference(this@BidInProgress, TOKEN)
        bidInProgressViewModel?.getBidInProgressRequest()
    }

    private fun initViewModel() {
        bidInProgressViewModel =
            ViewModelProvider(this@BidInProgress)[BidderBidInProgressViewModel::class.java]
        binding.lifecycleOwner = this@BidInProgress
        bidInProgressViewModel?.getBidInProgressData()?.observe(this@BidInProgress) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderBidInProgressResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG",
                                    "BidderBidInProgressResponseModel: ${Gson().toJson(data.data)}"
                                )
                                binding.idRefreshLayout.isRefreshing = false
                                bidInProgressViewModel?.isLastPage = (data.data?.mybids?.size
                                    ?: 0) < (bidInProgressViewModel?.limit?.toInt()!!)
                                binding.idRefreshLayout.isRefreshing = false
                                if (data.data?.mybids?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        recyclerProgressBids.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        recyclerProgressBids.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                Log.d("TAG", "dataModel: " + data.data)
                                arrBidInProgress.clear()
                                data.data?.mybids?.forEachIndexed { _, myBids ->
                                    arrBidInProgress.add(
                                        BidProgressModel(
                                            bidId = myBids._id,
                                            image = myBids.productDetails?.imageUrl,
                                            name = myBids.productDetails?.subCategory?.enName,
                                            state = myBids.productDetails?.productLocation,
                                            weight = myBids.productDetails?.quantity.toString() + " " + myBids.productDetails?.unit,
                                            timeLeft = timeLeftWithoutSecond(
                                                myBids.productDetails?.endDate.toString(),
                                                this@BidInProgress
                                            ),
                                            money = myBids.productDetails?.productPrice.toString(),
                                            highestBidAmount = getString(R.string.sar) + " " + myBids.highestBidAmount.toString()
                                        )
                                    )
                                }

                                if (recyclerAdapter == null || recyclerAdapter?.arrBisInProgress?.isEmpty() == true) {
                                    recyclerAdapter = RecyclerBidInProgressAdapter()
                                    recyclerAdapter?.bidInProgressListener(this@BidInProgress)
                                    binding.recyclerProgressBids.adapter =
                                        recyclerAdapter
                                    recyclerAdapter?.addItems(arrBidInProgress)
                                } else {
                                    recyclerAdapter?.updateItemList(arrBidInProgress)
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
                                val signin = Intent(this@BidInProgress, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(this@BidInProgress, data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@BidInProgress, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        binding.closeBidInProgress.setOnClickListener {
            finish()
        }
    }

    override fun progressClick(position: Int) {
        val toBidderBiddingDetails = Intent(this@BidInProgress, BidderBiddingDetails::class.java)
        toBidderBiddingDetails.putExtra("bidId", arrBidInProgress.getOrNull(position)?.bidId)
        startActivity(toBidderBiddingDetails)
    }

    private fun initPagination() {
        binding.recyclerProgressBids.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidInProgressViewModel?.isLastPage!! && !bidInProgressViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidInProgressViewModel?.page = bidInProgressViewModel?.page!! + 1
                    bidInProgressViewModel?.limit = bidInProgressViewModel?.limit!!
                    bidInProgressViewModel?.isDataLoading = true
                    bidInProgressViewModel?.request?.token =
                        Preferences.getStringPreference(this@BidInProgress, TOKEN)
                    bidInProgressViewModel?.request?.apply {
                        this.page = bidInProgressViewModel?.page?.toString()
                        this.limit = bidInProgressViewModel?.limit?.toString()
                    }
                    bidInProgressViewModel?.getBidInProgressRequest()
                }
            }
        })
    }
}