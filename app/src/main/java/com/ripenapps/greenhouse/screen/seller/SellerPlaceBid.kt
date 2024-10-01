package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.ripenapps.greenhouse.databinding.ActivitySellerPlaceBidBinding
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
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.view_models.AddBidViewModel
import com.ripenapps.greenhouse.view_models.BidHighestDetailViewModel

class SellerPlaceBid : BaseActivity() {
    private lateinit var sellerPlaceBidBinding: ActivitySellerPlaceBidBinding
    private var addBidViewModel: AddBidViewModel? = null
    private var bidHighestDetailViewModel: BidHighestDetailViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SellerPlaceBid, R.color.status_bar)

        sellerPlaceBidBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_place_bid)
        if (Preferences.getStringPreference(this@SellerPlaceBid, "language") == "ar") {
            sellerPlaceBidBinding.idCloseBtn.rotation = 180f
        }

        sellerPlaceBidBinding.apply {
            enterNumber.doAfterTextChanged {
                if (enterNumber.text.isNullOrEmpty() || enterNumber.text.toString()
                        .toDouble() <= 0
                ) {
                    continueButton.isEnabled = false
                    continueButton.setBackgroundColor(
                        ContextCompat.getColor(
                            this@SellerPlaceBid, R.color.inactive_background
                        )
                    )
                } else {
                    continueButton.isEnabled = true
                    continueButton.setBackgroundColor(
                        ContextCompat.getColor(
                            this@SellerPlaceBid, R.color.greenhousetheme
                        )
                    )
                }
            }
        }
        initListeners()
        intiAddBidViewModel()
        initBidHighestDetailViewModel()
        initRequest()
    }

    private fun initRequest() {
        bidHighestDetailViewModel?.request?.bidType = "featured"
        bidHighestDetailViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerPlaceBid, TOKEN)
        bidHighestDetailViewModel?.request?.productId = intent.getStringExtra(PRODUCT_ID)
        bidHighestDetailViewModel?.request?.biddingDate = intent.getStringExtra("biddingDate")
        Log.d("TAG", "biddingDate: ${Gson().toJson(bidHighestDetailViewModel?.request)}")
        bidHighestDetailViewModel?.getBidHighestDetailRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initBidHighestDetailViewModel() {
        bidHighestDetailViewModel = ViewModelProvider(this)[BidHighestDetailViewModel::class.java]
        sellerPlaceBidBinding.lifecycleOwner = this
        bidHighestDetailViewModel?.getBidHighestDetailData()?.observe(this@SellerPlaceBid) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            BidHighestDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                sellerPlaceBidBinding.apply {
                                    totalBids.text =
                                        data.data?.biddingCount.toString() + " " + getString(R.string.bids)
                                    timeLeft.text =
                                        timeLeft(data.data?.endDate.toString(), this@SellerPlaceBid)
                                    productId = data.data?.productId
                                    vegetableImage.setImage(data.data?.productImageUrl.toString())
                                    name.text = data.data?.subcategory?.enName
                                    weight.text =
                                        data.data?.quantity.toString() + " " + data.data?.unit
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message ?: "")
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                val signin = Intent(this@SellerPlaceBid, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(
                                    this@SellerPlaceBid, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@SellerPlaceBid)
                    }

                    Status.ERROR -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@SellerPlaceBid, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun intiAddBidViewModel() {
        addBidViewModel = ViewModelProvider(this@SellerPlaceBid)[AddBidViewModel::class.java]
        sellerPlaceBidBinding.lifecycleOwner = this@SellerPlaceBid

        addBidViewModel?.getAddBidData()?.observe(this@SellerPlaceBid) {
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
                                BidSuccessfullPopUp(sellerPlaceBidBinding.enterNumber.text.toString())
                            confirmBidPlaced.show(supportFragmentManager, confirmBidPlaced.tag)
                            setResult(RESULT_OK, Intent().apply {
                                this.putExtra("status", "true")
                            })
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message ?: "")
                                accountBlocked?.show(supportFragmentManager, accountBlocked?.tag)
                            }
                        }

                        403, 401 -> {
                            val signin = Intent(this@SellerPlaceBid, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(this@SellerPlaceBid, data.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this@SellerPlaceBid)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@SellerPlaceBid, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {
        sellerPlaceBidBinding.apply {
            idCloseBtn.setOnClickListener {
                finish()
            }
        }

        sellerPlaceBidBinding.detailLayer.setOnClickListener {
            val intent = Intent(this, SellerProductDetail::class.java)
            intent.putExtra(PRODUCT_ID, productId)
            startActivity(intent)
        }

        sellerPlaceBidBinding.continueButton.setOnClickListener {
            addBidViewModel?.request?.bidType = "featured"
            addBidViewModel?.request?.token =
                Preferences.getStringPreference(this@SellerPlaceBid, TOKEN)
            addBidViewModel?.request?.amount = sellerPlaceBidBinding.enterNumber.text.toString()
            Log.d("TAG", "initListeners: " + addBidViewModel?.request?.amount)
            addBidViewModel?.request?.productId = intent.getStringExtra(PRODUCT_ID)
            addBidViewModel?.request?.biddingDate = intent.getStringExtra("biddingDate")
            addBidViewModel?.getAddBidRequest()
        }
    }
}