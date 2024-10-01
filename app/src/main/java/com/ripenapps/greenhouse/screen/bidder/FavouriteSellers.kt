package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
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
import com.ripenapps.greenhouse.databinding.ActivityFavouriteSellersBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.BestSellerModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.GetFavoriteSellerResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidderGetFavoriteSellerViewModel

class FavouriteSellers : BaseActivity(), RecyclerBestSellersAdapter.onClickListner {
    private lateinit var favouritebinding: ActivityFavouriteSellersBinding

    private var bidderGetFavoriteSellerViewModel: BidderGetFavoriteSellerViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var recyclerFavouriteAdapter: RecyclerBestSellersAdapter? = null
    private var isPaging: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@FavouriteSellers, R.color.status_bar)
        favouritebinding = DataBindingUtil.setContentView(this, R.layout.activity_favourite_sellers)
        if (Preferences.getStringPreference(this@FavouriteSellers, "language").equals("ar")) {
            favouritebinding.backwardImgFavourite.rotation = 180f
        }
        initPagination()
        initViewModel()
        initListeners()

        val searchEditText =
            favouritebinding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    override fun onStart() {
        super.onStart()
        bidderGetFavoriteSellerViewModel?.request?.page = "1"
        bidderGetFavoriteSellerViewModel?.page = 1
        recyclerFavouriteAdapter?.clearData()
        favSellerApi()
    }

    private fun favSellerApi() {
        bidderGetFavoriteSellerViewModel?.request?.token =
            Preferences.getStringPreference(this@FavouriteSellers, TOKEN)
        bidderGetFavoriteSellerViewModel?.getBidderGetFavoriteSellerRequest()
    }

    private fun initListeners() {
        favouritebinding.backwardImgFavourite.setOnClickListener {
            finish()
        }

        favouritebinding.idRefreshLayout.setOnRefreshListener {
            bidderGetFavoriteSellerViewModel?.page = 1
            bidderGetFavoriteSellerViewModel?.request?.page =
                bidderGetFavoriteSellerViewModel?.page.toString()
            bidderGetFavoriteSellerViewModel?.limit = bidderGetFavoriteSellerViewModel?.limit!!
            bidderGetFavoriteSellerViewModel?.request?.token =
                Preferences.getStringPreference(this, TOKEN)
            recyclerFavouriteAdapter?.clearData()
            favSellerApi()
        }

        favouritebinding.apply {
            searchIcon.setOnClickListener {
                myWishlistText.visibility = View.GONE
                backwardImgFavourite.visibility = View.GONE
                searchIcon.visibility = View.GONE
                search.visibility = View.VISIBLE
            }

            searchLayout.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE
                    bidderGetFavoriteSellerViewModel?.request?.search = query.trim()
                    bidderGetFavoriteSellerViewModel?.request?.page = "1"
                    bidderGetFavoriteSellerViewModel?.page = 1
                    recyclerFavouriteAdapter?.clearData()
                    favSellerApi()
                    return false
                }

            })

            cancelButton.setOnClickListener {
                myWishlistText.visibility = View.VISIBLE
                backwardImgFavourite.visibility = View.VISIBLE
                searchIcon.visibility = View.VISIBLE
                search.visibility = View.GONE
                bidderGetFavoriteSellerViewModel?.request?.search = null
                bidderGetFavoriteSellerViewModel?.request?.page = "1"
                bidderGetFavoriteSellerViewModel?.page = 1
                recyclerFavouriteAdapter?.clearData()
                favSellerApi()
            }
        }
    }

    private fun initViewModel() {
        bidderGetFavoriteSellerViewModel =
            ViewModelProvider(this)[BidderGetFavoriteSellerViewModel::class.java]
        favouritebinding.lifecycleOwner = this
        bidderGetFavoriteSellerViewModel?.getBidderGetFavoriteSellerData()
            ?.observe(this@FavouriteSellers) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            favouritebinding.idRefreshLayout.setVisibility(true)
                            favouritebinding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(
                                false
                            )
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                GetFavoriteSellerResponseModel::class.java
                            )
                            favouritebinding.idProgress.setVisibility(false)
                            Log.d("TAG", "initViewModel: $data")
                            when (data.status) {
                                200 -> {
                                    favouritebinding.idRefreshLayout.isRefreshing = false
                                    bidderGetFavoriteSellerViewModel?.isLastPage =
                                        (data.data.sellers.size) < (bidderGetFavoriteSellerViewModel?.limit?.toInt()!!)
                                    bidderGetFavoriteSellerViewModel?.isDataLoading = false
                                    if (data.data.sellers.isEmpty()) {
                                        if (isPaging) return@observe
                                        favouritebinding.apply {
                                            myFavouriteSellerRecycler.visibility = View.GONE
                                            placeHolder.visibility = View.VISIBLE
                                            headingPlaceHolder.visibility = View.VISIBLE
                                            placeHolderDescription.visibility = View.VISIBLE
                                        }
                                        return@observe
                                    } else {
                                        favouritebinding.apply {
                                            myFavouriteSellerRecycler.visibility = View.VISIBLE
                                            placeHolder.visibility = View.GONE
                                            headingPlaceHolder.visibility = View.GONE
                                            placeHolderDescription.visibility = View.GONE
                                        }
                                    }
                                    val arrBestSeller: ArrayList<BestSellerModel> = arrayListOf()
                                    data.data.sellers.forEachIndexed { _, seller ->
                                        val initials = getNameInitials(seller.name)
                                        arrBestSeller.add(
                                            BestSellerModel(
                                                sellerId = seller.id,
                                                circleText = initials,
                                                ratingText = seller.averageRating ?: "0.0",
                                                userNameText = seller.userName,
                                                locationSeller = seller.address,
                                                countHeartCount = seller.favoriteCount ?: 0,
                                                categories = seller.categories
                                            )
                                        )
                                    }
//                                    var recyclerFavouriteAdapter = RecyclerBestSellersAdapter()
//                                    recyclerFavouriteAdapter.onClickListner2(this)
//                                    recyclerFavouriteAdapter.addItems(arrBestSeller)
//                                    favouritebinding.myFavouriteSellerRecycler.adapter =
//                                        recyclerFavouriteAdapter


                                    if (recyclerFavouriteAdapter == null || recyclerFavouriteAdapter?.arrBestSellers?.isEmpty() == true) {
                                        recyclerFavouriteAdapter =
                                            RecyclerBestSellersAdapter("horizontal")
                                        recyclerFavouriteAdapter?.onClickListner2(this)
                                        favouritebinding.myFavouriteSellerRecycler.adapter =
                                            recyclerFavouriteAdapter
                                        recyclerFavouriteAdapter?.addItems(arrBestSeller)
                                    } else {
                                        recyclerFavouriteAdapter?.updateItem(arrBestSeller)
                                    }
                                    Log.d("TAG", "arrBestSellerSize: " + arrBestSeller.size)
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
                                    Preferences.removePreference(this@FavouriteSellers, "token")
                                    Preferences.removePreference(
                                        this@FavouriteSellers,
                                        "user_details"
                                    )
                                    Preferences.removePreference(
                                        this@FavouriteSellers,
                                        "isVerified"
                                    )
                                    Preferences.removePreference(this@FavouriteSellers, "roomId")
                                    val signin = Intent(this@FavouriteSellers, SignIn::class.java)
                                    startActivity(signin)
                                    finishAffinity()
                                }

                                else -> {
                                    favouritebinding.idRefreshLayout.setVisibility(true)
                                    favouritebinding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(
                                        false
                                    )
                                    Toast.makeText(
                                        this@FavouriteSellers, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.LOADING -> {
                            if (!isPaging) {
                                favouritebinding.idRefreshLayout.setVisibility(false)
                                favouritebinding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(
                                    true
                                )
                                favouritebinding.placeHolder.setVisibility(false)
                                favouritebinding.placeHolderDescription.setVisibility(false)
                                favouritebinding.headingPlaceHolder.setVisibility(false)
                            }
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            favouritebinding.idRefreshLayout.setVisibility(false)
                            favouritebinding.idSellerLayoutShimmer.idSellerLayoutMainShimmer.setVisibility(
                                true
                            )
                            favouritebinding.placeHolder.setVisibility(false)
                            favouritebinding.placeHolderDescription.setVisibility(false)
                            favouritebinding.headingPlaceHolder.setVisibility(false)
                            Toast.makeText(this@FavouriteSellers, it.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    override fun clickBestSeller(position: Int) {
        val toAboutSeller = Intent(this, AboutSeller::class.java)
        toAboutSeller.putExtra(
            SELLER_ID, recyclerFavouriteAdapter?.arrBestSellers?.getOrNull(position)?.sellerId
        )
        toAboutSeller.putExtra("fromFav", 1)
        startActivity(toAboutSeller)
    }

    private fun getNameInitials(name: String): String {
        val words = name.split(" ")
        val initials = StringBuilder()
        for (word in words) {
            if (word.isNotEmpty()) {
                initials.append(word[0].uppercase())
            }
        }
        return initials.toString()
    }

    private fun initPagination() {
        favouritebinding.myFavouriteSellerRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidderGetFavoriteSellerViewModel?.isLastPage!! && !bidderGetFavoriteSellerViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidderGetFavoriteSellerViewModel?.page =
                        bidderGetFavoriteSellerViewModel?.page!! + 1
                    bidderGetFavoriteSellerViewModel?.limit =
                        bidderGetFavoriteSellerViewModel?.limit!!
                    bidderGetFavoriteSellerViewModel?.isDataLoading = true
//                        binding.loader.setVisibility(bidderGetFavoriteSellerViewModel?.limit != 0f)
                    favouritebinding.idProgress.setVisibility(false)
                    isPaging = true
                    bidderGetFavoriteSellerViewModel?.request?.token =
                        Preferences.getStringPreference(this@FavouriteSellers, TOKEN)
                    bidderGetFavoriteSellerViewModel?.request?.apply {
                        this.page = bidderGetFavoriteSellerViewModel?.page?.toString()
                        this.limit = bidderGetFavoriteSellerViewModel?.limit?.toString()
                    }
                    bidderGetFavoriteSellerViewModel?.getBidderGetFavoriteSellerRequest()


                }
            }
        })
    }
}