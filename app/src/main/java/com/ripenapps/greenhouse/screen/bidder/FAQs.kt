package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFaqQuesAdapter
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFaqQuestionAdapter
import com.ripenapps.greenhouse.databinding.ActivityFaqsBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FaqQuestionModel
import com.ripenapps.greenhouse.datamodels.biddermodel.FaqQuestionTypeModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.commonModels.faq.FaqResponse
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.FaqViewModel

class FAQs : BaseActivity() {
    private lateinit var faqsBinding: ActivityFaqsBinding
    private var recyclerFaqTypeAdapter: RecyclerFaqQuesAdapter? = null
    private var arrQuestionType: ArrayList<FaqQuestionTypeModel>? = arrayListOf()
    private var recyclerAdapter: RecyclerFaqQuestionAdapter? = null
    private var arrQuestion: ArrayList<FaqQuestionModel>? = arrayListOf()
    private var faqViewModel: FaqViewModel? = null
    var accountBlocked: AccountBlocked? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@FAQs, R.color.status_bar)
        super.onCreate(savedInstanceState)
        faqsBinding = DataBindingUtil.setContentView(this, R.layout.activity_faqs)

        arrQuestionType?.add(
            FaqQuestionTypeModel(
                R.drawable.ic_faq_bell, getString(R.string.get_started), R.color.get_started_color
            )
        )
        arrQuestionType?.add(
            FaqQuestionTypeModel(
                R.drawable.ic_bid, getString(R.string.how_to_bid), R.color.how_to_bid_color
            )
        )
        arrQuestionType?.add(
            FaqQuestionTypeModel(
                R.drawable.ic_dollar,
                getString(R.string.payment_method),
                R.color.payment_method_color
            )
        )

        recyclerFaqTypeAdapter = arrQuestionType?.let { RecyclerFaqQuesAdapter(it) }
        faqsBinding.recyclerFaqTypes.adapter = recyclerFaqTypeAdapter
        initListener()
        initViewModel()
        faqApi()
    }

    private fun faqApi() {
        faqViewModel?.request?.token = Preferences.getStringPreference(this@FAQs, TOKEN)
        if (intent.getStringExtra("fromBidder") == "1") {
            faqViewModel?.request?.userType = "bidder"
        } else {
            faqViewModel?.request?.userType = "seller"
        }
        faqViewModel?.getFaqListRequest()
    }

    private fun initViewModel() {
        faqViewModel = ViewModelProvider(this@FAQs)[FaqViewModel::class.java]
        faqsBinding.lifecycleOwner = this
        faqViewModel?.getFaqListData()?.observe(this@FAQs) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        faqsBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(false)
                        faqsBinding.faqQuestionRecycler.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            FaqResponse::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG", "FaqResponse: ${Gson().toJson(data.data)}"
                                )
                                arrQuestion?.clear()
                                data.data?.data?.forEachIndexed { _, question ->
                                    arrQuestion?.add(
                                        FaqQuestionModel(
                                            question = question?.title,
                                            answer = question?.description
                                        )
                                    )
                                }
                                recyclerAdapter = arrQuestion?.let { it1 ->
                                    RecyclerFaqQuestionAdapter("", it1, {})
                                }
                                faqsBinding.faqQuestionRecycler.adapter = recyclerAdapter
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
                                val signin = Intent(this@FAQs, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(this@FAQs, data.message, Toast.LENGTH_SHORT).show()
                                faqsBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(
                                    false
                                )
                                faqsBinding.faqQuestionRecycler.setVisibility(true)
                            }
                        }
                    }

                    Status.LOADING -> {
                        faqsBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                        faqsBinding.faqQuestionRecycler.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        Toast.makeText(this@FAQs, it.message, Toast.LENGTH_SHORT).show()
                        faqsBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                        faqsBinding.faqQuestionRecycler.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                showToast(it.message.toString())
                faqsBinding.myProfileShimmer.myProfileMainShimmer.setVisibility(true)
                faqsBinding.faqQuestionRecycler.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        faqsBinding.apply {
            faqsBinding.closeFaq.setOnClickListener {
                finish()
            }
        }
    }
}