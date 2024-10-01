package com.ripenapps.greenhouse.screen.seller

import android.os.Bundle
import android.util.Log
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.fragment.SwitchRejected
import com.ripenapps.greenhouse.fragment.sellerfragemnt.VerificationRejected

class VerificationRejectedByAdmin : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_rejected_by_admin)

//        reason = intent.getStringExtra("REJECTED_REASON")

        val userType = intent?.getStringExtra("type") ?: ""
        if (userType.isNotEmpty() && userType == "bidder") {
            val rejectedReason = SwitchRejected(intent.getStringExtra("reason"), "")
            rejectedReason.show(supportFragmentManager, rejectedReason.tag)
        } else {
            val rejected = VerificationRejected(
                intent.getStringExtra("NUMBER"), intent.getStringExtra("REJECTED_REASON")
            )
            Log.e("TAG", "onCreate: " + intent.getStringExtra("NUMBER"))
            rejected.show(supportFragmentManager, rejected.tag)
        }
    }
}