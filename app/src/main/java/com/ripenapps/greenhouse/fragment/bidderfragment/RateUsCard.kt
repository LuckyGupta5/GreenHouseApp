package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentRateUsCardBinding
import com.ripenapps.greenhouse.model.bidder.response.AddWishListResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.AddReviewViewModel

class RateUsCard(
    private val sellerId: String,
    private val orderId: String,
    private val applyCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentRateUsCardBinding
    private var addReviewViewModel: AddReviewViewModel? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_rate_us_card, container, false)
        // Inflate the layout for this fragment
        initListeners()
        initViewModel()
        return binding.root
    }

    private fun initViewModel() {
        addReviewViewModel = ViewModelProvider(this@RateUsCard)[AddReviewViewModel::class.java]
        binding.lifecycleOwner = this@RateUsCard
        addReviewViewModel?.addReviewData()?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        AddWishListResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            Log.d("TAG", "RateUsResponse: done" + Gson().toJson(data))
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            binding.submitButton.isEnabled = true
                            dismiss()
                            applyCallBack.invoke()
                        }

                        403, 401 -> {
                            val signin = Intent(mContext, SignIn::class.java)
                            startActivity(signin)
                            dismiss()
                            activity?.finishAffinity()
                        }

                        else -> {
                            binding.submitButton.isEnabled = true
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    binding.submitButton.isEnabled = true
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            submitButton.setOnClickListener {
                submitButton.isEnabled = false
                ratingApi()
            }
        }
    }

    private fun ratingApi() {
        addReviewViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
        addReviewViewModel?.request?.review = binding.review.text.toString().trim()
        addReviewViewModel?.request?.rating = binding.ratingBar.rating.toString()
        addReviewViewModel?.request?.sellerId = sellerId
        addReviewViewModel?.request?.orderId = orderId
        addReviewViewModel?.addReviewRequest()
    }
}