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
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.databinding.ActivityViewAllFeaturedProductBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetFeaturedProductResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.hideKeyboard
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.GetFeaturedProductViewModel

class ViewAllFeaturedProduct : BaseActivity(), RecyclerFeaturedProductAdapter.onClickListner {
    private lateinit var binding: ActivityViewAllFeaturedProductBinding
    private var recyclerFeatAdapter: RecyclerFeaturedProductAdapter? = null
    private var featuredProductViewModel: GetFeaturedProductViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var isPaging: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@ViewAllFeaturedProduct, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_all_featured_product)

        if (Preferences.getStringPreference(this@ViewAllFeaturedProduct, "language").equals("ar")) {
            binding.backwardImg.rotation = 180f
        }

        initPagination()
        initViewModel()
        intiAddWishListViewModel()
        initListener()
        featuredListingApi()
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel = ViewModelProvider(this)[AddWishListViewModel::class.java]
        binding.lifecycleOwner = this
        addWishListViewModel?.getAddWishListData()?.observe(this@ViewAllFeaturedProduct) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            Toast.makeText(
                                this@ViewAllFeaturedProduct, data.message, Toast.LENGTH_SHORT
                            ).show()
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@ViewAllFeaturedProduct, "token")
                            Preferences.removePreference(
                                this@ViewAllFeaturedProduct, "user_details"
                            )
                            Preferences.removePreference(this@ViewAllFeaturedProduct, "isVerified")
                            Preferences.removePreference(this@ViewAllFeaturedProduct, "roomId")
                            val signin = Intent(this@ViewAllFeaturedProduct, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(
                                    supportFragmentManager, accountBlocked?.tag
                                )
                            }
                        }

                        else -> {
                            Toast.makeText(
                                this@ViewAllFeaturedProduct, data.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@ViewAllFeaturedProduct, it.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun featuredListingApi() {
        featuredProductViewModel?.page = 1
        recyclerFeatAdapter?.clearData()
        featuredProductViewModel?.request?.token =
            Preferences.getStringPreference(this@ViewAllFeaturedProduct, TOKEN)
        featuredProductViewModel?.getFeaturedProductRequest()
    }

    private fun initViewModel() {
        featuredProductViewModel =
            ViewModelProvider(this@ViewAllFeaturedProduct)[GetFeaturedProductViewModel::class.java]
        binding.lifecycleOwner = this@ViewAllFeaturedProduct
        featuredProductViewModel?.getFeaturedProductData1()?.observe(this@ViewAllFeaturedProduct) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.myProductShimmer.idMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetFeaturedProductResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        Log.d("TAG", "featuredProductResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                featuredProductViewModel?.isLastPage = (data.data?.highestBid?.size
                                    ?: 0) < (featuredProductViewModel!!.limit.toInt())
                                featuredProductViewModel!!.isDataLoading = false
                                if (isPaging) return@observe

                                val arrFeatureViewAllImg: ArrayList<FeaturedProductModel> =
                                    arrayListOf()
                                data.data?.highestBid?.forEachIndexed { _, product ->
                                    arrFeatureViewAllImg.add(
                                        FeaturedProductModel(
                                            sellerId = product?.productDetails?.sellerId ?: "",
                                            id = product?.productDetails?._id ?: "",
                                            vegetableImg = product?.productDetails?.imageUrl ?: "",
                                            vegetableName = product?.subCategoryName?.enName,
                                            location = product?.productDetails?.productLocation,
                                            vegetableWeight = product?.productDetails?.quantity.toString() + " " + product?.productDetails?.unit,
                                            time = timeLeft(
                                                product?.productDetails?.endDate.toString(),
                                                this@ViewAllFeaturedProduct
                                            ),
                                            wishlist = product?.isWishlist ?: false,
                                            vegetablePrice = getString(R.string.sar) + " " + product?.productDetails?.price,
                                            isBid = product?.productDetails?.hasUserBid ?: false,
                                            categoryName = product?.subCategoryName?.enName?.trim()
                                        )
                                    )
                                }

                                if (recyclerFeatAdapter == null || recyclerFeatAdapter?.arrFeaturedProducts?.isEmpty() == true) {
                                    recyclerFeatAdapter =
                                        RecyclerFeaturedProductAdapter("horizontal")
                                    binding.featuredViewallRecycler.adapter = recyclerFeatAdapter
                                    recyclerFeatAdapter?.onClickListner1(this@ViewAllFeaturedProduct)
                                    recyclerFeatAdapter?.addItems(arrFeatureViewAllImg)
                                } else {
                                    recyclerFeatAdapter?.updateItem(arrFeatureViewAllImg)
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
                                Preferences.removePreference(this@ViewAllFeaturedProduct, "token")
                                Preferences.removePreference(
                                    this@ViewAllFeaturedProduct, "user_details"
                                )
                                Preferences.removePreference(
                                    this@ViewAllFeaturedProduct, "isVerified"
                                )
                                Preferences.removePreference(this@ViewAllFeaturedProduct, "roomId")
                                val signin = Intent(this@ViewAllFeaturedProduct, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.myProductShimmer.idMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@ViewAllFeaturedProduct, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            binding.myProductShimmer.idMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.myProductShimmer.idMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        Toast.makeText(this@ViewAllFeaturedProduct, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    fun initListener() {
        binding.backwardImg.setOnClickListener {
            finish()
        }

        binding.searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                binding.searchLayout.clearFocus()
                hideKeyboard(
                    binding.searchLayout, this@ViewAllFeaturedProduct
                )
                featuredProductViewModel?.request?.search = query.trim()
                featuredProductViewModel?.request?.page = "1"
                featuredProductViewModel?.page = 1
                recyclerFeatAdapter?.clearData()
                featuredListingApi()
                return false
            }
        })


        binding.searchTab.setOnClickListener {
            binding.apply {
                heading.visibility = View.GONE
                searchTab.visibility = View.GONE
                search.visibility = View.VISIBLE
            }
        }

        binding.cancelButton.setOnClickListener {
            binding.searchLayout.setQuery("", false)
            featuredProductViewModel?.request?.search = null
            featuredProductViewModel?.page = 1
            recyclerFeatAdapter?.clearData()
            featuredListingApi()

            binding.apply {
                heading.visibility = View.VISIBLE
                searchTab.visibility = View.VISIBLE
                search.visibility = View.GONE
            }
        }


        binding.idRefreshLayout.setOnRefreshListener {
            featuredListingApi()
            featuredProductViewModel?.page = 1
            featuredProductViewModel?.request?.page = featuredProductViewModel?.page.toString()
            featuredProductViewModel?.limit = featuredProductViewModel?.limit!!
            featuredProductViewModel?.request?.token = Preferences.getStringPreference(this, TOKEN)
            recyclerFeatAdapter?.clearData()
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(this@ViewAllFeaturedProduct, BidderProductDetails::class.java)
        toProductDetails.putExtra(
            PRODUCT_ID, recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toProductDetails.putExtra(
            Constants.SELLER_ID,
            recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.sellerId
        )
        startActivity(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(this@ViewAllFeaturedProduct, PlaceBid::class.java)
        toPlaceBid.putExtra(
            PRODUCT_ID, recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toPlaceBid.putExtra(
            SUB_CATEGORY_NAME,
            recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.categoryName
        )
        startActivity(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId =
            recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        addWishListViewModel?.request?.token =
            Preferences.getStringPreference(this@ViewAllFeaturedProduct, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.wishlist =
            recyclerFeatAdapter?.arrFeaturedProducts?.getOrNull(position)?.wishlist != true
        recyclerFeatAdapter?.notifyItemChanged(position)
    }

    private fun initPagination() {
        binding.featuredViewallRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!featuredProductViewModel?.isLastPage!! && !featuredProductViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    featuredProductViewModel?.page = featuredProductViewModel?.page!! + 1
                    featuredProductViewModel?.limit = featuredProductViewModel?.limit!!
                    featuredProductViewModel?.request?.token =
                        Preferences.getStringPreference(this@ViewAllFeaturedProduct, TOKEN)
                    featuredProductViewModel?.isDataLoading = true
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                    featuredProductViewModel?.request?.token =
                        Preferences.getStringPreference(this@ViewAllFeaturedProduct, TOKEN)
                    featuredProductViewModel?.request.apply {
                        this?.page = featuredProductViewModel?.page.toString()
                        this?.limit = featuredProductViewModel?.limit.toString()
                    }
                    featuredProductViewModel?.getFeaturedProductRequest()
                }
            }
        })
    }
}