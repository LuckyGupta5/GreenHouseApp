package com.ripenapps.greenhouse.screen.seller

import android.os.Bundle
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.fragment.SwitchRejected3Times
import com.ripenapps.greenhouse.fragment.sellerfragemnt.RejectedBottomSheet

class Rejected3Times : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rejected3_times)
        val userType = intent?.getStringExtra("type") ?: ""
        if(userType.isNotEmpty() && userType == "seller") {
            val toreject3Times=SwitchRejected3Times()
            toreject3Times.show(supportFragmentManager,toreject3Times.tag)
        }
        else{
            val toRejected3Times = RejectedBottomSheet()
            toRejected3Times.show(supportFragmentManager, toRejected3Times.tag)
        }


    }
}