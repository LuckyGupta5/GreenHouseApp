package com.ripenapps.greenhouse.fragment.bidderfragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.bidder.RecyclerBestSellersAdapter
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.databinding.FragmentHomeBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.BestSellerModel
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderProductListLatestResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetBestSellerResponseModel
import com.ripenapps.greenhouse.model.profile.GetUserDetailResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.AboutSeller
import com.ripenapps.greenhouse.screen.bidder.BidderNotification
import com.ripenapps.greenhouse.screen.bidder.BidderProductDetails
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.bidder.PlaceBid
import com.ripenapps.greenhouse.screen.bidder.ViewAllBestSellers
import com.ripenapps.greenhouse.screen.bidder.ViewAllFeaturedProduct
import com.ripenapps.greenhouse.screen.seller.SellerWallet
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
import com.ripenapps.greenhouse.utills.formatToReadableNumber
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.GetBestSellerViewModel
import com.ripenapps.greenhouse.view_models.GetFeaturedProductViewModel
import com.ripenapps.greenhouse.view_models.UserDetailsViewModel

class HomeFragment : Fragment(), RecyclerFeaturedProductAdapter.onClickListner,
    RecyclerBestSellersAdapter.onClickListner {

    // Member variables for view binding and view models
    private val arrFeatureImg: ArrayList<FeaturedProductModel> = arrayListOf()
    private val arrBestSellerImg: ArrayList<BestSellerModel> = arrayListOf()
    private lateinit var mcontext: Context
    private lateinit var homeFragmentBinding: FragmentHomeBinding
    private var recyclerFeatAdapter: RecyclerFeaturedProductAdapter? = null
    private var recyclerselleradapter: RecyclerBestSellersAdapter? = null
    private var bestSellerViewModel: GetBestSellerViewModel? = null
    private var featuredProductViewModel: GetFeaturedProductViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private lateinit var getUserDetailsViewModel: UserDetailsViewModel
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext = context
    }

    override fun onCreateView( // Initialize the fragment
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        homeFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        if (Preferences.getStringPreference(mcontext, "language") == "ar") {
            homeFragmentBinding.greenHouseImg.scaleType = ImageView.ScaleType.FIT_END
        }

        checkNotificationPermission()   // Check notification permission
        initBestSellerViewModel()
        bestSellerApi()  // Make API calls
        intiAddWishListViewModel()  // Initialize ViewModels
        initFeaturedListingViewModel()  // Initialize ViewModels
        featuredListingApi()  // Make API calls
        initViewModel()//user detail Initialize ViewModels
        userDetailApi()  // Make API calls
        initActivityResultInstance()
        return homeFragmentBinding.root
    }


    private fun intiAddWishListViewModel() {
        addWishListViewModel =
            ViewModelProvider(this@HomeFragment)[AddWishListViewModel::class.java]
        homeFragmentBinding.lifecycleOwner = this@HomeFragment

        addWishListViewModel?.getAddWishListData()?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "WishlistResponse: done" + Gson().toJson(data))
                            Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }
    }

    private fun initActivityResultInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        featuredListingApi()
                    }
                }
            }
    }

    private fun userDetailApi() {
        getUserDetailsViewModel.request.token = Preferences.getStringPreference(mcontext, TOKEN)
        getUserDetailsViewModel.getUserDetailsRequest()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        getUserDetailsViewModel =
            ViewModelProvider(this@HomeFragment)[UserDetailsViewModel::class.java]
        homeFragmentBinding.lifecycleOwner = this@HomeFragment
        getUserDetailsViewModel.getUserDetailsData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(false)
                            homeSearch.setVisibility(true)
                            idRefreshLayout.setVisibility(true)
                        }
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                homeFragmentBinding.apply {
                                    homeWallet.text =
                                        "" + data?.data?.userDetails?.amount?.formatToReadableNumber()
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
                                Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
                                homeFragmentBinding.apply {
                                    homeProductShimmer.idMainShimmer.setVisibility(false)
                                    homeSearch.setVisibility(true)
                                    idRefreshLayout.setVisibility(true)
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun featuredListingApi() {
        featuredProductViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
        featuredProductViewModel?.getFeaturedProductRequest()
    }

    private fun initFeaturedListingViewModel() {
        featuredProductViewModel =
            ViewModelProvider(this@HomeFragment)[GetFeaturedProductViewModel::class.java]
        homeFragmentBinding.lifecycleOwner = this@HomeFragment

        featuredProductViewModel?.getFeaturedProductData1()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(false)
                            homeSearch.setVisibility(true)
                            idRefreshLayout.setVisibility(true)
                        }
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidderProductListLatestResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                featuredProductViewModel?.isLastPage = (data.data?.products?.size
                                    ?: 0) < (featuredProductViewModel?.limit?.toInt()!!)
                                featuredProductViewModel?.isDataLoading = false
                                if (data.data?.products?.isEmpty() == true) {
                                    homeFragmentBinding.allFeaturedProduct.setVisibility(false)
                                    homeFragmentBinding.featureProduct.setVisibility(false)
                                } else {
                                    homeFragmentBinding.allFeaturedProduct.setVisibility(false) // to be true
                                    homeFragmentBinding.featureProduct.setVisibility(true)
                                }
                                arrFeatureImg.clear()
                                data.data?.products?.forEachIndexed { _, product ->
                                    arrFeatureImg.add(
                                        FeaturedProductModel(
                                            sellerId = product.sellerId ?: "",
                                            id = product.id ?: "",
                                            vegetableImg = product.imageUrl ?: "",
                                            vegetableName = product.subCategoryId?.enName,
//                                            if (Preferences.getStringPreference(
//                                                    mcontext, "language"
//                                                ) == "en"
//                                            )
//                                            else product?.subCategoryName?.arName,
                                            location = product.productLocation,
                                            vegetableWeight = product.quantity.toString() + " " + product.unit,
                                            time = timeLeft(
                                                product.endDate.toString(), mcontext
                                            ),
                                            wishlist = product.isWishlist,
                                            vegetablePrice = getString(R.string.sar) + " " + product.price,
                                            isBid = product.isBid ?: false
                                        )
                                    )
                                }

                                if (arrFeatureImg.size >= 5) {
                                    homeFragmentBinding.allFeaturedProduct.setVisibility(true)
                                } else {
                                    homeFragmentBinding.allFeaturedProduct.setVisibility(false)
                                }
                                recyclerFeatAdapter = RecyclerFeaturedProductAdapter()
                                recyclerFeatAdapter?.addItems(arrFeatureImg)
                                recyclerFeatAdapter?.onClickListner1(this@HomeFragment)
                                homeFragmentBinding.featuresProductsRecycler.adapter =
                                    recyclerFeatAdapter
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
                                Toast.makeText(
                                    mcontext, data.message, Toast.LENGTH_SHORT
                                ).show()
                                homeFragmentBinding.apply {
                                    homeProductShimmer.idMainShimmer.setVisibility(false)
                                    homeSearch.setVisibility(true)
                                    idRefreshLayout.setVisibility(true)
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        showToast(it.message ?: "No internet")
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun bestSellerApi() {
        bestSellerViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
        bestSellerViewModel?.getBestSellerRequest()
    }

    // Initialize GetBestSellerViewModel and observe data changes
    private fun initBestSellerViewModel() {
        bestSellerViewModel =
            ViewModelProvider(this@HomeFragment)[GetBestSellerViewModel::class.java]
        homeFragmentBinding.lifecycleOwner = this@HomeFragment
        bestSellerViewModel?.getBestSellerData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(false)
                            homeSearch.setVisibility(true)
                            idRefreshLayout.setVisibility(true)
                        }
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetBestSellerResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "myBidsListingResponse: ${data.data}")

                                if (data.data?.bestSellers?.isEmpty() == true) {
                                    homeFragmentBinding.allBestSellers.setVisibility(false)
                                    homeFragmentBinding.bestSeller.setVisibility(false)
                                } else {
                                    homeFragmentBinding.allBestSellers.setVisibility(true)
                                    homeFragmentBinding.bestSeller.setVisibility(true)
                                }
                                arrBestSellerImg.clear()
                                data.data?.bestSellers?.forEachIndexed { _, bestSeller ->
                                    arrBestSellerImg.add(
                                        BestSellerModel(
                                            sellerId = bestSeller?.sellerId,
                                            circleText = CommonUtils.getInitials(bestSeller?.name.toString()),
                                            ratingText = bestSeller?.averageRating?.toDouble()
                                                .toString(),
                                            userNameText = bestSeller?.userName,
                                            locationSeller = bestSeller?.address?.trim(),
                                            countHeartCount = bestSeller?.favoriteCount,
                                            categories = bestSeller?.categories ?: mutableListOf()
                                        )
                                    )
                                }

                                if (arrBestSellerImg.size >= 5) {
                                    homeFragmentBinding.allBestSellers.setVisibility(true)
                                } else {
                                    homeFragmentBinding.allBestSellers.setVisibility(false)
                                }
                                recyclerselleradapter = RecyclerBestSellersAdapter()
                                recyclerselleradapter?.addItems(arrBestSellerImg)
                                recyclerselleradapter?.onClickListner2(this@HomeFragment)
                                homeFragmentBinding.bestSellerHomeRecycler.adapter =
                                    recyclerselleradapter
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
                                Toast.makeText(
                                    mcontext, data.message, Toast.LENGTH_SHORT
                                ).show()
                                homeFragmentBinding.apply {
                                    homeProductShimmer.idMainShimmer.setVisibility(false)
                                    homeSearch.setVisibility(true)
                                    idRefreshLayout.setVisibility(true)
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                        homeFragmentBinding.apply {
                            homeProductShimmer.idMainShimmer.setVisibility(true)
                            homeSearch.setVisibility(false)
                            idRefreshLayout.setVisibility(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    // Initialize listeners for UI interactions
    @SuppressLint("CommitTransaction")
    private fun initListener() {

        // Set up swipe-to-refresh layout
        homeFragmentBinding.idRefreshLayout.setOnRefreshListener {
            bestSellerApi()
            featuredListingApi()
            userDetailApi()
            homeFragmentBinding.idRefreshLayout.isRefreshing = false
        }

        //View All Best Sellers
        homeFragmentBinding.allBestSellers.setOnClickListener {
            val bestSeller = Intent(mcontext, ViewAllBestSellers::class.java)
            startActivity(bestSeller)
        }

        homeFragmentBinding.homeSearch.setOnClickListener {
            callBackToSearch(0)
        }

        homeFragmentBinding.allFeaturedProduct.setOnClickListener {
            val toAllFeaturedProduct = Intent(mcontext, ViewAllFeaturedProduct::class.java)
            startActivity(toAllFeaturedProduct)
        }

        homeFragmentBinding.walletBox.setOnClickListener {
            val toWallet = Intent(mcontext, SellerWallet::class.java)
            toWallet.putExtra("forBidderWallet", "1")
            startActivity(toWallet)
        }

        homeFragmentBinding.vegetableCard.setOnClickListener {
            callBackToSearch(1)
        }
        homeFragmentBinding.fruitsCard.setOnClickListener {
            callBackToSearch(2)
        }
        homeFragmentBinding.datesCard.setOnClickListener {
            callBackToSearch(3)
        }

        homeFragmentBinding.icNotification.setOnClickListener {
            val toNotification = Intent(mcontext, BidderNotification::class.java)
            startActivity(toNotification)
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(mcontext, BidderProductDetails::class.java)
        toProductDetails.putExtra(PRODUCT_ID, arrFeatureImg.getOrNull(position)?.id)
        toProductDetails.putExtra(SELLER_ID, arrFeatureImg.getOrNull(position)?.sellerId)
        try {
            getContent.launch(toProductDetails)
        } catch (e: Exception) {
            Log.d("TAG", "click: ${e.message}")
        }
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(mcontext, PlaceBid::class.java)
        toPlaceBid.putExtra(PRODUCT_ID, arrFeatureImg.getOrNull(position)?.id)
        toPlaceBid.putExtra(
            SUB_CATEGORY_NAME, arrFeatureImg.getOrNull(position)?.categoryName
        )
        getContent.launch(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId = arrFeatureImg.getOrNull(position)?.id
        addWishListViewModel?.request?.token = Preferences.getStringPreference(
            mcontext, TOKEN
        )
        addWishListViewModel?.getAddWishListRequest()
        arrFeatureImg.getOrNull(position)?.wishlist =
            arrFeatureImg.getOrNull(position)?.wishlist != true
        recyclerFeatAdapter!!.notifyItemChanged(position)
    }

    override fun clickBestSeller(position: Int) {
        val tobestSeller = Intent(mcontext, AboutSeller::class.java)
        tobestSeller.putExtra(SELLER_ID, arrBestSellerImg.getOrNull(position)?.sellerId)
        startActivity(tobestSeller)
    }

    private fun callBackToSearch(status: Int) {
        (activity as? Home)?.applyFilter(status)
    }

    // Check and request notification permission
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    CommonUtils.showSettingDialog(mcontext, askedPermission = "Notification")
                }

                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("TAG", "checkNotificationPermission: ")
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast("Notification permission granted")
            } else {
                CommonUtils.showSettingDialog(mcontext, askedPermission = "Notification")
            }
        }
}