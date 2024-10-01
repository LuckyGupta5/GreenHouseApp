package com.ripenapps.greenhouse.screen

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.OnBoardingViewPagerAdapter
import com.ripenapps.greenhouse.databinding.ActivityOnBoardingBinding
import com.ripenapps.greenhouse.datamodels.ViewPagerItem
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Preferences

class OnBoarding : BaseActivity() {
    private lateinit var onBoardingBinding: ActivityOnBoardingBinding
    private lateinit var viewPagerModel: ArrayList<ViewPagerItem>
    private lateinit var onBoardingAdapter: OnBoardingViewPagerAdapter
    private lateinit var pageChangeListener: ViewPager2.OnPageChangeCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@OnBoarding, R.color.status_bar)
        onBoardingBinding =
            DataBindingUtil.setContentView(this@OnBoarding, R.layout.activity_on_boarding)

        if (Preferences.getStringPreference(this@OnBoarding, "language").equals("ar")) {
            onBoardingBinding.btnForward.rotation = 180f
        }

        viewPagerModel = arrayListOf(
            ViewPagerItem(
                R.mipmap.walkthroughscreen,
                getString(R.string.auction_title),
                getString(R.string.auction_desc)
            ),
            ViewPagerItem(
                R.drawable.mobile_image2,
                getString(R.string.onBoard2_title2),
                getString(R.string.onBoard2_desc2)
            ),
            ViewPagerItem(
                R.drawable.mobile_image3,
                getString(R.string.onBoard3_title3),
                getString(R.string.onBoard3_desc3)
            ),
        )

        onBoardingAdapter = OnBoardingViewPagerAdapter(viewPagerModel)
        onBoardingBinding.viewPager.adapter = onBoardingAdapter //onBoardScreenAdapter
        onBoardingBinding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        initOnClickListner() // calling OnClickListener
        intiOnPageChange()  // calling OnPageChangeListener
    }

    private fun intiOnPageChange() {
        //changing the view images on scrolling the pages on OnBoardingScreens
        pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        onBoardingBinding.view1.setImageResource(R.drawable.view_selected)
                        onBoardingBinding.view2.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.view3.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.background.setBackgroundResource(R.drawable.bg_onboardscreen)

                    }

                    1 -> {
                        onBoardingBinding.view2.setImageResource(R.drawable.view_selected)
                        onBoardingBinding.view1.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.view3.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.background.setBackgroundResource(R.drawable.onboard_screen2)
                    }

                    2 -> {
                        onBoardingBinding.view3.setImageResource(R.drawable.view_selected)
                        onBoardingBinding.view2.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.view1.setImageResource(R.drawable.view_unselected)
                        onBoardingBinding.background.setBackgroundResource(R.drawable.onboard_screen3)
                    }
                }
            }
        }
        onBoardingBinding.viewPager.registerOnPageChangeCallback(pageChangeListener)
    }

    private fun initOnClickListner() {
        onBoardingBinding.btnForward.setOnClickListener {
            if (onBoardingBinding.viewPager.currentItem == 2) {
                Preferences.setBooleanPreference(this@OnBoarding, "signin", true)
                val toSignIn = Intent(this, SignIn::class.java)
                startActivity(toSignIn)
                finish()
            } else {
                onBoardingBinding.viewPager.currentItem += 1
            }
        }
    }
}