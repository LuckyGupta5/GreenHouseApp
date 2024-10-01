@file:Suppress("DEPRECATION")

package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerImageSliderAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerSoldProductDetailBinding
import com.ripenapps.greenhouse.model.seller.response.SellerSoldProductResponseModel
import com.ripenapps.greenhouse.utills.Constants.ORDER_ID
import com.ripenapps.greenhouse.utills.Preferences

class SellerSoldProductDetail : BaseActivity() {
    private lateinit var binding: ActivitySellerSoldProductDetailBinding
    private var soldProductResponse: SellerSoldProductResponseModel.Data.Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@SellerSoldProductDetail, R.layout.activity_seller_sold_product_detail
        )
        if (Preferences.getStringPreference(this@SellerSoldProductDetail, "language") == "ar") {
            binding.backbtn.rotation = 180f
        }

        soldProductResponse = Gson().fromJson(
            intent.getStringExtra("soldProductData"),
            SellerSoldProductResponseModel.Data.Order::class.java
        )
        initListeners()
        setDetailData()
    }

    @SuppressLint("SetTextI18n")
    private fun setDetailData() {
        binding.apply {
            productName.text = soldProductResponse?.productDetails?.subCategory?.enName
            location.text = soldProductResponse?.productDetails?.productLocation
            productPrice.text = getString(R.string.sar) + " " + soldProductResponse?.productDetails?.productPrice
            description.text = soldProductResponse?.productDetails?.description
            val quantity = soldProductResponse?.productDetails?.quantity ?: ""
            val unit = soldProductResponse?.productDetails?.unit?.lowercase() ?: ""
            productQuantity.text = "$quantity $unit"
            val recyclerAdapter = RecyclerImageSliderAdapter(
                this@SellerSoldProductDetail,
                soldProductResponse?.productDetails?.imageUrl as MutableList<String>
            )
            binding.productImage.adapter =
                recyclerAdapter
            val helper: SnapHelper = LinearSnapHelper()
            helper.attachToRecyclerView(productImage)
        }
    }

    private fun initListeners() {
        binding.apply {
            viewOrderDetail.setOnClickListener {
                val intent =
                    Intent(this@SellerSoldProductDetail, SellerOrderDetailsSelfPickup::class.java)
                intent.putExtra(ORDER_ID, soldProductResponse?._id)
                startActivity(intent)
            }
            backbtn.setOnClickListener {
                finish()
            }
        }
    }
}