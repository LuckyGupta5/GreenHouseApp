package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.view_models.BidHighestDetailViewModel
import com.ripenapps.greenhouse.databinding.ActivityPlaceBidBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.BidSuccessfullPopUp
import com.ripenapps.greenhouse.model.commonModels.addBid.response.AddBidResponseModel
import com.ripenapps.greenhouse.model.commonModels.addBid.response.BidHighestDetailResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.timeLeft
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.Constants.SELLER_ID
import com.ripenapps.greenhouse.utills.Constants.SUB_CATEGORY_NAME
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddBidViewModel

class PlaceBid : BaseActivity() {
    private lateinit var binding: ActivityPlaceBidBinding
    private var addBidViewModel: AddBidViewModel? = null
    private var bidHighestDetailViewModel: BidHighestDetailViewModel? = null
    private var productId: String? = null
    private var sellerId: String? = null
    private var categoryId: String? = null
    var accountBlocked: AccountBlocked? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@PlaceBid, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_bid)

        if (Preferences.getStringPreference(this@PlaceBid, "language").equals("ar")) {
            binding.detailBtn.rotation = 180f
        }

        initListeners()
        intiAddBidViewModel()
        initBidHighestDetailViewModel()
        highestBidDetailApi()

        binding.enterNumber.doAfterTextChanged {
            if (binding.enterNumber.text.isNullOrEmpty() || binding.enterNumber.text.toString().toDouble() <= 0) {
                binding.continueButton.isEnabled = false
                binding.continueButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this@PlaceBid, R.color.inactive_background
                    )
                )
            } else {
                binding.continueButton.isEnabled = true
                binding.continueButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this@PlaceBid, R.color.greenhousetheme
                    )
                )
            }
        }
    }

    private fun highestBidDetailApi() {
        bidHighestDetailViewModel?.request?.bidType = "purchase"
        bidHighestDetailViewModel?.request?.token =
            Preferences.getStringPreference(this@PlaceBid, TOKEN)
        bidHighestDetailViewModel?.request?.productId = intent.getStringExtra(PRODUCT_ID)
        Log.d("TAG", "highestBidDetailApi: ${intent.getStringExtra(PRODUCT_ID)}")
        bidHighestDetailViewModel?.getBidHighestDetailRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initBidHighestDetailViewModel() {
        bidHighestDetailViewModel = ViewModelProvider(this)[BidHighestDetailViewModel::class.java]
        binding.lifecycleOwner = this
        bidHighestDetailViewModel?.getBidHighestDetailData()?.observe(this@PlaceBid) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.placeBidShimmer.placeBidMainShimmer.setVisibility(false)
                        binding.mainLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidHighestDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG",
                                    "initBidHighestDetailViewModel: ${Gson().toJson(data.data)}"
                                )
                                binding.apply {
                                    categoryId = data.data?.subcategory?.categoryId
                                    productId = data.data?.productId
                                    sellerId = data.data?.sellerId
                                    weight.text =
                                        data.data?.quantity.toString() + " " + data.data?.unit
                                    totalBids.text =
                                        data.data?.biddingCount.toString() + " " + getString(R.string.bids)
                                    timeLeft.text =
                                        timeLeft(data.data?.endDate.toString(), this@PlaceBid)
                                    vegetableImage.setImage(data.data?.productImageUrl.toString())
                                    name.text = data.data?.subcategory?.enName
                                    if (data.data?.biddingCount == 0) {
                                        maxBidWarning.text =
                                            getString(R.string.enter) + " " + getString(R.string.sar) + " " + data.data.productPrice + " " + getString(
                                                R.string.or_more
                                            )
                                    } else {
                                        maxBidWarning.text =
                                            getString(R.string.enter) + " " + getString(R.string.sar) + " " + data.data?.highestBid?.amount + " " + getString(
                                                R.string.or_more
                                            )
                                    }
                                }
                            }

                            403,401 -> {
                                val signin = Intent(this@PlaceBid, SignIn::class.java)
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
                                binding.placeBidShimmer.placeBidMainShimmer.setVisibility(false)
                                binding.mainLayout.setVisibility(true)
                                Toast.makeText(
                                    this@PlaceBid, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.placeBidShimmer.placeBidMainShimmer.setVisibility(true)
                        binding.mainLayout.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.placeBidShimmer.placeBidMainShimmer.setVisibility(true)
                        binding.mainLayout.setVisibility(false)
                        Toast.makeText(this@PlaceBid, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun intiAddBidViewModel() {
        addBidViewModel = ViewModelProvider(this@PlaceBid)[AddBidViewModel::class.java]
        binding.lifecycleOwner = this@PlaceBid

        addBidViewModel?.getAddBidData()?.observe(this@PlaceBid) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddBidResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "initViewModel: done" + Gson().toJson(data))
                            val confirmBidPlaced =
                                BidSuccessfullPopUp(binding.enterNumber.text.toString())
                            confirmBidPlaced.show(supportFragmentManager, confirmBidPlaced.tag)
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("status", "update")
                            })
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(
                                    supportFragmentManager, accountBlocked?.tag
                                )
                            }
                        }
                        403,401 -> {
                            val signin = Intent(this@PlaceBid, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }
                        else -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(this@PlaceBid, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this@PlaceBid)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@PlaceBid, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            idCloseBtn.setOnClickListener {
                finish()
            }
            detailLayer.setOnClickListener {
                val toProductDetails = Intent(this@PlaceBid, BidderProductDetails::class.java)
                toProductDetails.putExtra(PRODUCT_ID, productId)
                toProductDetails.putExtra(SELLER_ID, sellerId)
                startActivity(toProductDetails)
            }
            continueButton.setOnClickListener {

                continueButton.isEnabled = false
                Handler().postDelayed({
                    continueButton.isEnabled = true
                }, 2000)

                addBidViewModel?.request?.bidType = "purchase"
                addBidViewModel?.request?.subCategoryName = intent.getStringExtra(SUB_CATEGORY_NAME)
                addBidViewModel?.request?.token =
                    Preferences.getStringPreference(this@PlaceBid, TOKEN)
                addBidViewModel?.request?.amount = binding.enterNumber.text.toString()
                addBidViewModel?.request?.productId = intent.getStringExtra(PRODUCT_ID)
                addBidViewModel?.request?.categoryId = categoryId
                addBidViewModel?.getAddBidRequest()
            }
        }
    }
}