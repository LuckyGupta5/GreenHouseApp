package com.ripenapps.greenhouse.screen.seller

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
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerHighestBidListAdapter
import com.ripenapps.greenhouse.databinding.ActivityFeaturedBidDetailsPlacedBidBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.HighestBiddingListModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerHighestBidListResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.featuredTime
import com.ripenapps.greenhouse.utills.CommonUtils.fullDate
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.HighestBidListViewModel

class FeaturedBidDetailsPlacedBid : BaseActivity() {
    private lateinit var binding: ActivityFeaturedBidDetailsPlacedBidBinding
    private val arrHistorySeller: ArrayList<HighestBiddingListModel> = arrayListOf()
    private var highestBidListViewModel: HighestBidListViewModel? = null
    private var accountBlocked: AccountBlocked? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>
    private lateinit var recyclerHistory: RecyclerHighestBidListAdapter
    private var loginData: LoginResponseModel.LoginData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@FeaturedBidDetailsPlacedBid, R.color.status_bar)
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_featured_bid_details_placed_bid)

        if (Preferences.getStringPreference(this@FeaturedBidDetailsPlacedBid, "language") == "ar") {
            binding.backwardImage.rotation = 180f
        }
        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@FeaturedBidDetailsPlacedBid, "user_details"),
            LoginResponseModel.LoginData::class.java
        )
        Log.d("TAG", "productId: ${intent.getStringExtra(PRODUCT_ID)}")
        initListeners()
        initViewModel()
        featuredListingApi()
        initStartActivityInstance()
    }


    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    Log.d("TAG", "$status")
                    if (status?.isNotEmpty() == true && status == "true") {
                        featuredListingApi()
                    }
                }
            }
    }

    private fun featuredListingApi() {
        highestBidListViewModel?.request?.token =
            Preferences.getStringPreference(this@FeaturedBidDetailsPlacedBid, TOKEN)
        highestBidListViewModel?.request?.biddingDate = intent.getStringExtra("biddingDate")
        highestBidListViewModel?.getHighestBidListRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        highestBidListViewModel = ViewModelProvider(this)[HighestBidListViewModel::class.java]
        binding.lifecycleOwner = this
        highestBidListViewModel?.getHighestBidListData()?.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.featuredDetailsMainShimmer.featuredDetailsMainShimmer.setVisibility(false)
                    binding.idRefreshLayout.setVisibility(true)
                    binding.btnPlaceABid.setVisibility(true)
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        SellerHighestBidListResponseModel::class.java
                    )
                    when (data?.status) {
                        200 -> {
                            Log.d("TAG", "FeatureListingResponse: ${data.data}")
                            binding.idRefreshLayout.isRefreshing = false
                            ProcessDialog.dismissDialog()
                            highestBidListViewModel?.isLastPage = (data.data?.highestBid?.size
                                ?: 0) < (highestBidListViewModel?.limit?.toInt()!!)
                            ProcessDialog.dismissDialog()
                            highestBidListViewModel?.isDataLoading = false
                            if (data.data?.highestBid?.isEmpty() == true) {
                                binding.apply {
                                    bidRanking.setVisibility(false)
                                    rankingRecycler.visibility = View.GONE
                                    top.visibility = View.GONE
                                    placeHolder.visibility = View.VISIBLE
                                    headingPlaceHolder.visibility = View.VISIBLE
                                    placeHolderDescription.visibility = View.VISIBLE
                                }
                                return@observe
                            } else {
                                binding.apply {
                                    bidRanking.setVisibility(true)
                                    rankingRecycler.visibility = View.VISIBLE
                                    top.visibility = View.VISIBLE
                                    placeHolder.visibility = View.GONE
                                    headingPlaceHolder.visibility = View.GONE
                                    placeHolderDescription.visibility = View.GONE
                                }
                            }

                            binding.apply {
                                date.text =
                                    fullDate(data.data?.highestBid?.getOrNull(0)?.biddingDate.toString())
                                time.text = featuredTime(data.data?.highestBid?.getOrNull(0)?.biddingDate.toString(), this@FeaturedBidDetailsPlacedBid)
                                if (time.text == "expired") {
                                    time.setVisibility(false)
                                }
                            }
                            initAdapter(data.data?.highestBid)
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
                            val signIn =
                                Intent(this@FeaturedBidDetailsPlacedBid, SignIn::class.java)
                            startActivity(signIn)
                            finishAffinity()
                        }

                        else -> {
                            binding.featuredDetailsMainShimmer.featuredDetailsMainShimmer.setVisibility(false)
                            binding.idRefreshLayout.setVisibility(true)
                            binding.btnPlaceABid.setVisibility(true)
                            Toast.makeText(this, "${data?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    binding.featuredDetailsMainShimmer.featuredDetailsMainShimmer.setVisibility(true)
                    binding.idRefreshLayout.setVisibility(false)
                    binding.btnPlaceABid.setVisibility(false)
                }

                Status.ERROR -> {
                    binding.featuredDetailsMainShimmer.featuredDetailsMainShimmer.setVisibility(true)
                    binding.btnPlaceABid.setVisibility(false)
                    binding.idRefreshLayout.setVisibility(false)
                    Log.e("TAG", "onErrorCallBack: ${it.message}")
                    Toast.makeText(this@FeaturedBidDetailsPlacedBid, it.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initAdapter(highestBid: List<SellerHighestBidListResponseModel.Data.HighestBid>?) {
        arrHistorySeller.clear()
        arrHistorySeller.apply {
            highestBid?.forEachIndexed { index, highestBid ->
                arrHistorySeller.add(
                    HighestBiddingListModel(
                        cardViewText = (index + 1).toString(),
                        username = highestBid.userName ?: "",
                        subCategoryName = highestBid.subCategoryName?.enName ?: "",
                        bidPrice = getString(R.string.sar) + " " + highestBid.amount.toString(),
                        userId = highestBid.userId,
                        productId = highestBid.productId
                    )
                )

                if (highestBid.productId == intent.getStringExtra(PRODUCT_ID)) {
                    binding.btnPlaceABid.setVisibility(false)
                }
            }
        }
        recyclerHistory = RecyclerHighestBidListAdapter(arrHistorySeller, ::handler)
        binding.rankingRecycler.adapter = recyclerHistory
    }

    private fun handler(model: HighestBiddingListModel) {
        if (intent.getStringExtra("fromOngoingFeature") != "1") {
            if (model.userId == loginData?.userDetails?.id) {
                binding.apply {
                    val toPlaceBid =
                        Intent(this@FeaturedBidDetailsPlacedBid, SellerPlaceBid::class.java)
                    toPlaceBid.apply {
                        putExtra(PRODUCT_ID, model.productId)
                        putExtra("biddingDate", intent.getStringExtra("biddingDate"))
                    }
                    getContent.launch(Intent(toPlaceBid))
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            backwardImage.setOnClickListener {
                finish()
            }
            idRefreshLayout.setOnRefreshListener {
                featuredListingApi()
            }

            btnPlaceABid.setOnClickListener {
                val toPlaceBid =
                    Intent(this@FeaturedBidDetailsPlacedBid, SellerPlaceBid::class.java)
                toPlaceBid.apply {
                    putExtra(PRODUCT_ID, intent.getStringExtra(PRODUCT_ID))
                    putExtra("biddingDate", intent.getStringExtra("biddingDate"))
                }
                getContent.launch(Intent(toPlaceBid))
            }
        }
    }
}