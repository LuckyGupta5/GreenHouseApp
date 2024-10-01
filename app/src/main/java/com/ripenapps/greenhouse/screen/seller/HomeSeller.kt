package com.ripenapps.greenhouse.screen.seller

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityHomeSellerBinding
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerHomeFragment
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerMyBidsFragment
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerMyOrders
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerProductFragment
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerProfileFragment
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Preferences

class HomeSeller : BaseActivity() {
    private lateinit var homeSellerBinding: ActivityHomeSellerBinding
    private val sellerMyBidsFragment = SellerMyBidsFragment()
    private val sellerMyOrders = SellerMyOrders()
    private val sellerHome = SellerHomeFragment()
    private val sellerProduct = SellerProductFragment()
    private val sellerProfile = SellerProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@HomeSeller, R.color.status_bar)
        super.onCreate(savedInstanceState)
        homeSellerBinding = DataBindingUtil.setContentView(this, R.layout.activity_home_seller)

        Preferences.setBooleanPreference(this@HomeSeller, "isBidder", false)
        Preferences.setBooleanPreference(
            this@HomeSeller, "isVerified", false
        )
        Preferences.setBooleanPreference(this@HomeSeller, "isSeller", true)
        Preferences.setBooleanPreference(this@HomeSeller, "signin", false)

        loadFragment(SellerHomeFragment(), true)
        homeSellerBinding.bottonNavView.selectedItemId = R.id.nav_seller_home
        homeSellerBinding.bottonNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_seller_myBids -> {
                    //Toast.makeText(this@HomeSeller, "Under Development - M4", Toast.LENGTH_SHORT).show()
                    if (!sellerMyBidsFragment.isAdded) {
                        loadFragment(sellerMyBidsFragment, true)
                    } else {
                        loadFragment(sellerMyBidsFragment, false)
                    }
                    true
                }

                R.id.nav_seller_myOrders -> {
                    if (!sellerMyOrders.isAdded) {
                        loadFragment(sellerMyOrders, true)
                    } else {
                        loadFragment(sellerMyOrders, false)
                    }
                    true
                }

                R.id.nav_seller_home -> {
                    if (!sellerHome.isAdded) {
                        loadFragment(sellerHome, true)
                    } else {
                        loadFragment(sellerHome, false)
                    }
                    true
                }

                R.id.nav_seller_products -> {
                    if (!sellerProduct.isAdded) {
                        loadFragment(sellerProduct, true)
                    } else {
                        loadFragment(sellerProduct, false)
                    }
                    true
                }

                R.id.nav_seller_profile -> {
                    if (!sellerProfile.isAdded) {
                        loadFragment(sellerProfile, true)
                    } else {
                        loadFragment(sellerProfile, false)
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment, b: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (b) {
            transaction.add(R.id.idContainer, fragment)
        } else {
            transaction.replace(R.id.idContainer, fragment)
        }
        transaction.commit()
    }
}
