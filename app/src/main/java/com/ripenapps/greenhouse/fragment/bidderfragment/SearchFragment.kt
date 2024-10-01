package com.ripenapps.greenhouse.fragment.bidderfragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
//import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.bidder.RecentSearchRecycler
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.databinding.FragmentSearchBinding
import com.ripenapps.greenhouse.databinding.ItemLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.datamodels.biddermodel.RecentSearchModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetSearchProductListResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.BidderProductDetails
import com.ripenapps.greenhouse.screen.bidder.PlaceBid
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.below
import com.ripenapps.greenhouse.utills.isValidList
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.SearchProductListViewModel

class SearchFragment : Fragment(), RecyclerFeaturedProductAdapter.onClickListner {
    private lateinit var searchBinding: FragmentSearchBinding
    private val arrFeatureViewAllImg: ArrayList<FeaturedProductModel> = arrayListOf()
    private var recyclerFeatAdapter: RecyclerFeaturedProductAdapter? = null
    private var searchProductListViewModel: SearchProductListViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    private lateinit var recentSearchRecyclerAdapter: RecentSearchRecycler
    private lateinit var mContext: Context
    private var status: Int = -1
    var accountBlocked: AccountBlocked? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>
    private var arrRecentSearches: ArrayList<RecentSearchModel> = arrayListOf()
    private var fromSearch = false
    var backPressed = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        changeStatusBarColor(this@SearchFragment, R.color.status_bar)
        searchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

        initViewModel()
        initListener()
        searchProductListingApi()
        initStartActivityInstance()
        intiAddWishListViewModel()
        checkSearchViewFocus()
        setBack()
        if (!isBindingInitialized && status != -1) {
            callBackFromHome(status)
        }

        val searchEditText =
            searchBinding.searchTab.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        return searchBinding.root
    }

    private fun setBack() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressed) {
                    activity?.finish()
                }
                searchBinding.cancelButton.performClick()
                Handler(Looper.getMainLooper()).postDelayed({ backPressed = false }, 2000)
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel =
            ViewModelProvider(this@SearchFragment)[AddWishListViewModel::class.java]
        searchBinding.lifecycleOwner = this@SearchFragment
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

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        searchProductListingApi()
                    }
                }
            }
    }

    private fun initListener() {

        searchBinding.apply {
            idRefreshLayout.setOnRefreshListener {
                searchProductListViewModel?.page = 1
                searchProductListViewModel?.request?.page =
                    searchProductListViewModel?.page.toString()
                searchProductListViewModel?.limit = searchProductListViewModel?.limit!!
                recyclerFeatAdapter?.clearData()
                searchProductListingApi()
            }

            searchTab.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }

                @SuppressLint("SetTextI18n")
                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE
                    searchTab.clearFocus()
                    activity?.let { CommonUtils.hideKeyboard(searchTab, it) }
                    initSearch(query)

                    if (!initCheckQueryValidations(query)) {
                        if (arrRecentSearches.size >= 10) {
                            arrRecentSearches.removeAt(arrRecentSearches.size - 1)
                            arrRecentSearches.add(
                                0, RecentSearchModel(searchTab.query.toString())
                            ) //adding recent searches into the array}
                        } else {
                            arrRecentSearches.add(
                                0, RecentSearchModel(searchTab.query.toString())
                            ) //adding recent searches into the array
                        }
                        Preferences.setStringPreference(
                            mContext, "recentSearches", Gson().toJson(arrRecentSearches)
                        )
                        initRecentSearchAdapter()
                        backPressed = false
                    }

                    val result = mContext.resources.getString(R.string.showing_results_for)
                    val textLine = getString(R.string.showing_results_for) + " " + query
                    val ss = SpannableString(textLine)
                    ss.setSpan(
                        StyleSpan(Typeface.BOLD),
                        result.length + 1,
                        textLine.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    searchBinding.resultsFor.text = ss
                    fromSearch = true
                    return false
                }
            })

            cancelButton.setOnClickListener {
                searchTab.clearFocus()
                idRefreshLayout.below(searchBinding.newListingHeading)
                recentSearchRecycler.setVisibility(false)
                searchDesc.setVisibility(true)
                chipGroup.setVisibility(true)
                topSearchHeading.setVisibility(true)
                newListingHeading.setVisibility(true)
                resultsFor.setVisibility(false)
                idRefreshLayout.setVisibility(true)
                searchTab.setQuery("", true)
                searchProductListViewModel?.request?.search = null
                searchProductListViewModel?.request?.searchType = null
                searchProductListViewModel?.page = 1
                recyclerFeatAdapter?.clearData()
                fromSearch = false
                searchProductListingApi()
                backPressed = true
            }
        }
    }

    private fun initCheckQueryValidations(query: String): Boolean {
        if (arrRecentSearches.isEmpty()) return false
        arrRecentSearches.forEach {
            if (it.recentSearch == query) {
                return true
            }
        }
        return false
    }

    private fun checkSearchViewFocus() {
        searchBinding.searchTab.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchBinding.recentSearchRecycler.setVisibility(true)
                searchBinding.searchDesc.setVisibility(false)
                searchBinding.chipGroup.setVisibility(false)
                searchBinding.topSearchHeading.setVisibility(false)
                searchBinding.newListingHeading.setVisibility(false)
                searchBinding.resultsFor.setVisibility(false)
                searchBinding.idRefreshLayout.setVisibility(false)
                searchBinding.placeHolder.setVisibility(false)
                searchBinding.placeHolderDescription.setVisibility(false)
                searchBinding.headingPlaceHolder.setVisibility(false)
                val recentSearch = Preferences.getStringPreference(mContext, "recentSearches") ?: ""
                if (recentSearch.isNotEmpty()) {
                    arrRecentSearches =
                        Gson().fromJson(recentSearch, Array<RecentSearchModel>::class.java)
                            .toCollection(ArrayList())
                    initRecentSearchAdapter()
                }
            } else {
                searchBinding.recentSearchRecycler.setVisibility(false)
                searchBinding.searchDesc.setVisibility(true)
                searchBinding.chipGroup.setVisibility(true)
                searchBinding.topSearchHeading.setVisibility(true)
                searchBinding.newListingHeading.setVisibility(true)
                searchBinding.resultsFor.setVisibility(false)
                searchBinding.idRefreshLayout.setVisibility(true)
                searchBinding.placeHolder.setVisibility(false)
                searchBinding.placeHolderDescription.setVisibility(false)
                searchBinding.headingPlaceHolder.setVisibility(false)
                backPressed = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecentSearchAdapter() {
        if (arrRecentSearches.isEmpty()) return
        try {
            recentSearchRecyclerAdapter = RecentSearchRecycler(arrRecentSearches, ::handler)
            searchBinding.recentSearchRecycler.adapter = recentSearchRecyclerAdapter
            recentSearchRecyclerAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d("TAG", "initRecentSearchAdapter: ${e.message}")
        }
    }

    private fun handler(recentSearchModel: RecentSearchModel, path: String) {
        if (path == "forDelete") {
            val position =
                recentSearchRecyclerAdapter.arrRecentSearch.indexOfFirst { it.recentSearch == recentSearchModel.recentSearch }
            if (recentSearchRecyclerAdapter.arrRecentSearch.isValidList(position)) {
                recentSearchRecyclerAdapter.arrRecentSearch.removeAt(position)
                recentSearchRecyclerAdapter.notifyItemRemoved(position)
            }
            Preferences.setStringPreference(
                mContext, "recentSearches", Gson().toJson(arrRecentSearches)
            )
        } else {
            searchBinding.searchTab.setQuery(recentSearchModel.recentSearch, true)
        }
    }

    fun initSearch(query: String) {
        searchProductListViewModel?.request?.search = query.trim().lowercase()
        searchProductListViewModel?.request?.page = "1"
        searchProductListViewModel?.page = 1
        searchProductListViewModel?.request?.searchType = "global"
        recyclerFeatAdapter?.clearData()
        searchProductListingApi()
    }

    private fun searchProductListingApi() {
        searchProductListViewModel?.request?.token =
            Preferences.getStringPreference(mContext, TOKEN)
        searchProductListViewModel?.getSearchProductListRequest()
    }

    private fun initViewModel() {
        searchProductListViewModel =
            ViewModelProvider(this@SearchFragment)[SearchProductListViewModel::class.java]
        searchBinding.lifecycleOwner = this@SearchFragment
        searchProductListViewModel?.getSearchProductListData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        searchBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                        searchBinding.idRefreshLayout.setVisibility(true)
                        searchBinding.chipGroup.setVisibility(true)
                        searchBinding.topSearchHeading.setVisibility(true)
                        searchBinding.newListingHeading.setVisibility(true)
                        searchBinding.searchDesc.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetSearchProductListResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG", "SearchProductListingApi: ${Gson().toJson(data.data)}"
                                )
                                searchBinding.idRefreshLayout.isRefreshing = false

                                if (data.data?.searchCounts?.isEmpty() == true) {
                                    searchBinding.topSearchHeading.setVisibility(false)
                                    searchBinding.chipGroup.setVisibility(false)
                                } else {
                                    searchBinding.topSearchHeading.setVisibility(true)
                                    searchBinding.chipGroup.setVisibility(true)
                                }

                                if (fromSearch) {
                                    searchBinding.apply {
                                        resultsFor.setVisibility(true)
                                        idRefreshLayout.below(searchBinding.resultsFor)
                                        chipGroup.setVisibility(false)
                                        searchDesc.setVisibility(false)
                                        topSearchHeading.setVisibility(false)
                                        newListingHeading.setVisibility(false)
                                    }
                                } else {
                                    searchBinding.apply {
                                        resultsFor.setVisibility(false)
                                        chipGroup.setVisibility(true)
                                        searchDesc.setVisibility(true)
                                        topSearchHeading.setVisibility(true)
                                        newListingHeading.setVisibility(true)
                                    }
                                }
//                                handleProducts(data.data?.products)
                                if (data.data?.products?.isEmpty() == true) {
                                    searchBinding.apply {
                                        searchProductRecycler.setVisibility(false)
                                        newListingHeading.setVisibility(false)
                                        placeHolder.setVisibility(true)
                                        headingPlaceHolder.setVisibility(true)
                                        placeHolderDescription.setVisibility(true)
                                        resultsFor.setVisibility(false)
                                    }
                                } else {
                                    searchBinding.apply {
                                        searchProductRecycler.setVisibility(true)
                                        placeHolder.setVisibility(false)
                                        headingPlaceHolder.setVisibility(false)
                                        placeHolderDescription.setVisibility(false)
                                    }
                                }
                                arrFeatureViewAllImg.clear()
                                data.data?.products?.forEachIndexed { _, product ->
                                    arrFeatureViewAllImg.add(
                                        FeaturedProductModel(
                                            categoryName = product?.categoryId?.enName,
                                            sellerId = product?.sellerId ?: "",
                                            id = product?._id ?: "",
                                            vegetableImg = product?.imageUrl ?: "",
                                            vegetableName = product?.subCategoryId?.enName,
//                                            if (Preferences.getStringPreference(
//                                                    mContext, "language"
//                                                ) == "en"
//                                            )
//                                            else product?.subCategoryId?.arName,
                                            location = product?.productLocation,
                                            vegetableWeight = product?.quantity.toString() + " " + product?.unit,
                                            time = timeLeft(
                                                product?.endDate.toString(), mContext
                                            ),
                                            wishlist = product?.isWishlist,
                                            vegetablePrice = getString(R.string.sar) + " " + product?.price,
                                            isFeatured = product?.isFeatured,
                                            isBid = product?.hasUserBid ?: false
                                        )
                                    )
                                }
                                handleTopSearches(data.data?.searchCounts)
                                recyclerFeatAdapter = RecyclerFeaturedProductAdapter("horizontal")
                                recyclerFeatAdapter?.addItems(arrFeatureViewAllImg)
                                recyclerFeatAdapter?.onClickListner1(this)
                                searchBinding.searchProductRecycler.adapter = recyclerFeatAdapter
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                searchBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                                searchBinding.idRefreshLayout.setVisibility(true)
                                searchBinding.chipGroup.setVisibility(true)
                                searchBinding.topSearchHeading.setVisibility(true)
                                searchBinding.newListingHeading.setVisibility(true)
                                searchBinding.searchDesc.setVisibility(true)
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        searchBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                        searchBinding.idRefreshLayout.setVisibility(false)
                        searchBinding.chipGroup.setVisibility(false)
                        searchBinding.topSearchHeading.setVisibility(false)
                        searchBinding.newListingHeading.setVisibility(false)
                        searchBinding.searchDesc.setVisibility(false)
                        searchBinding.apply {
                            placeHolder.setVisibility(false)
                            headingPlaceHolder.setVisibility(false)
                            placeHolderDescription.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        searchBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                        searchBinding.idRefreshLayout.setVisibility(false)
                        searchBinding.chipGroup.setVisibility(false)
                        searchBinding.topSearchHeading.setVisibility(false)
                        searchBinding.newListingHeading.setVisibility(false)
                        searchBinding.searchDesc.setVisibility(false)
                        searchBinding.apply {
                            placeHolder.setVisibility(false)
                            headingPlaceHolder.setVisibility(false)
                            placeHolderDescription.setVisibility(false)
                        }
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                searchBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                searchBinding.idRefreshLayout.setVisibility(false)
                searchBinding.chipGroup.setVisibility(false)
                searchBinding.topSearchHeading.setVisibility(false)
                searchBinding.newListingHeading.setVisibility(false)
                searchBinding.searchDesc.setVisibility(false)
                searchBinding.apply {
                    placeHolder.setVisibility(false)
                    headingPlaceHolder.setVisibility(false)
                    placeHolderDescription.setVisibility(false)
                }
                Log.d("error", e.message.toString())
            }
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(mContext, BidderProductDetails::class.java)
        toProductDetails.putExtra(
            Constants.PRODUCT_ID, arrFeatureViewAllImg.getOrNull(position)?.id
        )
        toProductDetails.putExtra(
            Constants.SELLER_ID, arrFeatureViewAllImg.getOrNull(position)?.sellerId
        )
        getContent.launch(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(mContext, PlaceBid::class.java)
        toPlaceBid.putExtra(Constants.PRODUCT_ID, arrFeatureViewAllImg.getOrNull(position)?.id)
        toPlaceBid.putExtra(
            Constants.SUB_CATEGORY_NAME, arrFeatureViewAllImg.getOrNull(position)?.categoryName
        )
        getContent.launch(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId = arrFeatureViewAllImg.getOrNull(position)?.id
        addWishListViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        arrFeatureViewAllImg.getOrNull(position)?.wishlist =
            arrFeatureViewAllImg.getOrNull(position)?.wishlist != true
        recyclerFeatAdapter!!.notifyItemChanged(position)
    }

    private fun handleTopSearches(searchCounts: List<GetSearchProductListResponseModel.Data.SearchCount?>?) {
        searchBinding.chipGroup.apply {
            removeAllViews()
            searchCounts?.forEachIndexed { _, searchCount ->
                addView(createRecentSearchChip(searchCount))
            }
        }
    }

    private fun createRecentSearchChip(label: GetSearchProductListResponseModel.Data.SearchCount?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val chip = DataBindingUtil.inflate<ItemLayoutBinding>(
            layoutInflater, R.layout.item_layout, null, false
        )
        chip.itemName.text = label?._id
        chip.itemName.setOnClickListener {
            searchBinding.searchTab.setQuery(label?._id, true)
        }
        return chip.root
    }


    private var isBindingInitialized = true
    fun callBackFromHome(status: Int) {
        this.status = status
        if (this::searchBinding.isInitialized) {
            when (status) {
                1 -> {
                    searchBinding.searchTab.setQuery("vegetables", true)
                    initSearch("vegetables")
                }

                2 -> {
                    searchBinding.searchTab.setQuery("fruits", true)
                    initSearch("fruits")
                }

                3 -> {
                    searchBinding.searchTab.setQuery("dates", true)
                    initSearch("dates")
                }
            }
        } else {
            isBindingInitialized = false
        }
    }
}