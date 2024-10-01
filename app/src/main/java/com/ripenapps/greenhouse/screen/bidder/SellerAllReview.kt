package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerReviewAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerAllReviewBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.ReviewModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.ReviewFilter
import com.ripenapps.greenhouse.model.bidder.response.GetReviewsResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.formatDate
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.GetReviewsViewModel

class SellerAllReview : BaseActivity() {
    private var getReviewViewModel: GetReviewsViewModel? = null
    private lateinit var binding: ActivitySellerAllReviewBinding
    private var arrAllReview: ArrayList<ReviewModel> = arrayListOf()
    private var filterModel: FilterModel? = null
    private var accountBlocked: AccountBlocked? = null
    private var reviewFilter: ReviewFilter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SellerAllReview, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_all_review)

        if (Preferences.getStringPreference(this@SellerAllReview, "language").equals("ar")) {
            binding.closeAllReviews.rotation = 180f
        }

        initViewModel()
        intiListeners()
        reviewApi()
    }

    private fun reviewApi() {
        getReviewViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerAllReview, TOKEN)
        getReviewViewModel?.request?.sellerId = intent?.getStringExtra(Constants.SELLER_ID) ?: ""
        getReviewViewModel?.getReviewsRequest()
    }

    private fun intiListeners() {
        binding.apply {
            closeAllReviews.setOnClickListener {
                finish()
            }

            sortFilter.setOnClickListener {
                if (reviewFilter?.isAdded != true) {
                    reviewFilter = ReviewFilter(filterModel, ::handelFilter)
                    reviewFilter?.show(supportFragmentManager, reviewFilter?.tag)
                }
            }

            idRefreshLayout.setOnRefreshListener {
                reviewApi()
            }
        }
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "handlerData: ${Gson().toJson(filterModel)}")
        this@SellerAllReview.filterModel = model
        filter()
    }

    private fun filter() {
        getReviewViewModel?.request?.sortDirection = filterModel?.reviewFiler
        reviewApi()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        getReviewViewModel =
            ViewModelProvider(this@SellerAllReview)[GetReviewsViewModel::class.java]
        binding.lifecycleOwner = this@SellerAllReview
        getReviewViewModel?.getReviewsData()?.observe(this@SellerAllReview) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.reviewShimmer.reviewMainShimmer.setVisibility(false)
                        binding.linaerLayer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetReviewsResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                arrAllReview.clear()
                                data.data?.reviews?.forEachIndexed { _, reviews ->
                                    arrAllReview.add(
                                        ReviewModel(
                                            initials = reviews.name,
                                            reviewerName = reviews.name,
                                            reviewDay = formatDate(reviews.createdAt.toString()),
                                            reviewStar = reviews.rating.toString(),
                                            review = reviews.review
                                        )
                                    )
                                }

                                binding.apply {
                                    avgRating.text =
                                        data.data?.averageRating?.toString() + " " + getString(R.string.rating)
                                    totalRating.text =
                                        arrAllReview.size.toString() + " " + getString(R.string.reviews)
                                }

                                val recyclerAdapter = RecyclerReviewAdapter(arrAllReview)
                                binding.allReviewsRecycler.adapter = recyclerAdapter
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
                                val signin = Intent(this@SellerAllReview, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.reviewShimmer.reviewMainShimmer.setVisibility(false)
                                binding.linaerLayer.setVisibility(true)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@SellerAllReview, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.reviewShimmer.reviewMainShimmer.setVisibility(true)
                        binding.linaerLayer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.reviewShimmer.reviewMainShimmer.setVisibility(true)
                        binding.linaerLayer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                        Toast.makeText(this@SellerAllReview, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }
}