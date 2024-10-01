package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityAddBankAccountSellerBinding
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Preferences

class AddBankAccountSeller : BaseActivity() {
    private lateinit var binding: ActivityAddBankAccountSellerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@AddBankAccountSeller, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_bank_account_seller)

        if (Preferences.getStringPreference(this@AddBankAccountSeller, "language").equals("ar")) {
            binding.btnBack.rotation = 180f
        }
        initListener()
    }

    private fun initListener() {
        binding.apply {
            newAddAccount.setOnClickListener {
                val intent = Intent(this@AddBankAccountSeller, AddBankAccount::class.java)
                startActivity(intent)
            }
            btnBack.setOnClickListener {
                finish()
            }
        }
    }
}