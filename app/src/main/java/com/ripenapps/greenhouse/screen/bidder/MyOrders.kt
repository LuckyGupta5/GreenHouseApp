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
import com.ripenapps.greenhouse.adapter.bidder.RecyclerMyOrderAdapter
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.view_models.BidderGetMyOrdersViewModel
import com.ripenapps.greenhouse.databinding.ActivityMyOrdersBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.MyOrderModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.MyOrderFilter
import com.ripenapps.greenhouse.model.bidder.response.BidderGetMyOrdersResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.hoursLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility

class MyOrders : BaseActivity() {
    private var bidderMyOrderViewModel: BidderGetMyOrdersViewModel? = null
    private lateinit var binding: ActivityMyOrdersBinding
    private var recyclerAdapter: RecyclerMyOrderAdapter? = null
    var accountBlocked: AccountBlocked? = null
    private var filterModel: FilterModel? = null
    private var myOrderFilter: MyOrderFilter? = null
    private var isPaging: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@MyOrders, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_orders)

        if (Preferences.getStringPreference(this@MyOrders, "language").equals("ar")) {
            binding.backIcon.rotation = 180f
        }

        initPagination()
        initListeners()
        initViewModel()
        pullToRefresh()
        getMyOrderApi()

        val searchEditText =
            binding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun filter() {
        bidderMyOrderViewModel?.request?.category = filterModel?.category
        bidderMyOrderViewModel?.request?.status = filterModel?.status
        bidderMyOrderViewModel?.request?.page = "1"
        bidderMyOrderViewModel?.page = 1
        recyclerAdapter?.clearData()
        getMyOrderApi()
    }

    private fun getMyOrderApi() {
        bidderMyOrderViewModel?.request?.page = "1"
        bidderMyOrderViewModel?.page = 1
        recyclerAdapter?.clearData()
        bidderMyOrderViewModel?.request?.token =
            Preferences.getStringPreference(this@MyOrders, TOKEN)
        bidderMyOrderViewModel?.getBidderGetMyOrdersRequest()
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            bidderMyOrderViewModel?.page = 1
            bidderMyOrderViewModel?.request?.page = bidderMyOrderViewModel?.page.toString()
            bidderMyOrderViewModel?.limit = bidderMyOrderViewModel?.limit!!
            bidderMyOrderViewModel?.request?.token =
                Preferences.getStringPreference(this@MyOrders, TOKEN)
            recyclerAdapter?.clearData()
            getMyOrderApi()

        }
    }

    private fun initViewModel() {
        bidderMyOrderViewModel =
            ViewModelProvider(this@MyOrders)[BidderGetMyOrdersViewModel::class.java]
        binding.lifecycleOwner = this@MyOrders
        bidderMyOrderViewModel?.getBidderGetMyOrdersData()?.observe(this@MyOrders) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderGetMyOrdersResponseModel::class.java
                        )
                        Log.d("TAG", "initViewModel: $data")
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                bidderMyOrderViewModel?.isLastPage = (data.data?.orders?.size
                                    ?: 0) < (bidderMyOrderViewModel?.limit?.toInt()!!)
                                bidderMyOrderViewModel?.isDataLoading = false
                                if (data.data?.orders?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        myOrdersRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        myOrdersRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrMyOrder: ArrayList<MyOrderModel> = arrayListOf()
                                data.data?.orders?.forEachIndexed { _, orders ->
                                    arrMyOrder.add(
                                        MyOrderModel(
                                            _id = orders.id,
                                            orderStatusImage = orders.orderStatus,
                                            orderStatus = orders.orderStatus,
                                            orderDayTime = CommonUtils.fullDateAndTimeFormat(orders.createdAt.toString()),
                                            productImage = orders.productDetails?.imageUrl,
                                            orderID = getString(R.string.order_id) + orders.orderId,
                                            productName = orders.productDetails?.subCategory?.enName,
                                            productWeight = "${orders.productDetails?.quantity} ${orders.productDetails?.unit}",
                                            productPrice = orders.productDetails?.productPrice.toString(),
                                            shippingMethod = if (orders.shippingMethod == "myself") getString(
                                                R.string.i_will_pick_by_myself
                                            )
                                            else orders.shippingMethod,
                                            shippingTimeLeft = hoursLeft(orders.selfPickUpTimer.toString())
                                        )
                                    )
                                }
                                if (recyclerAdapter == null || recyclerAdapter?.arrMyOrders?.isEmpty() == true) {
                                    recyclerAdapter = RecyclerMyOrderAdapter(
                                        ::performClick
                                    )
                                    binding.myOrdersRecycler.adapter = recyclerAdapter
                                    recyclerAdapter?.addItems(arrMyOrder)
                                } else {
                                    recyclerAdapter?.updateItemList(arrMyOrder)
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
                                Preferences.removePreference(this@MyOrders, "token")
                                Preferences.removePreference(this@MyOrders, "user_details")
                                Preferences.removePreference(this@MyOrders, "isVerified")
                                Preferences.removePreference(this@MyOrders, "roomId")
                                val signin = Intent(this@MyOrders, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@MyOrders, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(true)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@MyOrders, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(true)
                binding.idRefreshLayout.setVisibility(false)
                binding.placeHolder.setVisibility(false)
                binding.placeHolderDescription.setVisibility(false)
                binding.headingPlaceHolder.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun performClick(position: Int) {
        val orderDetail = Intent(this@MyOrders, BidderOrderDetailScreen::class.java)
        orderDetail.putExtra(
            ORDER_ID,
            recyclerAdapter?.arrMyOrders?.getOrNull(position)?._id
        ) //666153f9747bf56295c52e3e  666153f9747bf56295c52e30

        startActivity(orderDetail)
    }

    private fun initListeners() {
        binding.apply {

            cancelButton.setOnClickListener {
                binding.searchLayout.setQuery("", false)
                bidderMyOrderViewModel?.request?.search = null
                bidderMyOrderViewModel?.request?.page = "1"
                bidderMyOrderViewModel?.page = 1
                bidderMyOrderViewModel?.request?.search = null
                recyclerAdapter?.clearData()
                getMyOrderApi()
            }
            searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    // task HERE
                    bidderMyOrderViewModel?.request?.search = query.trim()
                    bidderMyOrderViewModel?.request?.page = "1"
                    bidderMyOrderViewModel?.page = 1
                    recyclerAdapter?.clearData()
                    getMyOrderApi()
                    return false
                }
            })
            backIcon.setOnClickListener {
                finish()
            }
            myOrdersFilter.setOnClickListener {
                if (myOrderFilter?.isAdded != true) {
                    myOrderFilter = MyOrderFilter(filterModel, ::handelFilter) //high order function
                    myOrderFilter?.show(supportFragmentManager, myOrderFilter?.tag)
                }
            }
        }
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "initListeners: ${Gson().toJson(filterModel)}")
        this@MyOrders.filterModel = model
        filter()
    }

    private fun initPagination() {
        binding.myOrdersRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!bidderMyOrderViewModel?.isLastPage!! && !bidderMyOrderViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    bidderMyOrderViewModel?.page = bidderMyOrderViewModel?.page!! + 1
                    bidderMyOrderViewModel?.limit = bidderMyOrderViewModel?.limit!!
                    bidderMyOrderViewModel?.isDataLoading = true
//                        binding.loader.setVisibility(bidderMyOrderViewModel?.limit != 0f)
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                    bidderMyOrderViewModel?.request?.token =
                        Preferences.getStringPreference(this@MyOrders, TOKEN)
                    bidderMyOrderViewModel?.request?.apply {
                        this.page = bidderMyOrderViewModel?.page?.toString()
                        this.limit = bidderMyOrderViewModel?.limit?.toString()
                    }
                    bidderMyOrderViewModel?.getBidderGetMyOrdersRequest()
                }
            }
        })
    }
}