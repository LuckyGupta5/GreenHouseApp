package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerMyBidsAdapter
import com.ripenapps.greenhouse.view_models.SellerSoldProductViewModel
import com.ripenapps.greenhouse.databinding.ActivitySoldProductBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.MyBidsSellerModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerSoldProductResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility

class SoldProduct : BaseActivity(), RecyclerMyBidsAdapter.onClickListner {
    private lateinit var binding: ActivitySoldProductBinding
    private var recyclerAdapter: RecyclerMyBidsAdapter? = null
    private var soldProductViewModel: SellerSoldProductViewModel? = null
    private var isPaging: Boolean = false
    var accountBlocked: AccountBlocked? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@SoldProduct, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sold_product)
        if (Preferences.getStringPreference(this@SoldProduct, "language") == "ar") {
            binding.btnBackAddresses.rotation = 180f
        }
        initPagination()
        pullToRefresh()
        initViewModel()
        binding.btnBackAddresses.setOnClickListener {
            finish()
        }
        soldProductApi()
    }

    private fun soldProductApi() {
        soldProductViewModel?.request?.page = "1"
        soldProductViewModel?.page = 1
        soldProductViewModel?.request?.token = Preferences.getStringPreference(this, "token")
        soldProductViewModel?.soldProductRequest()
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            soldProductViewModel?.request?.page = "1"
            soldProductViewModel?.page = 1
            soldProductViewModel?.limit = soldProductViewModel?.limit!!
            soldProductViewModel?.request?.token = Preferences.getStringPreference(this, "token")
            binding.idRefreshLayout.isRefreshing = true
            soldProductViewModel?.soldProductRequest()
        }
    }

    private fun initViewModel() {
        soldProductViewModel =
            ViewModelProvider(this@SoldProduct)[SellerSoldProductViewModel::class.java]
        binding.lifecycleOwner = this@SoldProduct
        soldProductViewModel?.soldProductData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerSoldProductResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "soldProductResponse: ${Gson().toJson(data.data)}")
                                soldProductViewModel?.isLastPage = (data.data?.orders?.size
                                    ?: 0) < (soldProductViewModel?.limit?.toInt() ?: 0)
                                binding.idRefreshLayout.isRefreshing = false
                                soldProductViewModel?.isDataLoading = false

                                if (data.data?.orders?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        soldProductRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        soldProductRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrMyBids: ArrayList<MyBidsSellerModel> = arrayListOf()
                                data.data?.orders?.forEachIndexed { _, myBids ->
                                    arrMyBids.add(
                                        MyBidsSellerModel(
                                            soldProductData = data.data,
                                            id = myBids.productId,
                                            vegimg = (myBids.productDetails?.imageUrl?.joinToString(", ") ?: ""),
                                            name = myBids.productDetails?.subCategory?.enName,
                                            location = myBids.productDetails?.productLocation,
                                            weight = myBids.productDetails?.quantity.toString() + " " + myBids.productDetails?.unit,
                                            aedPrice = (myBids.productDetails?.productPrice ?: 0).toString(),
                                            highestPriceBid = getString(R.string.sar) +" "+  myBids.highestBidPrice
                                        )
                                    )
                                }
                                if (recyclerAdapter == null || recyclerAdapter?.arrMyBids?.isEmpty() == true || soldProductViewModel?.page == 1) {
                                    recyclerAdapter = RecyclerMyBidsAdapter(::onClick)
                                    recyclerAdapter?.onClickListner(this)
                                    binding.soldProductRecycler.adapter = recyclerAdapter
                                    recyclerAdapter?.addItems(arrMyBids)
                                } else {
                                    recyclerAdapter?.updateItemList(arrMyBids)
                                }
                                ProcessDialog.dismissDialog()
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@SoldProduct, "token")
                                Preferences.removePreference(this@SoldProduct, "user_details")
                                Preferences.removePreference(this@SoldProduct, "isVerified")
                                Preferences.removePreference(this@SoldProduct, "roomId")
                                val signIn = Intent(this@SoldProduct, SignIn::class.java)
                                startActivity(signIn)
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
                                binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                                ProcessDialog.dismissDialog()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(true)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())

            }
        }
    }

    override fun onClick(position: Int) {
        val intent = Intent(this@SoldProduct, SellerSoldProductDetail::class.java)
        intent.putExtra(
            "soldProductData", Gson().toJson(
                recyclerAdapter?.arrMyBids!!.getOrNull(0)?.soldProductData?.orders?.getOrNull(
                    position
                )
            )
        )
        startActivity(intent)
    }

    private fun initPagination() {
        binding.soldProductRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!soldProductViewModel?.isLastPage!! && !soldProductViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    soldProductViewModel?.page = soldProductViewModel?.page?.plus(1)!!
                    soldProductViewModel?.limit = soldProductViewModel!!.limit
                    Log.d("TAG", "onScrolled: " + soldProductViewModel?.page)
                    soldProductViewModel?.request?.token =
                        Preferences.getStringPreference(this@SoldProduct, "token")
                    soldProductViewModel?.isDataLoading = true

                    soldProductViewModel?.request.apply {
                        this?.page = soldProductViewModel?.page.toString()
                        this?.limit = soldProductViewModel?.limit.toString()
                    }
                    soldProductViewModel?.soldProductRequest()
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}
