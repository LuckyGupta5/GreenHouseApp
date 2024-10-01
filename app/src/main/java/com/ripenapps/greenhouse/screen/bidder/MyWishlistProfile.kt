package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.databinding.ActivityMyWishlistProfileBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderGetWishListNewResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.BidderGetWishlistViewModel

class MyWishlistProfile : BaseActivity(), RecyclerFeaturedProductAdapter.onClickListner {
    private lateinit var myWishlistProfileBinding: ActivityMyWishlistProfileBinding
    private var bidderGetWishlistViewModel: BidderGetWishlistViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var recyclerWishListAdapter: RecyclerFeaturedProductAdapter? = null
    private var isPaging: Boolean = false
    private lateinit var getContent: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@MyWishlistProfile, R.color.status_bar)
        myWishlistProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_my_wishlist_profile)

        if (Preferences.getStringPreference(this@MyWishlistProfile, "language") == "ar") {
            myWishlistProfileBinding.backwardImgWishlist.rotation = 180f
        }

        initPagination()
        initViewModel()
        initListeners()
        intiAddWishListViewModel()
        getWishlistApi()
        initStartActivityInstance()

        val searchEditText =
            myWishlistProfileBinding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        getWishlistApi()
                    }
                }
            }
    }

    private fun getWishlistApi() {
        bidderGetWishlistViewModel?.request?.token =
            Preferences.getStringPreference(this@MyWishlistProfile, TOKEN)
        bidderGetWishlistViewModel?.getBidderGetWishlistRequest()
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel = ViewModelProvider(this)[AddWishListViewModel::class.java]
        myWishlistProfileBinding.lifecycleOwner = this

        addWishListViewModel?.getAddWishListData()?.observe(this@MyWishlistProfile) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            Toast.makeText(this@MyWishlistProfile, data.message, Toast.LENGTH_SHORT)
                                .show()
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
                            Preferences.removePreference(this@MyWishlistProfile, "token")
                            Preferences.removePreference(this@MyWishlistProfile, "user_details")
                            Preferences.removePreference(this@MyWishlistProfile, "isVerified")
                            Preferences.removePreference(this@MyWishlistProfile, "roomId")
                            val signin = Intent(this@MyWishlistProfile, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            Toast.makeText(this@MyWishlistProfile, data.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@MyWishlistProfile, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {

        myWishlistProfileBinding.searchLayout.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                bidderGetWishlistViewModel?.request?.search = query.trim()
                bidderGetWishlistViewModel?.request?.page = "1"
                bidderGetWishlistViewModel?.page = 1
                recyclerWishListAdapter?.clearData()
                getWishlistApi()
                return false
            }
        })

        myWishlistProfileBinding.idRefreshLayout.setOnRefreshListener {
            bidderGetWishlistViewModel?.page = 1
            bidderGetWishlistViewModel?.request?.page = bidderGetWishlistViewModel?.page.toString()
            bidderGetWishlistViewModel?.limit = bidderGetWishlistViewModel?.limit!!
            bidderGetWishlistViewModel?.request?.token =
                Preferences.getStringPreference(this, TOKEN)
            recyclerWishListAdapter?.clearData()
            getWishlistApi()

        }
        //Back  to the profile Page
        myWishlistProfileBinding.apply {
            backwardImgWishlist.setOnClickListener {
                finish()
            }
            searchIcon.setOnClickListener {
                myWishlistText.visibility = View.GONE
                backwardImgWishlist.visibility = View.GONE
                searchIcon.visibility = View.GONE
                search.visibility = View.VISIBLE
            }
            cancelButton.setOnClickListener {
                myWishlistText.visibility = View.VISIBLE
                backwardImgWishlist.visibility = View.VISIBLE
                searchIcon.visibility = View.VISIBLE
                search.visibility = View.GONE
                searchLayout.setQuery("", false)
                bidderGetWishlistViewModel?.request?.search = null
                bidderGetWishlistViewModel?.page = 1
                recyclerWishListAdapter?.clearData()
                bidderGetWishlistViewModel?.request?.token =
                    Preferences.getStringPreference(this@MyWishlistProfile, TOKEN)
                bidderGetWishlistViewModel?.getBidderGetWishlistRequest()
            }
        }
    }

    private fun initViewModel() {
        bidderGetWishlistViewModel = ViewModelProvider(this)[BidderGetWishlistViewModel::class.java]
        myWishlistProfileBinding.lifecycleOwner = this
        bidderGetWishlistViewModel?.getBidderGetWishlistData()?.observe(this@MyWishlistProfile) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        myWishlistProfileBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                        myWishlistProfileBinding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderGetWishListNewResponseModel::class.java
                        )
                        myWishlistProfileBinding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                myWishlistProfileBinding.idRefreshLayout.isRefreshing = false

                                bidderGetWishlistViewModel?.isLastPage = (data.data?.products?.size
                                    ?: 0) < (bidderGetWishlistViewModel?.limit?.toInt()!!)
                                bidderGetWishlistViewModel?.isDataLoading = false
                                if (data.data?.products?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    myWishlistProfileBinding.apply {
                                        myWishListRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }

                                    return@observe
                                } else {
                                    myWishlistProfileBinding.apply {
                                        myWishListRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrMyWishlistAll: ArrayList<FeaturedProductModel> =
                                    arrayListOf()
                                data.data?.products?.forEachIndexed { _, product ->
                                    arrMyWishlistAll.add(
                                        FeaturedProductModel(
                                            categoryName = product.subCategoryId.enName?.trim(),
                                            sellerId = product.sellerId,
                                            id = product.id,
                                            vegetableImg = product.imageUrl,
                                            vegetableName = product.subCategoryId.enName?.trim(),
//                                            if (Preferences.getStringPreference(
//                                                    this@MyWishlistProfile, "language"
//                                                ) == "en"
//                                            )
//                                            else product.subCategoryId.arName?.trim(),
                                            location = product.productLocation,
                                            vegetableWeight = product.quantity.toString() + " " + product.unit,
                                            time = timeLeft(
                                                product.endDate, this@MyWishlistProfile
                                            ),
                                            vegetablePrice = getString(R.string.sar) + " " + product.price,
                                            wishlist = true,
                                            isBid = product.hasUserBid ?: false
                                        )
                                    )
                                }
                                if (recyclerWishListAdapter == null || recyclerWishListAdapter?.arrFeaturedProducts?.isEmpty() == true) {
                                    recyclerWishListAdapter =
                                        RecyclerFeaturedProductAdapter("horizontal")
                                    myWishlistProfileBinding.myWishListRecycler.adapter =
                                        recyclerWishListAdapter
                                    recyclerWishListAdapter?.addItems(arrMyWishlistAll)
                                    recyclerWishListAdapter?.onClickListner1(this@MyWishlistProfile)
                                } else {
                                    recyclerWishListAdapter?.updateItem(arrMyWishlistAll)
                                }
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
                                Preferences.removePreference(this@MyWishlistProfile, "token")
                                Preferences.removePreference(this@MyWishlistProfile, "user_details")
                                Preferences.removePreference(this@MyWishlistProfile, "isVerified")
                                Preferences.removePreference(this@MyWishlistProfile, "roomId")
                                val signin = Intent(this@MyWishlistProfile, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                myWishlistProfileBinding.myProductShimmer.idMainShimmer.setVisibility(
                                    false
                                )
                                myWishlistProfileBinding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@MyWishlistProfile, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            myWishlistProfileBinding.myProductShimmer.idMainShimmer.setVisibility(
                                true
                            )
                            myWishlistProfileBinding.idRefreshLayout.setVisibility(false)
                            myWishlistProfileBinding.placeHolder.setVisibility(false)
                            myWishlistProfileBinding.placeHolderDescription.setVisibility(false)
                            myWishlistProfileBinding.headingPlaceHolder.setVisibility(false)
                        }

                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        myWishlistProfileBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                        myWishlistProfileBinding.idRefreshLayout.setVisibility(false)
                        myWishlistProfileBinding.placeHolder.setVisibility(false)
                        myWishlistProfileBinding.placeHolderDescription.setVisibility(false)
                        myWishlistProfileBinding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@MyWishlistProfile, it.message, Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(this@MyWishlistProfile, BidderProductDetails::class.java)
        toProductDetails.putExtra(
            PRODUCT_ID,
            recyclerWishListAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toProductDetails.putExtra(
            SELLER_ID,
            recyclerWishListAdapter?.arrFeaturedProducts?.getOrNull(position)?.sellerId
        )
        startActivity(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(this@MyWishlistProfile, PlaceBid::class.java)
        toPlaceBid.putExtra(
            PRODUCT_ID, recyclerWishListAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toPlaceBid.putExtra(
            SUB_CATEGORY_NAME,
            recyclerWishListAdapter?.arrFeaturedProducts?.getOrNull(position)?.categoryName
        )
        getContent.launch(toPlaceBid)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId =
            recyclerWishListAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        addWishListViewModel?.request?.token =
            Preferences.getStringPreference(this@MyWishlistProfile, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        recyclerWishListAdapter?.arrFeaturedProducts?.removeAt(position)
        recyclerWishListAdapter?.notifyDataSetChanged()
        if (recyclerWishListAdapter?.arrFeaturedProducts?.isEmpty() == true) {
            myWishlistProfileBinding.apply {
                myWishListRecycler.visibility = View.GONE
                placeHolder.visibility = View.VISIBLE
                headingPlaceHolder.visibility = View.VISIBLE
                placeHolderDescription.visibility = View.VISIBLE
                searchIcon.setVisibility(false)
            }
        } else {
            myWishlistProfileBinding.apply {
                myWishListRecycler.visibility = View.VISIBLE
                placeHolder.visibility = View.GONE
                headingPlaceHolder.visibility = View.GONE
                placeHolderDescription.visibility = View.GONE
                searchIcon.setVisibility(true)
            }
        }
    }

    private fun initPagination() {
        myWishlistProfileBinding.myWishListRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidderGetWishlistViewModel?.isLastPage!! && !bidderGetWishlistViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidderGetWishlistViewModel?.page = bidderGetWishlistViewModel?.page!! + 1
                    bidderGetWishlistViewModel?.limit = bidderGetWishlistViewModel?.limit!!
                    bidderGetWishlistViewModel?.isDataLoading = true

                    isPaging = true
//                        binding.loader.setVisibility(bidderGetWishlistViewModel?.limit != 0f)
                    bidderGetWishlistViewModel?.request?.token =
                        Preferences.getStringPreference(this@MyWishlistProfile, TOKEN)
                    bidderGetWishlistViewModel?.request?.apply {
                        this.page = bidderGetWishlistViewModel?.page?.toString()
                        this.limit = bidderGetWishlistViewModel?.limit?.toString()
                    }
                    bidderGetWishlistViewModel?.getBidderGetWishlistRequest()
                }
            }
        })
    }
}