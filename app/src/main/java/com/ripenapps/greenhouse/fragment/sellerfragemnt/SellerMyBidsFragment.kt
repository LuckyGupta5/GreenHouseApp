package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.RecyclerMyBidsAdapter
import com.ripenapps.greenhouse.view_models.SellerGetMyBidsViewModel
import com.ripenapps.greenhouse.databinding.FragmentSellerMyBidsBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.MyBidsSellerModel
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerProductListFilterModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerMyBidsResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.BiddingDetails
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility

class SellerMyBidsFragment : Fragment(), RecyclerMyBidsAdapter.onClickListner {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentSellerMyBidsBinding
    private var recyclerAdapter: RecyclerMyBidsAdapter? = null
    var accountBlocked: AccountBlocked? = null
    private var sellerGetMyBidsViewModel: SellerGetMyBidsViewModel? = null
    private var sellerProductListFilterModel: SellerProductListFilterModel? = null
    private var sellerMyBidsFilter: SellerMyBidsFilter? = null
    private var isPaging: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onStart() {
        super.onStart()
        sellerGetMyBidsViewModel?.request?.page = "1"
        sellerGetMyBidsViewModel?.page = 1
        recyclerAdapter?.clearData()
        sellerGetMyBidsViewModel?.request?.token =
            Preferences.getStringPreference(mContext, Constants.TOKEN)
        sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        CommonUtils.changeStatusBarColor(this@SellerMyBidsFragment, R.color.status_bar)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_seller_my_bids, container, false)

        val searchEditText =
            binding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START

        initPagination()
        initListener()
        initViewModel()
        pullToRefresh()
        searching()
        return binding.root
    }

    private fun filter() {
        if (sellerProductListFilterModel != null) {
            sellerGetMyBidsViewModel?.request?.categoryName = sellerProductListFilterModel?.category
            sellerGetMyBidsViewModel?.request?.remainingHours = sellerProductListFilterModel?.hours
        } else {
            sellerGetMyBidsViewModel?.request?.categoryName = null
            sellerGetMyBidsViewModel?.request?.remainingHours = null
        }
        sellerGetMyBidsViewModel?.request?.page = "1"
        sellerGetMyBidsViewModel?.page = 1
        recyclerAdapter?.clearData()
        myBidListingApi(sellerGetMyBidsViewModel?.request?.categoryName)
    }

    private fun searching() {
        binding.searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                activity?.let { CommonUtils.hideKeyboard(binding.searchLayout, it) }
                sellerGetMyBidsViewModel?.request?.search = query.trim()
                sellerGetMyBidsViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, Constants.TOKEN)
                sellerGetMyBidsViewModel?.request?.page = "1"
                sellerGetMyBidsViewModel?.page = 1
                recyclerAdapter?.clearData()
                sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
                return false
            }
        })
    }

    var backPressed = false
    private fun setBack(){
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressed) {
                        activity?.finishAffinity()
                    }

                    backPressed = true
                    Handler(Looper.getMainLooper()).postDelayed({ backPressed = false }, 2000)
                }
            }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            sellerGetMyBidsViewModel?.request?.page = "1"
            sellerGetMyBidsViewModel?.request?.token =
                Preferences.getStringPreference(mContext, Constants.TOKEN)
            sellerGetMyBidsViewModel?.page = 1
            sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
        }
    }

    private fun myBidListingApi(productId: String?) {
        sellerGetMyBidsViewModel?.request?.page = "1"
        sellerGetMyBidsViewModel?.page = 1
        sellerGetMyBidsViewModel?.request?.categoryName = productId
        sellerGetMyBidsViewModel?.request?.token =
            Preferences.getStringPreference(mContext, Constants.TOKEN)
        sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
    }

    private fun initViewModel() {
        sellerGetMyBidsViewModel =
            ViewModelProvider(this@SellerMyBidsFragment)[SellerGetMyBidsViewModel::class.java]
        binding.lifecycleOwner = this@SellerMyBidsFragment
        sellerGetMyBidsViewModel?.getSellerMyBidsData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(AESHelper.decrypt(Companion.SECRET_KEY, it.data), SellerMyBidsResponseModel::class.java)
                        binding.idProgress.setVisibility(false)
                        Log.d("TAG", "sellerMyBidsResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                sellerGetMyBidsViewModel?.isLastPage = (data.data?.mybids?.size
                                    ?: 0) < (sellerGetMyBidsViewModel?.limit?.toInt()!!)
                                sellerGetMyBidsViewModel?.isDataLoading = false
                                ProcessDialog.dismissDialog()
                                if (data.data?.mybids?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        myBidsSellerRecycler2.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        myBidsSellerRecycler2.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrMyBids: ArrayList<MyBidsSellerModel> = arrayListOf()
                                data.data?.mybids?.forEachIndexed { _, myBids ->
                                    arrMyBids.add(
                                        MyBidsSellerModel(
                                            id = myBids.productDetails?.productId,
                                            vegimg = myBids.productDetails?.imageUrl ?: "",
                                            name = myBids.productDetails?.subCategory?.enName,
                                            location = myBids.productDetails?.productLocation,
                                            weight = myBids.productDetails?.quantity.toString() + " " + myBids.productDetails?.unit,
                                            aedPrice = (myBids.productDetails?.productPrice
                                                ?: 0).toString(),
                                            highestPriceBid = getString(R.string.sar) + " " + myBids.highestBidAmount.toString(),
                                            time = CommonUtils.timeLeftWithoutSecond(
                                                myBids.productDetails?.endDate.toString(), mContext
                                            )
                                        )
                                    )
                                }

                                if (recyclerAdapter == null || recyclerAdapter?.arrMyBids?.isEmpty() == true || sellerGetMyBidsViewModel?.page == 1) {
                                    recyclerAdapter = RecyclerMyBidsAdapter(
                                        ::itemClickAction
                                    )
                                    recyclerAdapter?.onClickListner(this)
                                    binding.myBidsSellerRecycler2.adapter = recyclerAdapter
                                    recyclerAdapter?.addItems(arrMyBids)
                                } else {
                                    recyclerAdapter?.updateItemList(arrMyBids)
                                }
                            }


                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(parentFragmentManager, accountBlocked?.tag)
                                }
                            }

                            403,401 -> {
                                val signIn = Intent(mContext, SignIn::class.java)
                                startActivity(signIn)
                                activity?.finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
//                                productFragmentBinding.shimmer.idMainShimmer.setVisibility(true)
                                binding.bidInProgressShimmer.bidInProgressMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
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


    private fun itemClickAction(position: Int) {/*     Toast.makeText(mContext, "Hello $position", Toast.LENGTH_SHORT).show()*/
        val intent = Intent(requireContext(), BiddingDetails::class.java)
        intent.putExtra(PRODUCT_ID, recyclerAdapter?.arrMyBids?.getOrNull(position)?.id)
        startActivity(intent)
    }

    private fun initListener() {
        binding.apply {
            filterMyBids.setOnClickListener {
                if (sellerMyBidsFilter?.isAdded != true) {
                    sellerMyBidsFilter = SellerMyBidsFilter(
                        sellerProductListFilterModel, ::handelFilter
                    ) //high order function
                    sellerMyBidsFilter?.show(parentFragmentManager, sellerMyBidsFilter?.tag)
                }

            }

            searchTab.setOnClickListener {
                heading.visibility = View.GONE
                searchTab.visibility = View.GONE
                filterMyBids.visibility = View.GONE
                search.visibility = View.VISIBLE
            }
            cancelButton.setOnClickListener {
                sellerGetMyBidsViewModel?.request?.search = null
                sellerGetMyBidsViewModel?.request?.page = "1"
                sellerGetMyBidsViewModel?.page = 1
                recyclerAdapter?.clearData()
                sellerGetMyBidsViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, Constants.TOKEN)
                sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
                heading.visibility = View.VISIBLE
                searchTab.visibility = View.VISIBLE
                filterMyBids.visibility = View.VISIBLE
                search.visibility = View.GONE
            }
        }
    }

    override fun onClick(position: Int) {
        val toBiddingDetails = Intent(requireContext(), BiddingDetails::class.java)
        startActivity(toBiddingDetails)
    }

    private fun handelFilter(model: SellerProductListFilterModel? = null) {
        this@SellerMyBidsFragment.sellerProductListFilterModel = model
        Log.d("TAG", "myBidsFilter: ${Gson().toJson(sellerProductListFilterModel)}")
        filter()
    }

    private fun initPagination() {
        binding.myBidsSellerRecycler2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!sellerGetMyBidsViewModel?.isLastPage!! && !sellerGetMyBidsViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    sellerGetMyBidsViewModel?.page = sellerGetMyBidsViewModel?.page!! + 1
                    sellerGetMyBidsViewModel?.limit = sellerGetMyBidsViewModel?.limit!!
                    sellerGetMyBidsViewModel?.isDataLoading = true
                    sellerGetMyBidsViewModel?.request?.token =
                        Preferences.getStringPreference(mContext, Constants.TOKEN)
                    sellerGetMyBidsViewModel?.request?.apply {
                        this.page = sellerGetMyBidsViewModel?.page?.toString()
                        this.limit = sellerGetMyBidsViewModel?.limit?.toString()
                    }
                    sellerGetMyBidsViewModel?.getSellerMyBidsRequest()
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}


