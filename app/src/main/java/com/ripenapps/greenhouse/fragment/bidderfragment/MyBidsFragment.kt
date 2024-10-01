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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.bidder.RecyclerMyBidAdapter
import com.ripenapps.greenhouse.databinding.FragmentMyBidsBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.MyBidModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.BidderBidInProgressResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetMyBidsResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.BidInProgress
import com.ripenapps.greenhouse.screen.bidder.BidderBiddingDetails
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.BidderBidInProgressViewModel
import com.ripenapps.greenhouse.view_models.GetMyBidsViewModel

class MyBidsFragment : Fragment(), RecyclerMyBidAdapter.onClickListner {
    private lateinit var binding: FragmentMyBidsBinding
    private lateinit var mcontext: Context
    private var bidInProgressViewModel: BidderBidInProgressViewModel? = null
    private var recyclerAdapter: RecyclerMyBidAdapter? = null
    private var getMyBidsViewModel: GetMyBidsViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var filterModel: FilterModel? = null
    private var isPaging: Boolean = false
    private var myBidFilter: MyBidFilter? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        changeStatusBarColor(this@MyBidsFragment, R.color.status_bar)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_bids, container, false)

        if (Preferences.getStringPreference(mcontext, "language") == "ar") {
            binding.backbtn.rotation = 180f
        }

        initPagination()
        initListeners()
        initViewModel()
        bidInProgressViewModel()
        pullToRefresh()
        searching()
        bidInProgressApi()
        myBidsListingAPI()
        initStartActivityInstance()

        val searchEditText =
            binding.searchLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        return binding.root
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        bidInProgressApi()
                        myBidsListingAPI()
                    }
                }
            }
    }

    private fun bidInProgressApi() {
        bidInProgressViewModel?.request?.page = "1"
        bidInProgressViewModel?.page = 1
        recyclerAdapter?.clearData()
        bidInProgressViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
        bidInProgressViewModel?.getBidInProgressRequest()
    }

    private fun bidInProgressViewModel() {
        bidInProgressViewModel =
            ViewModelProvider(this@MyBidsFragment)[BidderBidInProgressViewModel::class.java]
        binding.lifecycleOwner = this@MyBidsFragment
        bidInProgressViewModel?.getBidInProgressData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.myBidsShimmer.myBidsMainShimmer.setVisibility(false)
                        binding.idBidInProgress.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(true)

                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderBidInProgressResponseModel::class.java
                        )

                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                Log.d("TAG", "dataModel: " + data.data)
                                binding.apply {
                                    numberOfBiddings.text = data.data?.mybids?.size.toString()
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
                                Preferences.removePreference(mcontext, "token")
                                Preferences.removePreference(mcontext, "user_details")
                                Preferences.removePreference(mcontext, "isVerified")
                                Preferences.removePreference(mcontext, "roomId")
                                val signin = Intent(mcontext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                binding.myBidsShimmer.myBidsMainShimmer.setVisibility(false)
                                binding.idBidInProgress.setVisibility(true)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.myBidsShimmer.myBidsMainShimmer.setVisibility(true)
                        binding.idBidInProgress.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.myBidsShimmer.myBidsMainShimmer.setVisibility(true)
                        binding.idBidInProgress.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun myBidsListingAPI() {
        getMyBidsViewModel?.request?.page = "1"
        getMyBidsViewModel?.page = 1
        recyclerAdapter?.clearData()
        getMyBidsViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
        getMyBidsViewModel?.getMyBidsRequest()
    }

    private fun searching() {
        binding.searchLayout.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                activity?.let { CommonUtils.hideKeyboard(binding.searchLayout, it) }
                getMyBidsViewModel?.request?.search = query.trim()
                getMyBidsViewModel?.request?.token =
                    Preferences.getStringPreference(mcontext, TOKEN)
                getMyBidsViewModel?.request?.page = "1"
                getMyBidsViewModel?.page = 1
                recyclerAdapter?.clearData()
                getMyBidsViewModel?.getMyBidsRequest()
                return false
            }
        })
    }

    private fun pullToRefresh() {
        binding.idRefreshLayout.setOnRefreshListener {
            getMyBidsViewModel?.request?.page = "1"
            getMyBidsViewModel?.request?.page = getMyBidsViewModel?.page.toString()
            getMyBidsViewModel?.limit = getMyBidsViewModel?.limit!!
            getMyBidsViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
            getMyBidsViewModel?.page = 1
            recyclerAdapter?.clearData()
            myBidsListingAPI()
            bidInProgressApi()
        }
    }

    private fun initViewModel() {
        getMyBidsViewModel = ViewModelProvider(this@MyBidsFragment)[GetMyBidsViewModel::class.java]
        binding.lifecycleOwner = this@MyBidsFragment
        getMyBidsViewModel?.getMyBidsData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.myBidsShimmer.myBidsMainShimmer.setVisibility(false)
                        binding.idBidInProgress.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetMyBidsResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "myBidsListingResponse: ${data.data}")
                                binding.idRefreshLayout.isRefreshing = false
                                getMyBidsViewModel?.isLastPage = (data.data?.mybids?.size
                                    ?: 0) < (getMyBidsViewModel?.limit?.toInt()!!)
                                getMyBidsViewModel?.isDataLoading = false
                                if (data.data?.mybids?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        myBidRecycler.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        myBidRecycler.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }

                                val arrMyBids: ArrayList<MyBidModel> = arrayListOf()
                                data.data?.mybids?.forEachIndexed { _, myBids ->
                                    arrMyBids.add(
                                        MyBidModel(
                                            orderPlaced = myBids.orderPlaced,
                                            secondTimer = myBids.productDetails?.secondOrderTimer
                                                ?: "",
                                            bidId = myBids.id,
                                            statusText = myBids.bidStatus,
                                            timerToOrder = myBids.productDetails?.orderTimer ?: "",
                                            productImage = myBids.productDetails?.imageUrl ?: "",
                                            productName = myBids.productDetails?.subCategory?.enName,
//                                            if (Preferences.getStringPreference(
//                                                    mcontext, "language"
//                                                ) == "en"
//                                            )
//                                            else myBids.productDetails?.subCategory?.arName,
                                            address = myBids.productDetails?.productLocation,
                                            productWeight = myBids.productDetails?.quantity.toString()
                                                .trim() + " " + myBids.productDetails?.unit,
                                            myBidAmount = myBids.productDetails?.productPrice.toString()
                                                .trim(),
                                            productHighestBid = getString(R.string.sar) + " " + myBids.highestBidAmount.toString()
                                                .trim()
                                        )
                                    )
                                }

                                if (recyclerAdapter == null || recyclerAdapter?.arrMyBids?.isEmpty() == true) {
                                    recyclerAdapter = RecyclerMyBidAdapter()
                                    recyclerAdapter?.onClickListner(this)
                                    binding.myBidRecycler.adapter = recyclerAdapter
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

                            403, 401 -> {
                                Preferences.removePreference(mcontext, "token")
                                Preferences.removePreference(mcontext, "user_details")
                                Preferences.removePreference(mcontext, "isVerified")
                                Preferences.removePreference(mcontext, "roomId")
                                val signin = Intent(mcontext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                binding.myBidsShimmer.myBidsMainShimmer.setVisibility(false)
                                binding.idBidInProgress.setVisibility(true)
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging){
                            binding.myBidsShimmer.myBidsMainShimmer.setVisibility(true)
                            binding.idBidInProgress.setVisibility(false)
                            binding.idRefreshLayout.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.myBidsShimmer.myBidsMainShimmer.setVisibility(true)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        binding.idBidInProgress.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(false)
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            filterMyBids.setOnClickListener {
                if (myBidFilter?.isAdded != true) {
                    myBidFilter = MyBidFilter(filterModel, ::handelFilter) //high order function
                    myBidFilter?.show(parentFragmentManager, myBidFilter?.tag)
                }
            }
            idBidInProgress.setOnClickListener {
                val toBidInProgress = Intent(mcontext, BidInProgress::class.java)
                startActivity(toBidInProgress)
            }
            searchTab.setOnClickListener {
                heading.visibility = View.GONE
                searchTab.visibility = View.GONE
                filterMyBids.visibility = View.GONE
                search.visibility = View.VISIBLE
            }
            cancelButton.setOnClickListener {
                getMyBidsViewModel?.request?.search = null
                getMyBidsViewModel?.request?.token =
                    Preferences.getStringPreference(mcontext, TOKEN)
                getMyBidsViewModel?.request?.page = "1"
                getMyBidsViewModel?.page = 1
                recyclerAdapter?.clearData()
                getMyBidsViewModel?.getMyBidsRequest()
                heading.visibility = View.VISIBLE
                searchTab.visibility = View.VISIBLE
                filterMyBids.visibility = View.VISIBLE
                search.visibility = View.GONE
            }
        }
    }

    override fun onClick(position: Int) {
        val toBidderBiddingDetails = Intent(mcontext, BidderBiddingDetails::class.java)
        toBidderBiddingDetails.putExtra(
            "bidId", recyclerAdapter?.arrMyBids?.getOrNull(position)?.bidId
        )
        toBidderBiddingDetails.putExtra(
            SUB_CATEGORY_NAME, recyclerAdapter?.arrMyBids?.getOrNull(position)?.productName
        )
        getContent.launch(toBidderBiddingDetails)
    }

    private fun filter() {
        getMyBidsViewModel?.request?.category = filterModel?.category
        getMyBidsViewModel?.request?.status = filterModel?.status
        getMyBidsViewModel?.request?.page = "1"
        getMyBidsViewModel?.page = 1
        recyclerAdapter?.clearData()
        myBidsListingAPI()
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "myBidsFilter: ${Gson().toJson(filterModel)}")
        this@MyBidsFragment.filterModel = model
        filter()
    }

    private fun initPagination() {
        binding.myBidRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!getMyBidsViewModel?.isLastPage!! && !getMyBidsViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    getMyBidsViewModel?.page = getMyBidsViewModel?.page!! + 1
                    getMyBidsViewModel?.limit = getMyBidsViewModel?.limit!!
                    getMyBidsViewModel?.isDataLoading = true
//                        binding.loader.setVisibility(getMyBidsViewModel?.limit != 0f)
                    getMyBidsViewModel?.request?.token =
                        Preferences.getStringPreference(mcontext, TOKEN)
                    getMyBidsViewModel?.request?.apply {
                        this.page = getMyBidsViewModel?.page?.toString()
                        this.limit = getMyBidsViewModel?.limit?.toString()
                    }
                    getMyBidsViewModel?.getMyBidsRequest()
                    binding.idProgress.setVisibility(true)
                    isPaging = true
                }
            }
        })
    }
}