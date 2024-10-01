package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityHomeBinding
import com.ripenapps.greenhouse.fragment.bidderfragment.HomeFragment
import com.ripenapps.greenhouse.fragment.bidderfragment.MyBidsFragment
import com.ripenapps.greenhouse.fragment.bidderfragment.ProductsFragment
import com.ripenapps.greenhouse.fragment.bidderfragment.ProfileFragment
import com.ripenapps.greenhouse.fragment.bidderfragment.SearchFragment
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.showToast

class Home : BaseActivity() {
    private lateinit var homeBinding: ActivityHomeBinding

    // Initializing fragments
    private val homeFragment = HomeFragment()
    private val productFragment = ProductsFragment()
    private val myBidsFragment = MyBidsFragment()
    private val searchFragment = SearchFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@Home, R.color.status_bar) // Change status bar color
        super.onCreate(savedInstanceState)
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        // Setting preferences
        Preferences.setBooleanPreference(this@Home, "isSeller", false)
        Preferences.setBooleanPreference(this@Home, "isBidder", true)
        Preferences.setBooleanPreference(this@Home, "signin", false)

        // Load initial fragment
        loadFragment(HomeFragment(), true)
        homeBinding.bnView.selectedItemId = R.id.nav_home

        // Bottom navigation item selected listener
        homeBinding.bnView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_myBids -> {
                    if (!myBidsFragment.isAdded) {
                        loadFragment(myBidsFragment, true)
                    } else {
                        loadFragment(myBidsFragment, false)
                    }
                    true
                }

                R.id.nav_Search -> {
                    if (!searchFragment.isAdded) {
                        loadFragment(searchFragment, true)
                    } else {
                        loadFragment(searchFragment, false)
                    }
                    true
                }

                R.id.nav_home -> {
                    if (!homeFragment.isAdded) {
                        loadFragment(homeFragment, true)
                    } else {
                        loadFragment(homeFragment, false)
                    }
                    true
                }

                R.id.nav_Products -> {
                    if (!productFragment.isAdded) {
                        loadFragment(productFragment, true)
                    } else {
                        loadFragment(productFragment, false)
                    }
                    true
                }

                R.id.nav_Profile -> {
                    if (!profileFragment.isAdded) {
                        loadFragment(profileFragment, true)
                    } else {
                        loadFragment(profileFragment, false)
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForNotificationData() // Check for any notification data
    }

    // Function to handle notification data
    private fun checkForNotificationData() {
        val type = intent?.getStringExtra("type") ?: ""
        val bidId = intent?.getStringExtra("bidId") ?: ""
        if (type.isNotEmpty()) {
            Log.d("TAG", "checkForNotificationData: $bidId $type")
            // Open detail page if notification data is present
            val toBidDetail = Intent(this@Home, BidderBiddingDetails::class.java)
            toBidDetail.putExtra("bidId", bidId)
            startActivity(toBidDetail)
        }
    }

    // Function to load fragments
    private fun loadFragment(fragment: Fragment, add: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (add) {
            transaction.add(R.id.idContainer, fragment)
        } else {
            transaction.replace(R.id.idContainer, fragment)
        }
        transaction.commit()
    }

    // Function to apply filter and navigate to SearchFragment
    fun applyFilter(status: Int) {
        homeBinding.bnView.selectedItemId = R.id.nav_Search
        searchFragment.callBackFromHome(status)
    }

    private var backPressed = false

    // Function to handle back press with double-tap to exit
    private fun backPress() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressed) {
                        this@Home.finishAffinity()
                    }
                    backPressed = true
                    if (homeFragment.isVisible) {
                        showToast("Press back again to exit")
                    } else {
                        homeBinding.bnView.selectedItemId = R.id.nav_home
                    }
                    Handler(Looper.getMainLooper()).postDelayed({ backPressed = false }, 2000)
                }
            }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}
