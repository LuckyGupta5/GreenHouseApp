package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import com.ripenapps.greenhouse.adapter.seller.RecyclerImageSliderAdapter
import com.ripenapps.greenhouse.databinding.ActivityBidderProductDetailsBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FeaturedProductModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderProduSameSellerNewResponseModel
import com.ripenapps.greenhouse.model.bidder.response.BidderProductDetailResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.getInitials
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddWishListViewModel
import com.ripenapps.greenhouse.view_models.BidderProductDetailViewModel
import com.ripenapps.greenhouse.view_models.BidderProductSameSellerViewModel

@Suppress("DEPRECATION")
class BidderProductDetails : BaseActivity(), RecyclerFeaturedProductAdapter.onClickListner {
    private val arrSameSellerProduct: ArrayList<FeaturedProductModel> = arrayListOf()
    private lateinit var binding: ActivityBidderProductDetailsBinding
    private var bidderProductDetailViewModel: BidderProductDetailViewModel? = null
    private var bidderProductSameSellerViewModel: BidderProductSameSellerViewModel? = null
    private var addWishListViewModel: AddWishListViewModel? = null
    private var isWishlistSelected: Boolean = false
    var accountBlocked: AccountBlocked? = null
    private var recyclerSameSellerProductAdapter: RecyclerFeaturedProductAdapter? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>
    private var subCategoryName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bidder_product_details)

        if (Preferences.getStringPreference(this@BidderProductDetails, "language") == "ar") {
            binding.backbtn.rotation = 180f
            binding.icForward.rotation = 180f
            binding.shareProduct.setImageDrawable(
                ContextCompat.getDrawable(
                    this@BidderProductDetails, R.drawable.ic_reverse_share
                )
            )
        }

        initListener()
        productDetailModel()
        sameSellerViewModel()
        intiAddWishListViewModel()
        initStartActivityInstance()

        bidderProductDetailViewModel?.productId = intent?.getStringExtra(PRODUCT_ID) ?: ""
        bidderProductDetailViewModel?.sellerId = intent?.getStringExtra(SELLER_ID) ?: ""

        productDetailApi()
        sameSellerListApi()
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        productDetailApi()
                        sameSellerListApi()
                    }
                }
            }
    }

    private fun intiAddWishListViewModel() {
        addWishListViewModel =
            ViewModelProvider(this@BidderProductDetails)[AddWishListViewModel::class.java]
        binding.lifecycleOwner = this@BidderProductDetails

        addWishListViewModel?.getAddWishListData()?.observe(this@BidderProductDetails) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("status", "update")
                            })
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@BidderProductDetails, "token")
                            Preferences.removePreference(this@BidderProductDetails, "user_details")
                            Preferences.removePreference(this@BidderProductDetails, "isVerified")
                            Preferences.removePreference(this@BidderProductDetails, "roomId")
                            val signin = Intent(this@BidderProductDetails, SignIn::class.java)
                            startActivity(signin)
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
                            Toast.makeText(
                                this@BidderProductDetails, data.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Toast.makeText(this@BidderProductDetails, it.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }
    }

    private fun sameSellerListApi() {
        bidderProductSameSellerViewModel?.request?.sellerId = bidderProductDetailViewModel?.sellerId
        bidderProductSameSellerViewModel?.request?.productId =
            bidderProductDetailViewModel?.productId
        bidderProductSameSellerViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderProductDetails, TOKEN)
        bidderProductSameSellerViewModel?.getBidderProductSameSellerRequest()
    }

    private fun productDetailApi() {
        bidderProductDetailViewModel?.request?.productId = bidderProductDetailViewModel?.productId
        bidderProductDetailViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderProductDetails, TOKEN)
        bidderProductDetailViewModel?.getBidderProductDetailRequest()
    }

    private fun sameSellerViewModel() {
        bidderProductSameSellerViewModel =
            ViewModelProvider(this@BidderProductDetails)[BidderProductSameSellerViewModel::class.java]
        binding.lifecycleOwner = this@BidderProductDetails
        bidderProductSameSellerViewModel?.getBidderProductSameSellerData()
            ?.observe(this@BidderProductDetails) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(false)
                                scrollView.setVisibility(true)
                            }
                            val data = Gson().fromJson(
                                AESHelper.decrypt(SECRET_KEY, it.data),
                                BidderProduSameSellerNewResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    Log.d(
                                        "TAG",
                                        "sameSellerViewModel: ${Gson().toJson(data.data?.products)}"
                                    )
                                    if (data.data?.products?.isEmpty() == true) {
                                        binding.apply {
                                            recyclerSameSellerProduct.visibility = View.GONE
                                            sameSellerHeading.setVisibility(false)
                                            viewAllButton.visibility = View.GONE
                                        }

                                        return@observe
                                    } else {
                                        binding.apply {
                                            recyclerSameSellerProduct.visibility = View.VISIBLE
                                            sameSellerHeading.setVisibility(true)
                                            viewAllButton.visibility = View.GONE
                                        }
                                    }

                                    data.data?.products?.forEachIndexed { _, product ->
                                        arrSameSellerProduct.add(
                                            FeaturedProductModel(
                                                sellerId = product.sellerId,
                                                id = product.id,
                                                vegetableImg = product.imageUrl,
                                                vegetableName = product.subCategoryId?.enName?.trim(),
//                                                if (Preferences.getStringPreference(
//                                                        this@BidderProductDetails, "language"
//                                                    ) == "en"
//                                                )
//                                                else product.subCategoryId?.arName?.trim(),
                                                location = product.productLocation,
                                                vegetableWeight = product.quantity.toString() + " " + product.unit,
                                                time = timeLeft(
                                                    product.endDate.toString(),
                                                    this@BidderProductDetails
                                                ),
                                                vegetablePrice = getString(R.string.sar) + " " + product.price,
                                                wishlist = product.isWishlist,
                                                isFeatured = product.isFeatured,
                                                isBid = product.hasUserBid ?: false
                                            )
                                        )
                                    }

                                    recyclerSameSellerProductAdapter =
                                        RecyclerFeaturedProductAdapter("horizontal")
                                    recyclerSameSellerProductAdapter?.addItems(arrSameSellerProduct)
                                    recyclerSameSellerProductAdapter?.onClickListner1(this@BidderProductDetails)
                                    binding.recyclerSameSellerProduct.adapter =
                                        recyclerSameSellerProductAdapter
                                }

                                401, 403 -> {
                                    val toSignIn =
                                        Intent(this@BidderProductDetails, SignIn::class.java)
                                    startActivity(toSignIn)
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
                                    Toast.makeText(
                                        this@BidderProductDetails, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                    binding.apply {
                                        productDetailShimmer.productDetailMainShimmer.setVisibility(
                                            false
                                        )
                                        scrollView.setVisibility(true)
                                    }
                                }
                            }
                        }

                        Status.LOADING -> {
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            Toast.makeText(
                                this@BidderProductDetails, it.message, Toast.LENGTH_SHORT
                            ).show()
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun productDetailModel() {
        bidderProductDetailViewModel =
            ViewModelProvider(this@BidderProductDetails)[BidderProductDetailViewModel::class.java]
        binding.lifecycleOwner = this@BidderProductDetails
        bidderProductDetailViewModel?.getBidderProductDetailData()
            ?.observe(this@BidderProductDetails) {
                try {
                    when (it.status) {
                        Status.SUCCESS -> {
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(false)
                                scrollView.setVisibility(true)
                            }
                            val data = Gson().fromJson(
                                AESHelper.decrypt(SECRET_KEY, it.data),
                                BidderProductDetailResponseModel::class.java
                            )
                            when (data.status) {
                                200 -> {
                                    Log.d("TAG", "productDetailModel: ${Gson().toJson(data.data)}")
                                    binding.apply {
                                        subCategoryName =
                                            data.data?.productDetails?.subCategoryId?.enName
                                        if (data.data?.productDetails?.isBid == true) {
                                            highestBidLayer.setVisibility(true)
                                            highestBid.text =
                                                getString(R.string.sar) + " " + data.data.productDetails.highestBidAmount.toString()
                                            highestBidTitle.text = getString(R.string.your_bid)
                                            highestBiddingAmount.text =
                                                getString(R.string.sar) + " " + data.data.productDetails.myBidAmount.toString()
                                            idPlaceABidBtn.text = getString(R.string.change_bid)
                                        } else {
                                            highestBiddingAmount.text =
                                                getString(R.string.sar) + " " + data.data?.productDetails?.highestBidAmount.toString()
                                        }
                                        val initials =
                                            getInitials(data.data?.productDetails?.seller?.name.toString())
                                        sellerInitials.text = initials
                                        ratingNumber.text =
                                            data.data?.productDetails?.seller?.favoriteCount.toString() + " " + getString(
                                                R.string.user_fav
                                            )

                                        productName.text =
                                            data.data?.productDetails?.subCategoryId?.enName
//                                        if (Preferences.getStringPreference(
//                                                this@BidderProductDetails, "language"
//                                            ) == "en"
//                                        )
//                                        else data.data?.productDetails?.subCategoryId?.arName

                                        idRating.text =
                                            data.data?.productDetails?.averageRating?.toDouble()
                                                .toString()
                                        location.text = data.data?.productDetails?.productLocation
                                        userName.text = data.data?.productDetails?.seller?.userName
                                        productAmount.text =
                                            getString(R.string.sar) + " " + data.data?.productDetails?.price
                                        desc.text = data.data?.productDetails?.description
                                        val quantity = data.data?.productDetails?.quantity ?: ""
                                        val unit =
                                            data.data?.productDetails?.unit?.lowercase() ?: ""
                                        productWeight.text = "$quantity $unit"
                                        timeLeft.text = timeLeft(
                                            data.data?.productDetails?.endDate.toString(),
                                            this@BidderProductDetails
                                        ) + getString(R.string.for_bidding)
                                        if (data?.data?.productDetails?.isWishlist == true) {
                                            isWishlistSelected = true
                                            idWishlist.setImageResource(R.drawable.ic_wisghlist_green)
                                        } else {
                                            isWishlistSelected = false
                                            idWishlist.setImageResource(R.drawable.ic_wishlist)
                                        }
                                        val recyclerAdapter = RecyclerImageSliderAdapter(
                                            this@BidderProductDetails,
                                            data.data?.productDetails?.imageUrl as MutableList<String>
                                        )
                                        binding.productImage.adapter = recyclerAdapter
                                        val helper: SnapHelper = LinearSnapHelper()
                                        helper.attachToRecyclerView(productImage)
                                    }
//
                                }

                                401, 403 -> {
                                    val toSignIn =
                                        Intent(this@BidderProductDetails, SignIn::class.java)
                                    startActivity(toSignIn)
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
                                    Toast.makeText(
                                        this@BidderProductDetails, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                    binding.apply {
                                        productDetailShimmer.productDetailMainShimmer.setVisibility(
                                            false
                                        )
                                        scrollView.setVisibility(true)
                                    }
                                }

                            }
                        }

                        Status.LOADING -> {
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                        }

                        Status.ERROR -> {
                            Log.e("TAG", "initViewModel: ${it.message}")
                            binding.apply {
                                productDetailShimmer.productDetailMainShimmer.setVisibility(true)
                                scrollView.setVisibility(false)
                            }
                            Toast.makeText(
                                this@BidderProductDetails, it.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
    }

    private fun initListener() {
        binding.apply {
            shareProduct.setOnClickListener {
                shareProduct.isEnabled = false
                Handler().postDelayed({
                    shareProduct.isEnabled = true
                }, 2000)

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "http://13.235.137.221:6002/api/v1/bidder/product/details/id?productId=${
                            intent?.getStringExtra(PRODUCT_ID)
                        }"
                    )
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            backbtn.setOnClickListener {
                finish()
            }

            idPlaceABidBtn.setOnClickListener {
                val toPlaceBid = Intent(this@BidderProductDetails, PlaceBid::class.java)
                toPlaceBid.putExtra(PRODUCT_ID, bidderProductDetailViewModel?.productId)
                toPlaceBid.putExtra(SUB_CATEGORY_NAME, subCategoryName)
                getContent.launch(toPlaceBid)
            }

            sellerDetailLayer.setOnClickListener {
                val toAboutSeller = Intent(this@BidderProductDetails, AboutSeller::class.java)
                toAboutSeller.putExtra(PRODUCT_ID, intent?.getStringExtra(PRODUCT_ID) ?: "")
                toAboutSeller.putExtra(SELLER_ID, intent?.getStringExtra(SELLER_ID) ?: "")
                getContent.launch(toAboutSeller)
            }

            idWishlist.setOnClickListener {
                addWishListViewModel?.request?.type = "product"
                addWishListViewModel?.request?.productId = bidderProductDetailViewModel?.productId
                addWishListViewModel?.request?.token =
                    Preferences.getStringPreference(this@BidderProductDetails, TOKEN)
                addWishListViewModel?.getAddWishListRequest()

                isWishlistSelected = if (isWishlistSelected) {
                    binding.idWishlist.setImageResource(R.drawable.ic_wishlist)
                    Toast.makeText(
                        this@BidderProductDetails,
                        getString(R.string.wishlist_removed),
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    binding.idWishlist.setImageResource(R.drawable.ic_wisghlist_green)
                    Toast.makeText(
                        this@BidderProductDetails,
                        getString(R.string.wishlist_added),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            }
        }
    }

    override fun click(position: Int) {
        val toProductDetails = Intent(this@BidderProductDetails, BidderProductDetails::class.java)
        toProductDetails.putExtra(PRODUCT_ID, arrSameSellerProduct.getOrNull(position)?.id)
        toProductDetails.putExtra(SELLER_ID, arrSameSellerProduct.getOrNull(position)?.sellerId)
        startActivity(toProductDetails)
    }

    override fun onClickBid(position: Int) {
        val toPlaceBid = Intent(this@BidderProductDetails, PlaceBid::class.java)
        toPlaceBid.putExtra(
            PRODUCT_ID, arrSameSellerProduct.getOrNull(position)?.id
        )
        toPlaceBid.putExtra(
            SUB_CATEGORY_NAME, arrSameSellerProduct.getOrNull(position)?.categoryName
        )
        getContent.launch(toPlaceBid)
    }

    override fun onClickWishlist(position: Int) {
        addWishListViewModel?.request?.type = "product"
        addWishListViewModel?.request?.productId = arrSameSellerProduct.getOrNull(position)?.id
        addWishListViewModel?.request?.token =
            Preferences.getStringPreference(this@BidderProductDetails, TOKEN)
        addWishListViewModel?.getAddWishListRequest()
        arrSameSellerProduct[position].wishlist =
            arrSameSellerProduct.getOrNull(position)?.wishlist != true
        recyclerSameSellerProductAdapter!!.notifyItemChanged(position)
    }
}