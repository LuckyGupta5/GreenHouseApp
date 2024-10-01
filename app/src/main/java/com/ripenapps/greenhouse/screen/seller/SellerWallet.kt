package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerTransactionHistory
import com.ripenapps.greenhouse.databinding.ActivitySellerWalletBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.TransactionHistoryModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.WalletFilter
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.seller.response.WalletHistoryResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.TermCondition
import com.ripenapps.greenhouse.screen.bidder.AddMoneyInWallet
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.fullDateAndTimeFormat
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setInvisible
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.WalletHistoryViewModel

class SellerWallet : BaseActivity() {
    private lateinit var sellerWalletBinding: ActivitySellerWalletBinding
    private var availableBalance: String? = null
    private var totalBalance: String? = null
    private var freezedBalance: String? = null
    private var walletHistory: WalletHistoryViewModel? = null
    private var recyclerHistoryAdapter: RecyclerTransactionHistory? = null
    private var filterModel: FilterModel? = null
    private var loginData: LoginResponseModel.LoginData? = null
    private var isPaging: Boolean = false
    private var sellerWalletFilter: WalletFilter? = null
    var accountBlocked: AccountBlocked? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@SellerWallet, R.color.greenhousetheme)
        super.onCreate(savedInstanceState)
        sellerWalletBinding = DataBindingUtil.setContentView(this, R.layout.activity_seller_wallet)
        initPagination()
        initListeners()
        initHistoryModel()
        initSpannable()
        initStartActivityInstance()
        walletHistory?.request?.page = "1"
        recyclerHistoryAdapter?.clearData()
        transactionApi()

        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@SellerWallet, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        if (intent.getStringExtra("forBidderWallet") == "1") {
            sellerWalletBinding.withdrawMoney.setVisibility(false)
        } else {
            sellerWalletBinding.withdrawMoney.setVisibility(true)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        walletHistory?.request?.page = "1"
        recyclerHistoryAdapter?.clearData()
        transactionApi()
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    Log.d("TAG", "initStartActivityInstance: $status")
                    if (status == "update") {
                        transactionApi()
                    }
                }
            }
    }

    private fun filter() {
        walletHistory?.request?.search = filterModel?.transactionType
        walletHistory?.request?.page = "1"
        recyclerHistoryAdapter?.clearData()
        transactionApi()
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "initListeners: ${Gson().toJson(filterModel)}")
        this@SellerWallet.filterModel = model
        filter()
    }

    private fun transactionApi() {
        walletHistory?.request?.token = Preferences.getStringPreference(this, TOKEN)
        walletHistory?.walletHistoryRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun initHistoryModel() {
        walletHistory = ViewModelProvider(this@SellerWallet)[WalletHistoryViewModel::class.java]
        sellerWalletBinding.lifecycleOwner = this@SellerWallet
        walletHistory?.walletHistoryData()?.observe(this@SellerWallet) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        sellerWalletBinding.wallerShimmer.walletMainShimmer.setVisibility(false)
                        sellerWalletBinding.totalBalanceLayout.setVisibility(true)
                        sellerWalletBinding.progessLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            WalletHistoryResponseModel::class.java
                        )
                        sellerWalletBinding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "initHistoryModel: ${Gson().toJson(data)}")
                                sellerWalletBinding.idRefreshLayout.isRefreshing = false
                                availableBalance = "%.2f".format(data.data?.availableWalletAmount)
                                loginData?.userDetails?.amount = data.data?.availableWalletAmount
                                Log.d("TAG", "Amount Check: ${loginData?.userDetails?.amount}")
                                freezedBalance = data.data?.freezedWalletAmount.toString()
                                totalBalance = data.data?.walletTotalAmount.toString()
                                val freeze = data.data?.freezedWalletAmount
                                val available = data.data?.availableWalletAmount
                                val total = (freeze ?: 0.0) + (available ?: 0.0)
                                sellerWalletBinding.totalBalance.text =
                                    getString(R.string.sar) + " " + "%.2f".format(total)
                                sellerWalletBinding.availableBal.text =
                                    getString(R.string.sar) + " " + "%.2f".format(data.data?.availableWalletAmount)
                                sellerWalletBinding.freezeAmount.text =
                                    getString(R.string.sar) + " " + "%.2f".format(data.data?.freezedWalletAmount)
                                if (!isPaging && data.data?.walletHistory?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    sellerWalletBinding.apply {
                                        progessLayout.setVisibility(false)
                                        placeHolder.setVisibility(true)
                                        headingPlaceHolder.setVisibility(true)
                                    }
                                    return@observe
                                } else {
                                    sellerWalletBinding.apply {
                                        progessLayout.setVisibility(true)
                                        placeHolder.setVisibility(false)
                                        headingPlaceHolder.setVisibility(false)
                                    }
                                }
                                val arrTransactionHistory: MutableList<TransactionHistoryModel> =
                                    mutableListOf()
                                data.data?.walletHistory?.forEachIndexed { _, history ->
                                    arrTransactionHistory.add(
                                        TransactionHistoryModel(
                                            transactionId = history.transactionId,
                                            aedText = history.transactionSource,
                                            time = fullDateAndTimeFormat(history.updatedAt),
                                            amount = if (history.transactionType == "debit") "-" + getString(
                                                R.string.sar
                                            ) + " " + history.amount.toString()
                                            else "+" + getString(R.string.sar) + " " + history.amount.toString(),
                                            transactionType = history.transactionType
                                        )
                                    )
                                }
                                if (recyclerHistoryAdapter == null || recyclerHistoryAdapter?.arrTransactionHistory?.isEmpty() == true) {
                                    recyclerHistoryAdapter = RecyclerTransactionHistory()
                                    sellerWalletBinding.transactionHistoryRecycler.adapter =
                                        recyclerHistoryAdapter
                                    recyclerHistoryAdapter?.addItems(arrTransactionHistory)
                                } else {
                                    recyclerHistoryAdapter?.updateList(arrTransactionHistory)
                                }
                                sellerWalletBinding.idRefreshLayout.isRefreshing = false
                                walletHistory?.isLastPage = (data.data?.walletHistory?.size
                                    ?: 0) < (walletHistory?.limit?.toInt()!!)
                                walletHistory?.isDataLoading = false
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

                            403, 401 -> {
                                Preferences.removePreference(this@SellerWallet, "token")
                                Preferences.removePreference(this@SellerWallet, "user_details")
                                Preferences.removePreference(this@SellerWallet, "isVerified")
                                Preferences.removePreference(this@SellerWallet, "roomId")
                                val signin = Intent(this@SellerWallet, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                sellerWalletBinding.wallerShimmer.walletMainShimmer.setVisibility(false)
                                sellerWalletBinding.totalBalanceLayout.setVisibility(true)
                                sellerWalletBinding.progessLayout.setVisibility(true)
                                Toast.makeText(
                                    this@SellerWallet, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging) {
                            sellerWalletBinding.wallerShimmer.walletMainShimmer.setVisibility(true)
                            sellerWalletBinding.totalBalanceLayout.setInvisible()
                            sellerWalletBinding.progessLayout.setVisibility(false)
                            sellerWalletBinding.placeHolder.setVisibility(false)
                            sellerWalletBinding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        sellerWalletBinding.wallerShimmer.walletMainShimmer.setVisibility(true)
                        sellerWalletBinding.totalBalanceLayout.setInvisible()
                        sellerWalletBinding.progessLayout.setVisibility(false)
                        sellerWalletBinding.placeHolder.setVisibility(false)
                        sellerWalletBinding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@SellerWallet, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initSpannable() {
        val terms = sellerWalletBinding.walletTerms
        val textLine = getString(R.string.wallet_term_condition)
        val ss = SpannableString(textLine)
        val termsText = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val toTerms = Intent(this@SellerWallet, TermCondition::class.java)
                if (Preferences.getBooleanPreference(this@SellerWallet, "isBidder")) {
                    toTerms.putExtra("fromBidder", "1")
                }
                startActivity(toTerms)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.WHITE
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(termsText, 38, textLine.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        terms.movementMethod = LinkMovementMethod.getInstance()
        terms.text = ss
    }

    @SuppressLint("ResourceAsColor")
    private fun initListeners() {
        sellerWalletBinding.idRefreshLayout.setOnRefreshListener {
            walletHistory?.page = 1
            walletHistory?.request?.page = walletHistory?.page.toString()
            walletHistory?.limit = walletHistory?.limit ?: 10f
            walletHistory?.request?.token = Preferences.getStringPreference(this, TOKEN)
            recyclerHistoryAdapter?.clearData()
            transactionApi()
        }

        sellerWalletBinding.closeButton.setOnClickListener {
            finish()
        }

        sellerWalletBinding.addMoney.setOnClickListener {
            val addMoney = Intent(this@SellerWallet, AddMoneyInWallet::class.java)
            addMoney.putExtra("availableBalance", availableBalance)
            addMoney.putExtra("forAddMoney", "1")
            getContent.launch(addMoney)
        }

        sellerWalletBinding.withdrawMoney.setOnClickListener {
            val addMoney = Intent(this@SellerWallet, AddMoneyInWallet::class.java)
            addMoney.putExtra("availableBalance", availableBalance)
            addMoney.putExtra("forAddMoney", "2")
            getContent.launch(addMoney)
        }
        sellerWalletBinding.sellerWalletFilter.setOnClickListener {
            if (sellerWalletFilter?.isAdded != true) {
                sellerWalletFilter = WalletFilter(filterModel, isFromGraph = true, ::handelFilter)
                sellerWalletFilter?.show(supportFragmentManager, sellerWalletFilter?.tag)
            }
        }
    }

    private fun initPagination() {
        sellerWalletBinding.transactionHistoryRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!walletHistory?.isLastPage!! && !walletHistory?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    walletHistory?.page = walletHistory?.page!! + 1
                    walletHistory?.limit = walletHistory?.limit!!
                    walletHistory?.isDataLoading = true
                    sellerWalletBinding.idProgress.setVisibility(true)
                    isPaging = true
                    walletHistory?.request?.token =
                        Preferences.getStringPreference(this@SellerWallet, TOKEN)
                    walletHistory?.walletHistoryRequest()
                    walletHistory?.request?.apply {
                        this.page = walletHistory?.page?.toString()
                        this.limit = walletHistory?.limit?.toString()
                    }
                }
            }
        })
    }
}
