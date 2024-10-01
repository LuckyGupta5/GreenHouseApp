package com.ripenapps.greenhouse.fragment.bidderfragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.databinding.FragmentProductsBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderProductListLatestResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.BidderProductDetails
import com.ripenapps.greenhouse.screen.bidder.PlaceBid
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.BidderProductListViewModel

class ProductsFragment : Fragment(), RecyclerFeaturedProductAdapter.onClickListner {
    private lateinit var productFragmentBinding: FragmentProductsBinding
    private lateinit var mContext: Context
    private var recyclerProductAdapter: RecyclerFeaturedProductAdapter? = null
    private lateinit var bidderProductListViewModel: BidderProductListViewModel
    private var addWishListViewModel: AddWishListViewModel? = null
    private var bidderProductListData: String? = null
    private var accountBlocked: AccountBlocked? = null
    private var filterModel: FilterModel? = null
    private var myProductFilter: MyProductFilter? = null
    private var isPaging: Boolean = false
    private lateinit var getContent: ActivityResultLauncher<Intent>
    private var highestRange: Float = 0F

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        changeStatusBarColor(this@ProductsFragment, R.color.status_bar)
        productFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_products, container, false)

        initPagination()
        initListener()
        initViewModel()
        intiAddWishListViewModel()
        productListingApi()
        initStartActivityInstance()

        val searchEditText =
            productFragmentBinding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START

        return productFragmentBinding.root
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        bidderProductListViewModel.request.page = "1"
                        bidderProductListViewModel.page = 1
                        recyclerProductAdapter?.clearData()
                        productListingApi()
                    }
                }
            }
    }

    private fun productListingApi() {
        bidderProductListViewModel.request.page = "1"
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        bidderProductListViewModel.request.token = Preferences.getStringPreference(mContext, TOKEN)
        bidderProductListViewModel.getBidderProductListRequest()
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel = ViewModelProvider(this)[AddWishListViewModel::class.java]
        productFragmentBinding.lifecycleOwner = this

        addWishListViewModel?.getAddWishListData()?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }

                        403, 401 -> {
                            Preferences.removePreference(mContext, "token")
                            Preferences.removePreference(mContext, "user_details")
                            Preferences.removePreference(mContext, "isVerified")
                            Preferences.removePreference(mContext, "roomId")
                            val signin = Intent(mContext, SignIn::class.java)
                            startActivity(signin)
                            activity?.finishAffinity()
                        }

                        else -> {
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }
    }

    private fun initViewModel() {
        bidderProductListViewModel = ViewModelProvider(this)[BidderProductListViewModel::class.java]
        productFragmentBinding.lifecycleOwner = this
        bidderProductListViewModel.getBidderProductListData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        productFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                        productFragmentBinding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderProductListLatestResponseModel::class.java
                        )
                        productFragmentBinding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG",
                                    "ProductListingApi: ${Gson().toJson(data.data?.products)}"
                                )
                                productFragmentBinding.idRefreshLayout.isRefreshing = false
                                bidderProductListViewModel.isLastPage = (data.data?.products?.size
                                    ?: 0) < (bidderProductListViewModel.limit.toInt())
                                bidderProductListViewModel.isDataLoading = false
                                bidderProductListData = Gson().toJson(data.data)
                                if (data.data?.products?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    productFragmentBinding.apply {
                                        featuredProductsRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }

                                    return@observe
                                } else {
                                    productFragmentBinding.apply {
                                        featuredProductsRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrProductFragment: ArrayList<FeaturedProductModel> =
                                    arrayListOf()
                                data.data?.products?.forEachIndexed { _, product ->
                                    arrProductFragment.add(
                                        FeaturedProductModel(
                                            categoryName = product.subCategoryId?.enName?.trim(),
                                            sellerId = product.sellerId ?: "",
                                            id = product.id ?: "",
                                            vegetableImg = product.imageUrl ?: "",
                                            vegetableName = product.subCategoryId?.enName?.trim(),
//                                            if (Preferences.getStringPreference(mContext, "language") == "en")
//
//                                            else product.subCategoryId?.arName?.trim(),
                                            location = product.productLocation?.trim(),
                                            vegetableWeight = product.quantity.toString() + " " + product.unit,
                                            time = timeLeft(product.endDate.toString(), mContext),
                                            wishlist = product.isWishlist,
                                            vegetablePrice = getString(R.string.sar) + " " + product.price,
                                            isFeatured = product.isFeatured,
                                            isBid = product.hasUserBid ?: false
                                        )
                                    )
                                }

                                highestRange = data.data?.highestRange?.maxAmount ?: 0F
                                if (bidderProductListViewModel.page == 1 || recyclerProductAdapter == null || recyclerProductAdapter?.arrFeaturedProducts?.isEmpty() == true) {
                                    recyclerProductAdapter =
                                        RecyclerFeaturedProductAdapter("horizontal")
                                    productFragmentBinding.featuredProductsRecycler.adapter =
                                        recyclerProductAdapter
                                    recyclerProductAdapter?.onClickListner1(this)
                                    recyclerProductAdapter?.addItems(arrProductFragment)
                                } else {
                                    recyclerProductAdapter?.updateItem(arrProductFragment)
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(parentFragmentManager, accountBlocked?.tag)
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(mContext, "token")
                                Preferences.removePreference(mContext, "user_details")
                                Preferences.removePreference(mContext, "isVerified")
                                Preferences.removePreference(mContext, "roomId")
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }


                            else -> {
                                productFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                                productFragmentBinding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging){
                            productFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                            productFragmentBinding.idRefreshLayout.setVisibility(false)
                            productFragmentBinding.placeHolder.setVisibility(false)
                            productFragmentBinding.placeHolderDescription.setVisibility(false)
                            productFragmentBinding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        productFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                        productFragmentBinding.idRefreshLayout.setVisibility(false)
                        productFragmentBinding.placeHolder.setVisibility(false)
                        productFragmentBinding.placeHolderDescription.setVisibility(false)
                        productFragmentBinding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                productFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                productFragmentBinding.idRefreshLayout.setVisibility(false)
                Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {

        productFragmentBinding.searchLayout.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                productFragmentBinding.searchLayout.clearFocus()
                activity?.let { CommonUtils.hideKeyboard(productFragmentBinding.searchLayout, it) }
                bidderProductListViewModel.request.search = query.trim()
                bidderProductListViewModel.request.page = "1"
                bidderProductListViewModel.page = 1
                recyclerProductAdapter?.clearData()
                productListingApi()
                return false
            }
        })

        productFragmentBinding.idRefreshLayout.setOnRefreshListener {
            bidderProductListViewModel.page = 1
            bidderProductListViewModel.request.page = bidderProductListViewModel.page.toString()
            bidderProductListViewModel.limit = bidderProductListViewModel.limit
            bidderProductListViewModel.request.token =
                Preferences.getStringPreference(mContext, TOKEN)
            recyclerProductAdapter?.clearData()
            productListingApi()
        }

        productFragmentBinding.searchTab.setOnClickListener {
            productFragmentBinding.apply {
                myProductHeading.visibility = View.GONE
                searchTab.visibility = View.GONE
                myProductFilter.visibility = View.GONE
                search.visibility = View.VISIBLE
            }
        }

        productFragmentBinding.cancelButton.setOnClickListener {
            productFragmentBinding.searchLayout.setQuery("", false)
            bidderProductListViewModel.request.search = null
            bidderProductListViewModel.page = 1
            recyclerProductAdapter?.clearData()
            productListingApi()

            productFragmentBinding.apply {
                myProductHeading.visibility = View.VISIBLE
                searchTab.visibility = View.VISIBLE
                myProductFilter.visibility = View.VISIBLE
                search.visibility = View.GONE
            }
        }

        productFragmentBinding.myProductFilter.setOnClickListener {
            if (myProductFilter?.isAdded != true) {
                myProductFilter = MyProductFilter(filterModel, ::handelFilter, highestRange)
                myProductFilter?.show(parentFragmentManager, myProductFilter?.tag)
            }
        }

        productFragmentBinding.AllLayout.setOnClickListener {
            filterAll()
        }

        productFragmentBinding.vegetableLayout.setOnClickListener {
            filterVegetables()
        }

        productFragmentBinding.fruitsLayout.setOnClickListener {
            filterFruits()
        }

        productFragmentBinding.datesLayout.setOnClickListener {
            filterDates()
        }
    }

    private fun filterVegetables() {
        productFragmentBinding.vegetableLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.green_house_color_border)
        productFragmentBinding.AllLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.fruitsLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.datesLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.vegetableProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.white
            )
        )
        productFragmentBinding.allProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.fruitProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.datesProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        bidderProductListViewModel.request.category = VEGETABLES
        bidderProductListViewModel.request.page = "1"
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        productListingApi()
    }

    private fun filterFruits() {
        productFragmentBinding.fruitsLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.green_house_color_border)
        productFragmentBinding.vegetableLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.AllLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.datesLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.fruitProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.white
            )
        )
        productFragmentBinding.vegetableProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.allProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.datesProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        bidderProductListViewModel.request.category = FRUITS
        bidderProductListViewModel.request.page = 1.toString()
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        productListingApi()
    }

    private fun filterDates() {
        productFragmentBinding.datesLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.green_house_color_border)
        productFragmentBinding.vegetableLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.fruitsLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.AllLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.datesProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.white
            )
        )
        productFragmentBinding.vegetableProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.fruitProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.allProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        bidderProductListViewModel.request.category = DATES
        bidderProductListViewModel.request.page = "1"
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        productListingApi()
    }


    private fun filterAll() {
        productFragmentBinding.AllLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.green_house_color_border)
        productFragmentBinding.vegetableLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.fruitsLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.datesLayout.background =
            ContextCompat.getDrawable(mContext, R.drawable.ebebeb_border)
        productFragmentBinding.allProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.white
            )
        )
        productFragmentBinding.vegetableProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.fruitProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )
        productFragmentBinding.datesProduct.setTextColor(
            ContextCompat.getColor(
                mContext, R.color.black
            )
        )

        bidderProductListViewModel.request.category = null
        bidderProductListViewModel.request.page = "1"
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        productListingApi()
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(mContext, BidderProductDetails::class.java)
        toProductDetails.putExtra(
            PRODUCT_ID, recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toProductDetails.putExtra(
            SELLER_ID, recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.sellerId
        )
        getContent.launch(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(mContext, PlaceBid::class.java)
        toPlaceBid.putExtra(
            PRODUCT_ID, recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        )
        toPlaceBid.putExtra(
            SUB_CATEGORY_NAME,
            recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.categoryName
        )
        getContent.launch(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId =
            recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.id
        addWishListViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.wishlist =
            recyclerProductAdapter?.arrFeaturedProducts?.getOrNull(position)?.wishlist != true
        recyclerProductAdapter!!.notifyItemChanged(position)
    }

    private fun filter() {
        Log.d("TAG", "filter: ${filterModel?.price}")
        bidderProductListViewModel.request.price = filterModel?.price
        bidderProductListViewModel.request.remainingHours = filterModel?.hours
        bidderProductListViewModel.request.page = "1"
        bidderProductListViewModel.page = 1
        recyclerProductAdapter?.clearData()
        productListingApi()
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "myProductListFilter: ${Gson().toJson(filterModel)}")
        this@ProductsFragment.filterModel = model
        filter()
    }

    private fun initPagination() {
        productFragmentBinding.featuredProductsRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidderProductListViewModel.isLastPage && !bidderProductListViewModel.isDataLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidderProductListViewModel.page += 1
                    bidderProductListViewModel.limit = bidderProductListViewModel.limit
                    bidderProductListViewModel.isDataLoading = true
//                        binding.loader.setVisibility(sellerGetMyBidsViewModel?.limit != 0f)
                    productFragmentBinding.idProgress.setVisibility(true)
                    isPaging = true
                    bidderProductListViewModel.request.token =
                        Preferences.getStringPreference(mContext, TOKEN)
                    bidderProductListViewModel.request.apply {
                        this.page = bidderProductListViewModel.page.toString()
                        this.limit = bidderProductListViewModel.limit.toString()
                    }
                    bidderProductListViewModel.getBidderProductListRequest()

                }
            }
        })
    }
}