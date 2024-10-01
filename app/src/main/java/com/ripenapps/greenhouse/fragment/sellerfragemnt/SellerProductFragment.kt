package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.SellerProductFragmentAdapter
import com.ripenapps.greenhouse.databinding.FragmentSellerProductBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerProductListFilterModel
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerproductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.BidderProductListLatestResponseModel
import com.ripenapps.greenhouse.model.seller.response.CategoryResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.SellerAddProduct
import com.ripenapps.greenhouse.screen.seller.SellerProductDetail
import com.ripenapps.greenhouse.screen.seller.SoldProduct
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.CategoryViewModel
import com.ripenapps.greenhouse.view_models.ProductListingViewModel

class SellerProductFragment : Fragment() {
    private lateinit var sellerProductsFragmentBinding: FragmentSellerProductBinding
    private lateinit var mContext: Context
    private lateinit var categoryViewModel: CategoryViewModel
    private var productListViewModel: ProductListingViewModel? = null
    private var productListingData: String? = null
    private var accountBlocked: AccountBlocked? = null
    private var categoryData: CategoryResponseModel = CategoryResponseModel()
    private var recycleSellerProduct: SellerProductFragmentAdapter? = null
    private var sellerProductListFilterModel: SellerProductListFilterModel? = null
    private var sellerProductFilter: SellerProductFilter? = null
    private var isPaging: Boolean = false
    private var adminCommission: Double? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    // Context initialization
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    // Fragment view creation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        changeStatusBarColor(this@SellerProductFragment, R.color.status_bar)
        sellerProductsFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_product, container, false
        )

        // Check and set arrow rotation based on language preference
        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            sellerProductsFragmentBinding.arrow.rotation = 180f
        }

        // Initialize pagination, listeners, and view models
        initPagination()
        initListener()
        subCategoryViewModel()
        productListViewModel()
        initStartActivityInstance()

        // Set up search functionality
        sellerProductsFragmentBinding.searchLayout.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                sellerProductsFragmentBinding.searchLayout.clearFocus()
                productListViewModel?.request?.search = query.trim()
                productListViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                productListViewModel?.request?.page = "1"
                productListViewModel?.page = 1
                recycleSellerProduct?.clearData()
                productListViewModel?.getRequestProductList()
                return false
            }
        })

        // Initial API calls
        productListingApi()

        return sellerProductsFragmentBinding.root
    }

    // Initialize the activity result launcher for handling startActivityForResult
    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        productListViewModel?.request?.page = "1"
                        productListViewModel?.page = 1
                        recycleSellerProduct?.clearData()
                        productListingApi()
                    }
                }
            }
    }

    // Function to apply filter based on selected options
    private fun filter() {
        productListViewModel?.request?.category = sellerProductListFilterModel?.category
        productListViewModel?.request?.remainingHours = sellerProductListFilterModel?.hours
        productListViewModel?.request?.address = sellerProductListFilterModel?.address
        productListViewModel?.request?.lat = sellerProductListFilterModel?.latitude
        productListViewModel?.request?.long = sellerProductListFilterModel?.longitude
        productListViewModel?.request?.page = "1"
        productListViewModel?.page = 1
        recycleSellerProduct?.clearData()
        productListingApi()
    }

    // Function to initiate the initial product listing API call
    private fun productListingApi() {
        productListViewModel?.request?.page = "1"
        productListViewModel?.page = 1
        productListViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
        productListViewModel?.getRequestProductList()
    }

    // Function to observe changes in product listing data through ViewModel
    private fun productListViewModel() {
        productListViewModel = ViewModelProvider(this)[ProductListingViewModel::class.java]
        sellerProductsFragmentBinding.lifecycleOwner = this
        productListViewModel?.getProductListData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        sellerProductsFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                        sellerProductsFragmentBinding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderProductListLatestResponseModel::class.java
                        )
                        sellerProductsFragmentBinding.idProgress.setVisibility(false)
                        Log.d("TAG", "sellerProductListResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                sellerProductsFragmentBinding.idRefreshLayout.isRefreshing = false
                                productListViewModel?.isLastPage = (data.data?.products?.size ?: 0) < (productListViewModel?.limit!!)
                                productListViewModel?.isDataLoading = false
                                productListingData = Gson().toJson(data.data)
                                adminCommission = data.data?.adminCommission
                                if (data.data?.products?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    sellerProductsFragmentBinding.apply {
                                        sellerProduct.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    sellerProductsFragmentBinding.apply {
                                        sellerProduct.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrSellerProduct: ArrayList<SellerproductModel> = arrayListOf()
                                Log.d("TAG", "initViewModel2: ${Gson().toJson(data.data)}")
                                data.data?.products?.forEachIndexed { _, product ->
                                    arrSellerProduct.add(
                                        SellerproductModel(
                                            isFeatured = product.isFeatured,
                                            id = product.id ?: "",
                                            itemImg = product.imageUrl ?: "",
                                            name = product.subCategoryId?.enName,
                                            location = product.productLocation,
                                            countBids = (product.bidCount).toString() + " " + getString(
                                                R.string.bids
                                            ),// does not provide key
                                            weight = product.quantity.toString() + " " + product.unit,
                                            time = timeLeft(product.endDate.toString(), mContext),
                                            aedPrice = mContext.getString(R.string.sar) + " " + product.price
                                        )
                                    )
                                }
                                if (recycleSellerProduct == null || recycleSellerProduct?.arrSellerProduct?.isEmpty() == true || productListViewModel?.page == 1) {
                                    recycleSellerProduct = SellerProductFragmentAdapter(mContext, ::itemClickAction)
                                    sellerProductsFragmentBinding.sellerProduct.adapter = recycleSellerProduct
                                    recycleSellerProduct?.addItems(arrSellerProduct)
                                } else {
                                    recycleSellerProduct?.updateItemList(arrSellerProduct)
                                }
                            }

                            402 -> {
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                                val toLogin = Intent(mContext, SignIn::class.java)
                                startActivity(toLogin)
                                activity?.finish()
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
                                sellerProductsFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(false)
                                sellerProductsFragmentBinding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                        if (!isPaging) {
                            sellerProductsFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                            sellerProductsFragmentBinding.idRefreshLayout.setVisibility(false)
                            sellerProductsFragmentBinding.placeHolder.setVisibility(false)
                            sellerProductsFragmentBinding.placeHolderDescription.setVisibility(false)
                            sellerProductsFragmentBinding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                        sellerProductsFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                        sellerProductsFragmentBinding.idRefreshLayout.setVisibility(false)
                        sellerProductsFragmentBinding.placeHolder.setVisibility(false)
                        sellerProductsFragmentBinding.placeHolderDescription.setVisibility(false)
                        sellerProductsFragmentBinding.headingPlaceHolder.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                sellerProductsFragmentBinding.myProductShimmer.idMainShimmer.setVisibility(true)
                sellerProductsFragmentBinding.idRefreshLayout.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    // Function to handle subcategory data from API
    private fun subCategoryViewModel() {
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        sellerProductsFragmentBinding.lifecycleOwner = this
        categoryViewModel.getCategoryData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            CategoryResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                categoryData = data
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
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    // Function to handle item click actions in RecyclerView
    private fun itemClickAction(position: Int) {
        val intent = Intent(mContext, SellerProductDetail::class.java)
        intent.putExtra(PRODUCT_ID, recycleSellerProduct?.arrSellerProduct?.getOrNull(position)?.id)
        getContent.launch(intent)
    }

    // Function to initialize listeners for UI elements
    private fun initListener() {
        // Handle swipe-to-refresh action
        sellerProductsFragmentBinding.idRefreshLayout.setOnRefreshListener {
            productListViewModel?.page = 1
            productListViewModel?.request?.page = productListViewModel?.page.toString()
            productListViewModel?.limit = productListViewModel?.limit!!
            productListViewModel?.request?.token =
                Preferences.getStringPreference(requireContext(), TOKEN)
            productListingApi()
        }

        // Handle click on add product button
        sellerProductsFragmentBinding.idProductAdd.setOnClickListener {
            val toAddProduct = Intent(mContext, SellerAddProduct::class.java)
            toAddProduct.putExtra("adminCommission", adminCommission.toString())
            getContent.launch(toAddProduct)
        }

        // Handle click on filter icon
        sellerProductsFragmentBinding.icProductsFilter.setOnClickListener {
            if (sellerProductFilter?.isAdded != true) {
                sellerProductFilter = SellerProductFilter.newInstance(sellerProductListFilterModel, ::handelFilter)
                sellerProductFilter?.show(parentFragmentManager, sellerProductFilter?.tag)
            }
        }

        // Handle click on sold product layout
        sellerProductsFragmentBinding.soldProductLayout.setOnClickListener {
            val intent = Intent(mContext, SoldProduct::class.java)
            startActivity(intent)
        }

        // Handle click on search icon
        sellerProductsFragmentBinding.searchIcon.setOnClickListener {
            sellerProductsFragmentBinding.search.visibility = View.VISIBLE
            sellerProductsFragmentBinding.icProductsFilter.visibility = View.GONE
            sellerProductsFragmentBinding.myProduct.visibility = View.GONE
            sellerProductsFragmentBinding.searchIcon.visibility = View.GONE
        }

        // Handle click on cancel search button
        sellerProductsFragmentBinding.cancelButton.setOnClickListener {
            sellerProductsFragmentBinding.search.visibility = View.GONE
            sellerProductsFragmentBinding.icProductsFilter.visibility = View.VISIBLE
            sellerProductsFragmentBinding.myProduct.visibility = View.VISIBLE
            sellerProductsFragmentBinding.soldProductLayout.visibility = View.VISIBLE
            sellerProductsFragmentBinding.searchIcon.visibility = View.VISIBLE
            productListViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
            productListViewModel?.request?.search = null
            productListViewModel?.request?.page = "1"
            productListViewModel?.page = 1
            recycleSellerProduct?.clearData()
            productListViewModel?.getRequestProductList()
        }
    }

    // Function to handle filter changes
    private fun handelFilter(model: SellerProductListFilterModel? = null) {
        this@SellerProductFragment.sellerProductListFilterModel = model
        filter()
    }

    // Function to initialize pagination for RecyclerView
    private fun initPagination() {
        sellerProductsFragmentBinding.sellerProduct.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!productListViewModel?.isLastPage!! && !productListViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    productListViewModel?.page = productListViewModel?.page?.plus(1)!!
                    productListViewModel?.limit = productListViewModel!!.limit
                    productListViewModel?.request?.token =
                        Preferences.getStringPreference(requireContext(), TOKEN)
                    productListViewModel?.isDataLoading = true

                    productListViewModel?.request?.token =
                        Preferences.getStringPreference(requireContext(), TOKEN)
                    productListViewModel?.request.apply {
                        this?.page = productListViewModel?.page.toString()
                        this?.limit = productListViewModel?.limit.toString()
                    }
                    productListViewModel?.getRequestProductList()
                    sellerProductsFragmentBinding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}
