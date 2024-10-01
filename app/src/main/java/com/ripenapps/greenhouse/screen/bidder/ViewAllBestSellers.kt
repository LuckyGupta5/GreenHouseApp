package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerBestSellersAdapter
import com.ripenapps.greenhouse.databinding.ActivityViewAllBestSellersBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.BestSellerModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.BestSellerFilter
import com.ripenapps.greenhouse.model.bidder.response.GetBestSellerResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.GetBestSellerViewModel

class ViewAllBestSellers : BaseActivity(), RecyclerBestSellersAdapter.onClickListner {
    private lateinit var binding: ActivityViewAllBestSellersBinding
    private var bestSellerViewModel: GetBestSellerViewModel? = null
    private var recyclerFeatAdapter: RecyclerBestSellersAdapter? = null
    var accountBlocked: AccountBlocked? = null
    private var filterModel: FilterModel? = null
    private var isPaging: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@ViewAllBestSellers, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_all_best_sellers)

        if (Preferences.getStringPreference(this@ViewAllBestSellers, "language").equals("ar")) {
            binding.backwardImg.rotation = 180f
        }

        initPagination()
        initListener()
        initViewModel()
        bestSellerApi()
    }

    private fun bestSellerApi() {
        bestSellerViewModel?.page = 1
        recyclerFeatAdapter?.clearData()
        bestSellerViewModel?.request?.token = Preferences.getStringPreference(
            this@ViewAllBestSellers, TOKEN
        )
        bestSellerViewModel?.getBestSellerRequest()
    }

    private fun initViewModel() {
        bestSellerViewModel =
            ViewModelProvider(this@ViewAllBestSellers)[GetBestSellerViewModel::class.java]
        binding.lifecycleOwner = this@ViewAllBestSellers
        bestSellerViewModel?.getBestSellerData()?.observe(this@ViewAllBestSellers) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)

                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetBestSellerResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "myBidsListingResponse: ${data.data}")
                                binding.idRefreshLayout.isRefreshing = false
                                bestSellerViewModel?.isLastPage = (data.data?.bestSellers?.size
                                    ?: 0) < (bestSellerViewModel?.limit!!.toInt())
                                bestSellerViewModel?.isDataLoading = false

                                if (isPaging) return@observe

                                val arrBestSeller: ArrayList<BestSellerModel> = arrayListOf()
                                data.data?.bestSellers?.forEachIndexed { _, bestSeller ->
                                    arrBestSeller.add(
                                        BestSellerModel(
                                            sellerId = bestSeller?.sellerId,
                                            circleText = getInitials(bestSeller?.name.toString()),
                                            ratingText = bestSeller?.averageRating?.toDouble()
                                                .toString(),
                                            userNameText = bestSeller?.userName,
                                            locationSeller = bestSeller?.address,
                                            countHeartCount = bestSeller?.favoriteCount ?: 0,
                                            categories = bestSeller?.categories ?: mutableListOf()
                                        )
                                    )
                                }

                                if (recyclerFeatAdapter == null || recyclerFeatAdapter?.arrBestSellers?.isEmpty() == true) {
                                    recyclerFeatAdapter = RecyclerBestSellersAdapter("horizontal")
                                    binding.bestsellerViewallRecycler.adapter = recyclerFeatAdapter
                                    recyclerFeatAdapter?.addItems(arrBestSeller)
                                    recyclerFeatAdapter?.onClickListner2(this@ViewAllBestSellers)
                                } else {
                                    recyclerFeatAdapter?.updateItem(arrBestSeller)
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
                                val signin = Intent(this@ViewAllBestSellers, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@ViewAllBestSellers, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(this@ViewAllBestSellers, it.message, Toast.LENGTH_SHORT)
                            .show()
                        binding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        binding.idRefreshLayout.setOnRefreshListener {
            bestSellerViewModel?.page = 1
            recyclerFeatAdapter?.clearData()
            bestSellerViewModel?.request?.page = bestSellerViewModel?.page.toString()
            bestSellerViewModel?.limit = bestSellerViewModel?.limit!!
            bestSellerViewModel?.request?.token = Preferences.getStringPreference(this, TOKEN)
            bestSellerApi()
        }

        binding.searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                bestSellerViewModel?.request?.search = query.trim()
                bestSellerViewModel?.request?.page = "1"
                bestSellerViewModel?.page = 1
                recyclerFeatAdapter?.clearData()
                bestSellerApi()
                return false
            }

        })

        binding.apply {
            icSearch.setOnClickListener {
                bestSellerText.visibility = View.GONE
                backwardImg.visibility = View.GONE
                icSearch.visibility = View.GONE
                icFilter.setVisibility(false)
                search.visibility = View.VISIBLE
            }

            cancelButton.setOnClickListener {
                bestSellerText.visibility = View.VISIBLE
                backwardImg.visibility = View.VISIBLE
                icSearch.visibility = View.VISIBLE
                icFilter.setVisibility(true)
                search.visibility = View.GONE
                bestSellerViewModel?.request?.search = null
                bestSellerViewModel?.request?.page = "1"
                bestSellerViewModel?.page = 1
                recyclerFeatAdapter?.clearData()
                bestSellerApi()
            }
        }

        binding.backwardImg.setOnClickListener {
            finish()
        }

        binding.icFilter.setOnClickListener {
            val bestSellerFilter = BestSellerFilter(filterModel, ::handelFilter)
            bestSellerFilter.show(supportFragmentManager, bestSellerFilter.tag)
        }
    }

    override fun clickBestSeller(position: Int) {
        val toAboutSeller = Intent(this@ViewAllBestSellers, AboutSeller::class.java)
        toAboutSeller.putExtra(
            SELLER_ID, recyclerFeatAdapter?.arrBestSellers?.getOrNull(position)?.sellerId
        )
        startActivity(toAboutSeller)
    }

    private fun filter() {
        bestSellerViewModel?.request?.filter = filterModel?.bestSeller
        bestSellerViewModel?.request?.page = "1"
        bestSellerViewModel?.page = 1
        recyclerFeatAdapter?.clearData()
        bestSellerApi()
    }

    private fun handelFilter(model: FilterModel? = null) {
        this@ViewAllBestSellers.filterModel = model
        filter()
    }

    private fun initPagination() {
        binding.bestsellerViewallRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bestSellerViewModel?.isLastPage!! && !bestSellerViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bestSellerViewModel?.page = bestSellerViewModel?.page!! + 1
                    bestSellerViewModel?.limit = bestSellerViewModel?.limit!!
                    bestSellerViewModel?.request?.token =
                        Preferences.getStringPreference(this@ViewAllBestSellers, TOKEN)
                    bestSellerViewModel?.isDataLoading = true
//                        binding.loader.setVisibility(sellerGetMyBidsViewModel?.limit != 0f)
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                    bestSellerViewModel?.request?.token =
                        Preferences.getStringPreference(this@ViewAllBestSellers, TOKEN)
                    bestSellerViewModel?.request.apply {
                        this?.page = bestSellerViewModel?.page.toString()
                        this?.limit = bestSellerViewModel?.limit.toString()
                    }
                    bestSellerViewModel?.getBestSellerRequest()
                }
            }
        })
    }
}