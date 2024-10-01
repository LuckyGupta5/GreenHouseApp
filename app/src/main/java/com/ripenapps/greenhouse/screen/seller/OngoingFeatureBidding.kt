package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerOngoingFeatureAdapter
import com.ripenapps.greenhouse.databinding.ActivityOngoingFeatureBiddingBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.OnGoingFeatureBiddingModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.OngoingFeatureResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.featuredTime
import com.ripenapps.greenhouse.utills.CommonUtils.getCurrentDateFormatted
import com.ripenapps.greenhouse.utills.CommonUtils.getTimeRemaining
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.SellerOngoingFeatureBiddingViewModel

class OngoingFeatureBidding : BaseActivity() {
    private lateinit var binding: ActivityOngoingFeatureBiddingBinding
    private var accountBlocked: AccountBlocked? = null
    private var sellerOngoingFeatureViewModel: SellerOngoingFeatureBiddingViewModel? = null
    private var recyclerAdapter: RecyclerOngoingFeatureAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        changeStatusBarColor(this@OngoingFeatureBidding, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ongoing_feature_bidding)

        if (Preferences.getStringPreference(this@OngoingFeatureBidding, "language") == "ar") {
            binding.backwardImage.rotation = 180f
        }

        initViewModel()
        initListener()
        ongoingBiddingApi()
    }

    private fun initListener() {
        binding.apply {
            backwardImage.setOnClickListener {
                finish()
            }
            idRefreshLayout.setOnRefreshListener {
                sellerOngoingFeatureViewModel?.request?.token =
                    Preferences.getStringPreference(this@OngoingFeatureBidding, TOKEN)
                sellerOngoingFeatureViewModel?.getSellerOngoingFeatureBiddingRequest()
            }
        }
    }

    private fun ongoingBiddingApi() {
        sellerOngoingFeatureViewModel?.request?.token = Preferences.getStringPreference(this, TOKEN)
        sellerOngoingFeatureViewModel?.getSellerOngoingFeatureBiddingRequest()
    }

    private fun initViewModel() {
        sellerOngoingFeatureViewModel =
            ViewModelProvider(this@OngoingFeatureBidding)[SellerOngoingFeatureBiddingViewModel::class.java]
        binding.lifecycleOwner = this@OngoingFeatureBidding
        sellerOngoingFeatureViewModel?.getSellerOngoingFeatureBiddingData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            OngoingFeatureResponseModel::class.java
                        )
                        Log.d("TAG", "ongoingResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                if (data.data?.highestBid?.isEmpty() == true) {
                                    binding.apply {
                                        recycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        recycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }

                                val arrFeature: ArrayList<OnGoingFeatureBiddingModel> =
                                    arrayListOf()
                                data.data?.highestBid?.forEachIndexed { _, myBids ->
                                    arrFeature.add(
                                        OnGoingFeatureBiddingModel(
                                            id = myBids?.id,
                                            img = myBids?.imageUrl ?: "",
                                            subCategoryName = myBids?.subCategoryName?.enName,
                                            location = myBids?.productLocation,
                                            bids = myBids?.totalBids.toString() + " " + getString(R.string.bids),
                                            weight = myBids?.quantity.toString() + " " + myBids?.unit,
                                            bidPrice = this.getString(R.string.sar) + " " + (myBids?.featureBidAmt ?: 0).toString(),
                                            time = getTimeRemaining(this@OngoingFeatureBidding),
                                            position = myBids?.rank.toString(),
                                            subCategoryId = myBids?.subCategoryId
                                        )
                                    )
                                }
                                recyclerAdapter = RecyclerOngoingFeatureAdapter()
                                binding.recycler.adapter = recyclerAdapter
                                recyclerAdapter?.addItems(arrFeature)

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
                                val signin = Intent(this@OngoingFeatureBidding, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }
}
