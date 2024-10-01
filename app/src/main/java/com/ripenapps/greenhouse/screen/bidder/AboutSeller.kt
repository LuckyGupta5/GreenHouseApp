package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFeaturedProductAdapter
import com.ripenapps.greenhouse.adapter.bidder.RecyclerReviewAdapter
import com.ripenapps.greenhouse.databinding.ActivityAboutSellerBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.datamodels.biddermodel.ReviewModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderProduSameSellerNewResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetReviewsResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetSellerResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.convertUTCDate
import com.ripenapps.greenhouse.utills.CommonUtils.formatDate
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
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
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.BidderProductDetailViewModel
import com.ripenapps.greenhouse.view_models.BidderProductSameSellerViewModel
import com.ripenapps.greenhouse.view_models.GetReviewsViewModel
import com.ripenapps.greenhouse.view_models.GetSellerViewModel

class AboutSeller : BaseActivity(), RecyclerFeaturedProductAdapter.onClickListner {

    private lateinit var binding: ActivityAboutSellerBinding
    private var getReviewViewModel: GetReviewsViewModel? = null
    private val arrReviews: ArrayList<ReviewModel> = arrayListOf()
    private val arrProduct: ArrayList<FeaturedProductModel> = arrayListOf()
    private var bidderProductDetailViewModel: BidderProductDetailViewModel? = null
    private var bidderProductSameSellerViewModel: BidderProductSameSellerViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    private var recyclerSameSellerProductAdapter: RecyclerFeaturedProductAdapter? = null
    private var getSellerViewModel: GetSellerViewModel? = null
    private var isFav: Boolean = false
    private var accountBlocked: AccountBlocked? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@AboutSeller, R.color.status_bar) //status bar color function
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about_seller)

        initListeners()
        initReviews()
        sameSellerViewModel()
        intiAddWishListViewModel()
        initGetSellerViewModel()
        bidderProductDetailViewModel?.productId = intent?.getStringExtra(PRODUCT_ID) ?: ""
        bidderProductDetailViewModel?.sellerId = intent?.getStringExtra(SELLER_ID) ?: ""
        sameSellerListApi()
        getSellerApi()
        getReviewApi()
        initStartActivityInstance()
    }

    private fun getReviewApi() {
        getReviewViewModel?.request?.token =
            Preferences.getStringPreference(this@AboutSeller, TOKEN)
        getReviewViewModel?.request?.sellerId = intent?.getStringExtra(SELLER_ID) ?: ""
        getReviewViewModel?.request?.limit = "10"
        getReviewViewModel?.getReviewsRequest()
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        sameSellerListApi()
                        getSellerApi()
                        getReviewApi()
                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initReviews() {
        getReviewViewModel = ViewModelProvider(this@AboutSeller)[GetReviewsViewModel::class.java]
        binding.lifecycleOwner = this@AboutSeller
        getReviewViewModel?.getReviewsData()?.observe(this@AboutSeller) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetReviewsResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "ReviewResponse: ${Gson().toJson(data.data)}")
                                if (data.data?.reviews.isNullOrEmpty()) {
                                    binding.apply {
                                        recyclerReview.setVisibility(false)
                                        titleRating.setVisibility(false)
                                        avgRating.setVisibility(false)
                                        viewReview.setVisibility(false)
                                        viewAllButton.setVisibility(false)
                                        totalRating.setVisibility(false)
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        recyclerReview.setVisibility(true)
                                        titleRating.setVisibility(true)
                                        avgRating.setVisibility(true)
                                        viewReview.setVisibility(true)
                                        viewAllButton.setVisibility(true)
                                        totalRating.setVisibility(true)
                                    }
                                }
                                arrReviews.clear()
                                data.data?.reviews?.forEachIndexed { _, reviews ->
                                    arrReviews.add(
                                        ReviewModel(
                                            initials = reviews.name,
                                            reviewerName = reviews.name,
                                            reviewDay = formatDate(reviews.createdAt.toString()),
                                            reviewStar = reviews.rating.toString(),
                                            review = reviews.review
                                        )
                                    )
                                }

                                binding.apply {
                                    avgRating.text = data.data?.averageRating.toString()
                                    totalRating.text =
                                        arrReviews.size.toString() + " " + getString(R.string.reviews)
                                }

                                if (arrReviews.size > 5) {
                                    binding.viewAllButton.setVisibility(true)
                                } else {
                                    binding.viewAllButton.setVisibility(false)
                                }
                                val recyclerAdapter = RecyclerReviewAdapter(arrReviews)
                                binding.recyclerReview.adapter = recyclerAdapter
                                val helper: SnapHelper = LinearSnapHelper()
                                helper.attachToRecyclerView(binding.recyclerReview)
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
                                Preferences.removePreference(this@AboutSeller, "token")
                                Preferences.removePreference(this@AboutSeller, "user_details")
                                Preferences.removePreference(this@AboutSeller, "isVerified")
                                Preferences.removePreference(this@AboutSeller, "roomId")
                                val signin = Intent(this@AboutSeller, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    this@AboutSeller, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Toast.makeText(this@AboutSeller, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun getSellerApi() {
        getSellerViewModel?.request?.sellerId = intent?.getStringExtra(SELLER_ID) ?: ""
        getSellerViewModel?.request?.token =
            Preferences.getStringPreference(this@AboutSeller, TOKEN)
        getSellerViewModel?.getSellerRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initGetSellerViewModel() {
        getSellerViewModel = ViewModelProvider(this@AboutSeller)[GetSellerViewModel::class.java]
        binding.lifecycleOwner = this@AboutSeller
        getSellerViewModel?.getSellerData()?.observe(this@AboutSeller) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetSellerResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                Log.d("TAG", "aboutSellerResponse: ${Gson().toJson(data.data)}")
                                binding.apply {
                                    val initials = getInitials(data.data?.seller?.name.toString())
                                    sellerInitials.text = initials
                                    idRating.text = data.data?.seller?.averageRating ?: "0.0"
                                    ratingNumber.text =
                                        data.data?.seller?.favoriteCount.toString() + " " + getString(
                                            R.string.user_fav
                                        )
                                    location.text = data.data?.seller?.address?.trim()
                                    userName.text = data.data?.seller?.userName
                                    joinedDate.text =
                                        getString(R.string.joinedUs) + convertUTCDate(data.data?.seller?.acceptedAt.toString())

                                    if (data.data?.seller?.bio.isNullOrEmpty()) {
                                        infoAboutSeller.setVisibility(false)
                                    } else {
                                        infoAboutSeller.setVisibility(true)
                                        infoAboutSeller.text = data.data?.seller?.bio?.trim()
                                    }
                                    if (data.data?.seller?.isFav == true) {
                                        isFav = true
                                        icAddFav.setImageResource(R.drawable.ic_favorite_seller)
                                    } else {
                                        isFav = false
                                        icAddFav.setImageResource(R.drawable.ic_heart_black)
                                    }

                                    val isVegetable =
                                        data.data?.seller?.categories?.indexOfFirst { it.categoryId == VEGETABLES }
                                            ?: -1
                                    val isFruit =
                                        data.data?.seller?.categories?.indexOfFirst { it.categoryId == FRUITS }
                                            ?: -1
                                    val isDate =
                                        data.data?.seller?.categories?.indexOfFirst { it.categoryId == DATES }
                                            ?: -1
                                    binding.icCapsicum.setVisibility(isVegetable != -1)
                                    binding.icApple.setVisibility(isFruit != -1)
                                    binding.icDates.setVisibility(isDate != -1)
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
                                Preferences.removePreference(this@AboutSeller, "token")
                                Preferences.removePreference(this@AboutSeller, "user_details")
                                Preferences.removePreference(this@AboutSeller, "isVerified")
                                Preferences.removePreference(this@AboutSeller, "roomId")
                                val signin = Intent(this@AboutSeller, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(
                                    false
                                )
                                binding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(this@AboutSeller, data.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Toast.makeText(this@AboutSeller, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel = ViewModelProvider(this@AboutSeller)[AddWishListViewModel::class.java]
        binding.lifecycleOwner = this@AboutSeller
        addWishListViewModel?.getAddWishListData()?.observe(this@AboutSeller) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            Toast.makeText(
                                this@AboutSeller, data.message, Toast.LENGTH_SHORT
                            ).show()
                            isFav = if (isFav) {
                                binding.icAddFav.setImageResource(R.drawable.ic_heart_black)
                                false
                            } else {
                                binding.icAddFav.setImageResource(R.drawable.ic_favorite_seller)
                                true
                            }
                            getSellerApi()
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("status", "update")
                            })
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@AboutSeller, "token")
                            Preferences.removePreference(this@AboutSeller, "user_details")
                            Preferences.removePreference(this@AboutSeller, "isVerified")
                            Preferences.removePreference(this@AboutSeller, "roomId")
                            val signin = Intent(this@AboutSeller, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            Toast.makeText(
                                this@AboutSeller, data.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Toast.makeText(this@AboutSeller, it.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }
    }

    private fun sameSellerListApi() {
        bidderProductSameSellerViewModel?.request?.sellerId =
            intent?.getStringExtra(SELLER_ID) ?: ""
        bidderProductSameSellerViewModel?.request?.token =
            Preferences.getStringPreference(this@AboutSeller, TOKEN)
        bidderProductSameSellerViewModel?.getBidderProductSameSellerRequest()
    }

    private fun sameSellerViewModel() {
        bidderProductSameSellerViewModel =
            ViewModelProvider(this@AboutSeller)[BidderProductSameSellerViewModel::class.java]
        binding.lifecycleOwner = this@AboutSeller
        bidderProductSameSellerViewModel?.getBidderProductSameSellerData()
            ?.observe(this@AboutSeller) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(false)
                            binding.idRefreshLayout.setVisibility(true)
                            val data = Gson().fromJson(
                                AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                                BidderProduSameSellerNewResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {

                                    if (data.data?.products?.isEmpty() == true) {
                                        binding.apply {
                                            recyclerSellerProduct.setVisibility(false)
                                            placeHolder.setVisibility(true)
                                            headingPlaceHolder.setVisibility(true)
                                        }
                                        return@observe
                                    } else {
                                        binding.apply {
                                            recyclerSellerProduct.setVisibility(true)
                                            placeHolder.setVisibility(false)
                                            headingPlaceHolder.setVisibility(false)
                                        }
                                    }
                                    arrProduct.clear()
                                    data.data?.products?.forEachIndexed { _, product ->
                                        arrProduct.add(
                                            FeaturedProductModel(
                                                categoryName = product.subCategoryId?.enName,
                                                sellerId = product.sellerId,
                                                id = product.id,
                                                vegetableImg = product.imageUrl,
                                                vegetableName = product.subCategoryId?.enName?.trim(),
//                                                if (Preferences.getStringPreference(
//                                                        this@AboutSeller, "language"
//                                                    ) == "en"
//                                                )
//                                                else product.subCategoryId?.arName?.trim(),
                                                location = product.productLocation,
                                                vegetableWeight = product.quantity.toString() + " " + product.unit,
                                                time = timeLeft(
                                                    product.endDate.toString(), this@AboutSeller
                                                ),
                                                vegetablePrice = getString(R.string.sar) + " " + product.price,
                                                wishlist = product.isWishlist,
                                                isBid = product.hasUserBid ?: false
                                            )
                                        )
                                    }

                                    if (arrProduct.isNotEmpty()) {
                                        recyclerSameSellerProductAdapter =
                                            RecyclerFeaturedProductAdapter("horizontal")
                                        recyclerSameSellerProductAdapter?.addItems(arrProduct)
                                        recyclerSameSellerProductAdapter?.onClickListner1(this)
                                        binding.recyclerSellerProduct.adapter =
                                            recyclerSameSellerProductAdapter
                                    } else {
                                        showToast("error")
                                    }
                                }

                                403, 401 -> {
                                    Preferences.removePreference(this@AboutSeller, "token")
                                    Preferences.removePreference(this@AboutSeller, "user_details")
                                    Preferences.removePreference(this@AboutSeller, "isVerified")
                                    Preferences.removePreference(this@AboutSeller, "roomId")
                                    val signin = Intent(this@AboutSeller, SignIn::class.java)
                                    startActivity(signin)
                                    finishAffinity()
                                }

                                else -> {
                                    binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(
                                        false
                                    )
                                    binding.idRefreshLayout.setVisibility(true)
                                    Toast.makeText(
                                        this@AboutSeller, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        Status.LOADING -> {
                            binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            Toast.makeText(this@AboutSeller, it.message, Toast.LENGTH_SHORT).show()
                            binding.aboutSellerShimmer.aboutSellerMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        binding.apply {

            idRefreshLayout.setOnRefreshListener {
                sameSellerListApi()
                getSellerApi()
                getReviewApi()
            }
            backButton.setOnClickListener {
                finish()
            }
            icAddFav.setOnClickListener {
                addWishListViewModel?.request?.type = "seller"
                addWishListViewModel?.request?.sellerId = intent?.getStringExtra(SELLER_ID) ?: ""
                addWishListViewModel?.request?.token =
                    Preferences.getStringPreference(this@AboutSeller, TOKEN)
                addWishListViewModel?.getAddWishListRequest()
            }

            viewAllButton.setOnClickListener {
                val toRating = Intent(this@AboutSeller, SellerAllReview::class.java)
                toRating.putExtra(SELLER_ID, intent?.getStringExtra(SELLER_ID) ?: "")
                startActivity(toRating)
            }

            AllLayout.setOnClickListener {
                AllLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.green_house_color_border)
                vegetableLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                fruitsLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                datesLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                allProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.white
                    )
                )
                vegetableProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                fruitProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                datesProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                bidderProductSameSellerViewModel?.request?.category = null
                sameSellerListApi()
            }

            vegetableLayout.setOnClickListener {
                vegetableLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.green_house_color_border)
                AllLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                fruitsLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                datesLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                vegetableProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.white
                    )
                )
                allProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                fruitProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                datesProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                bidderProductSameSellerViewModel?.request?.category = VEGETABLES
                sameSellerListApi()
            }

            fruitsLayout.setOnClickListener {
                fruitsLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.green_house_color_border)
                vegetableLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                AllLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                datesLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                fruitProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.white
                    )
                )
                vegetableProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                allProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                datesProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                bidderProductSameSellerViewModel?.request?.category = FRUITS
                sameSellerListApi()
            }

            datesLayout.setOnClickListener {
                datesLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.green_house_color_border)
                vegetableLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                fruitsLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                AllLayout.background =
                    ContextCompat.getDrawable(this@AboutSeller, R.drawable.ebebeb_border)
                datesProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.white
                    )
                )
                vegetableProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                fruitProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                allProduct.setTextColor(
                    ContextCompat.getColor(
                        this@AboutSeller, R.color.black
                    )
                )
                bidderProductSameSellerViewModel?.request?.category = DATES
                sameSellerListApi()
            }
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(this@AboutSeller, BidderProductDetails::class.java)
        toProductDetails.putExtra(PRODUCT_ID, arrProduct.getOrNull(position)?.id)
        toProductDetails.putExtra(SELLER_ID, arrProduct.getOrNull(position)?.sellerId)
        startActivity(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(this@AboutSeller, PlaceBid::class.java)
        toPlaceBid.putExtra(PRODUCT_ID, arrProduct.getOrNull(position)?.id)
        toPlaceBid.putExtra(SUB_CATEGORY_NAME, arrProduct.getOrNull(position)?.categoryName)
        getContent.launch(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId = arrProduct.getOrNull(position)?.id
        addWishListViewModel?.request?.token =
            Preferences.getStringPreference(this@AboutSeller, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        arrProduct.getOrNull(position)?.wishlist = arrProduct.getOrNull(position)?.wishlist != true
        recyclerSameSellerProductAdapter!!.notifyItemChanged(position)
    }
}