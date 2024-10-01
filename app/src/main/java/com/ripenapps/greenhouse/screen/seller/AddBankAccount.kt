package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityAddBankAccountBinding
import com.ripenapps.greenhouse.fragment.sellerfragemnt.BankList
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SelectCountryBottomSheet
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Preferences

class AddBankAccount : BaseActivity() {
    private lateinit var addBankAccountBinding: ActivityAddBankAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@AddBankAccount, R.color.status_bar)
        addBankAccountBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_bank_account)
        if (Preferences.getStringPreference(this@AddBankAccount, "language").equals("ar")) {
            addBankAccountBinding.btnBack.rotation = 180f
        }
        initListeners()
    }

    private fun initListeners() {
        addBankAccountBinding.btnBack.setOnClickListener {
            finish()
        }
        addBankAccountBinding.btnAddBankAccount.setOnClickListener {
            val intent = Intent(this, AddBankAccountSeller::class.java)
            startActivity(intent)

        }
        addBankAccountBinding.bankList.setOnClickListener {
            val bankList = BankList()
            bankList.show(supportFragmentManager, bankList.tag)
        }
        addBankAccountBinding.btnAddBankAccount.setOnClickListener {
            val sellerbank = Intent(this, SellerBankAccount::class.java)
            startActivity(sellerbank)
        }
        addBankAccountBinding.countryList.setOnClickListener {
            val selectCountry = SelectCountryBottomSheet()
            selectCountry.show(supportFragmentManager, selectCountry.tag)
        }
    }
}