package com.ripenapps.greenhouse.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.BackgroundVerficationAdmin
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Preferences

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        changeStatusBarColor(this@SplashActivity, R.color.greenhousetheme)
//        throw RuntimeException("Test Crash")

        Handler().postDelayed({
            if (Preferences.getBooleanPreference(this@SplashActivity, "isVerified")) {
                val i = Intent(this@SplashActivity, BackgroundVerficationAdmin::class.java)
                startActivity(i)
                finish()
            } else if (Preferences.getBooleanPreference(this@SplashActivity, "signin")) {
                val toSignIn = Intent(this, SignIn::class.java)
                startActivity(toSignIn)
                finish()
            } else if (Preferences.getBooleanPreference(this@SplashActivity, "isBidder")) {
                val i = Intent(this@SplashActivity, Home::class.java)
                startActivity(i)
                finish()
            } else if (Preferences.getBooleanPreference(this@SplashActivity, "isSeller")) {
                val i = Intent(this@SplashActivity, HomeSeller::class.java)
                startActivity(i)
                finish()

            } else {
                val i = Intent(this, SelectLanguage::class.java)
                startActivity(i)
                finish()
            }
        }, 3000)
    }
}
