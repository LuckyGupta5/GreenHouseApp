package com.ripenapps.greenhouse.screen.seller

import android.os.Bundle
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityBackgroundVerficationAdminBinding
import com.ripenapps.greenhouse.fragment.SwitchVerification
import com.ripenapps.greenhouse.fragment.sellerfragemnt.VerificationToAdminSeller

class BackgroundVerficationAdmin() : BaseActivity() {
    private lateinit var binding: ActivityBackgroundVerficationAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_verfication_admin)

        val userType = intent?.getStringExtra("type") ?: ""
        if (userType.isNotEmpty() && userType == "seller") {
            val switchVerification = SwitchVerification(false)
            switchVerification.show(supportFragmentManager, switchVerification.tag)
        } else {
            val verificationToAdmin = VerificationToAdminSeller()
            verificationToAdmin.show(supportFragmentManager, verificationToAdmin.tag)
        }
    }
}