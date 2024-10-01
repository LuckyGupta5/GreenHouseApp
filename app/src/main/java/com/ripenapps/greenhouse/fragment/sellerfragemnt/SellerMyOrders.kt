package com.ripenapps.greenhouse.fragment.sellerfragemnt

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.bidder.RecyclerMyOrderAdapter
import com.ripenapps.greenhouse.databinding.FragmentSellerMyOrders2Binding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.MyOrderModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerGetMyOrdesResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.SellerOrderDetailsSelfPickup
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.hoursLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.SellerGetMyOrdersViewModel

class SellerMyOrders : Fragment() {
    private lateinit var binding: FragmentSellerMyOrders2Binding
    private var sellerGetMyOrdersViewModel: SellerGetMyOrdersViewModel? = null
    private var recyclerAdapter: RecyclerMyOrderAdapter? = null
    private var accountBlocked: AccountBlocked? = null
    private lateinit var mContext: Context
    private var filterModel: FilterModel? = null
    private var sellerMyOrderFilter: SellerMyOrderFilter? = null
    private var isPaging: Boolean = false
    private lateinit var getContent: ActivityResultLauncher<Intent>
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        CommonUtils.changeStatusBarColor(this@SellerMyOrders, R.color.status_bar)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_seller_my_orders2, container, false)
        val searchEditText =
            binding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START

        initPagination()
        initListeners()
        initViewModel()
        pullToRefresh()
        getMyOrderApi()
        initStartActivityInstance()
        searching()
        return binding.root
    }

    private fun searching() {
        binding.searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                activity?.let { CommonUtils.hideKeyboard(binding.searchLayout, it) }
                sellerGetMyOrdersViewModel?.request?.search = query.trim()
                sellerGetMyOrdersViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                sellerGetMyOrdersViewModel?.request?.page = "1"
                sellerGetMyOrdersViewModel?.page = 1
                recyclerAdapter?.clearData()
                sellerGetMyOrdersViewModel?.getSellerMyOrdersRequest()
                return false
            }
        })
    }

    private fun filter() {
        sellerGetMyOrdersViewModel?.request?.category = filterModel?.category
        sellerGetMyOrdersViewModel?.request?.status = filterModel?.status
        sellerGetMyOrdersViewModel?.request?.page = "1"
        sellerGetMyOrdersViewModel?.page = 1
        recyclerAdapter?.clearData()
        getMyOrderApi()
    }

    private fun getMyOrderApi() {
        sellerGetMyOrdersViewModel?.request?.page = "1"
        sellerGetMyOrdersViewModel?.page = 1
        sellerGetMyOrdersViewModel?.request?.token =
            Preferences.getStringPreference(mContext, TOKEN)
        sellerGetMyOrdersViewModel?.getSellerMyOrdersRequest()
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            sellerGetMyOrdersViewModel?.page = 1
            sellerGetMyOrdersViewModel?.request?.page = sellerGetMyOrdersViewModel?.page.toString()
            sellerGetMyOrdersViewModel?.limit = sellerGetMyOrdersViewModel?.limit!!
            sellerGetMyOrdersViewModel?.request?.token =
                Preferences.getStringPreference(mContext, TOKEN)
            getMyOrderApi()
        }
    }

    private fun initViewModel() {
        sellerGetMyOrdersViewModel =
            ViewModelProvider(this@SellerMyOrders)[SellerGetMyOrdersViewModel::class.java]
        binding.lifecycleOwner = this@SellerMyOrders
        sellerGetMyOrdersViewModel?.getSellerMyOrdersData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerGetMyOrdesResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        Log.d("TAG", "sellerMyOrderResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                sellerGetMyOrdersViewModel?.isLastPage = (data.data?.orders?.size
                                    ?: 0) < (sellerGetMyOrdersViewModel?.limit?.toInt()!!)
                                ProcessDialog.dismissDialog()
                                sellerGetMyOrdersViewModel?.isDataLoading = false
                                binding.idRefreshLayout.isRefreshing = false
                                ProcessDialog.dismissDialog()
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
                                            _id = orders._id,
                                            orderStatusImage = orders.sellerOrderStatus,
                                            orderStatus = orders.sellerOrderStatus,
                                            orderDayTime = CommonUtils.fullDateAndTimeFormat(orders.createdAt.toString()),
                                            productImage = orders.productDetails?.imageUrl,
                                            orderID = mContext.getString(R.string.order_id_hash) + orders.orderId,
                                            productName = orders.productDetails?.subCategory?.enName,
                                            productWeight = "${orders.productDetails?.quantity} ${orders.productDetails?.unit}",
                                            productPrice = orders.productDetails?.productPrice.toString(),
                                            shippingMethod = if (orders.shippingMethod == "myself") getString(
                                                R.string.i_will_pick_by_myself
                                            )
                                            else orders.shippingMethod,
                                            shippingTimeLeft = if (orders.sellerOrderStatus == "Received") hoursLeft(
                                                orders.packTimer.toString()
                                            ) else {
                                                hoursLeft(orders.selfPickUpTimer.toString())
                                            }
                                        )
                                    )
                                    Log.d(
                                        "TAG",
                                        "timeLeft " + data.data.orders.getOrNull(0)?.shippingMethod.toString()
                                    )
                                }
                                Log.d("TAG", "initViewModel: ${Gson().toJson(arrMyOrder)}")
                                if (recyclerAdapter == null || recyclerAdapter?.arrMyOrders?.isEmpty() == true || sellerGetMyOrdersViewModel?.page == 1) {
                                    recyclerAdapter = RecyclerMyOrderAdapter(::performClick)
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
                                ProcessDialog.dismissDialog()
                                Toast.makeText(
                                    mContext, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(true)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                            binding.idRefreshLayout.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.myOrdersShimmer.myOrderMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        ProcessDialog.dismissDialog()
                        showToast(it.message ?: getString(R.string.something_went_wrong))
                        binding.idRefreshLayout.isRefreshing = false

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

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val orderId = result.data?.extras?.getString("id")
                    val status = result.data?.extras?.getString("status")
                    val isUpdate = result.data?.extras?.getString("isUpdate") ?: ""
                    val isForPack = result.data?.extras?.getString("packed") ?: ""
                    val isDelivered = result.data?.extras?.getString("deliver") ?: ""
                    Log.d("outSide", "initStartActivityInstance: $orderId $status")
                    if (status?.isNotEmpty() == true && orderId?.isNotEmpty() == true) {
                        recyclerAdapter?.apply {
                            val position = this.arrMyOrders.indexOfFirst { it._id == orderId }
                            Log.d("position", "initStartActivityInstance: $position")
                            if (position != -1) {
                                if (isUpdate.isNotEmpty() && status.lowercase() == "cancelled".lowercase()) {
                                    arrMyOrders.removeAt(position)
                                    notifyItemRemoved(position)
                                    return@registerForActivityResult
                                }
                                if (arrMyOrders.getOrNull(position)?.orderStatus != status) {
                                    arrMyOrders.getOrNull(position)?.orderStatus = status
                                    arrMyOrders.getOrNull(position)?.shippingTimeLeft =
                                        if (status.lowercase() == "received") isForPack
                                        else isDelivered
                                    notifyItemChanged(position)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun performClick(position: Int) {
        val orderDetails = Intent(mContext, SellerOrderDetailsSelfPickup::class.java)
        orderDetails.putExtra(ORDER_ID, recyclerAdapter?.arrMyOrders?.getOrNull(position)?._id)
        getContent.launch(orderDetails)
    }
    private fun initListeners() {
        binding.apply {
            icProductsFilter.setOnClickListener {
                if (sellerMyOrderFilter?.isAdded != true) {
                    sellerMyOrderFilter =
                        SellerMyOrderFilter(filterModel, ::handelFilter) //high order function
                    sellerMyOrderFilter?.show(parentFragmentManager, sellerMyOrderFilter?.tag)
                }
            }

            binding.searchIcon.setOnClickListener {
                binding.search.visibility = View.VISIBLE
                binding.icProductsFilter.visibility = View.GONE
                binding.myProduct.visibility = View.GONE
                binding.searchIcon.visibility = View.GONE
            }
            binding.cancelButton.setOnClickListener {
                binding.search.visibility = View.GONE
                binding.icProductsFilter.visibility = View.VISIBLE
                binding.myProduct.visibility = View.VISIBLE
                binding.searchIcon.visibility = View.VISIBLE
                sellerGetMyOrdersViewModel?.request?.search = null
                sellerGetMyOrdersViewModel?.request?.page = "1"
                sellerGetMyOrdersViewModel?.page = 1
                recyclerAdapter?.clearData()
                getMyOrderApi()
            }

        }
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "initListeners: ${Gson().toJson(filterModel)}")
        this@SellerMyOrders.filterModel = model
        filter()
    }
    private fun initPagination() {
        binding.myOrdersRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!sellerGetMyOrdersViewModel?.isLastPage!! && !sellerGetMyOrdersViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    sellerGetMyOrdersViewModel?.page = sellerGetMyOrdersViewModel?.page!! + 1
                    sellerGetMyOrdersViewModel?.limit = sellerGetMyOrdersViewModel?.limit!!
                    sellerGetMyOrdersViewModel?.isDataLoading = true
                    sellerGetMyOrdersViewModel?.request?.token =
                        Preferences.getStringPreference(mContext, TOKEN)
                    sellerGetMyOrdersViewModel?.request?.apply {
                        this.page = sellerGetMyOrdersViewModel?.page?.toString()
                        this.limit = sellerGetMyOrdersViewModel?.limit?.toString()
                    }
                    sellerGetMyOrdersViewModel?.getSellerMyOrdersRequest()
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}
