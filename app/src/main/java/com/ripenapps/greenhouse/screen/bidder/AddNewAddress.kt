package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityAddNewAddressBinding
import com.ripenapps.greenhouse.utills.Companion.Companion.EXTRA_NAME
import com.ripenapps.greenhouse.utills.Companion.Companion.EXTRA_USERNUMBER

class AddNewAddress : BaseActivity() {
    private lateinit var addNewAddressBinding: ActivityAddNewAddressBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addNewAddressBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_new_address)

        initListener()
    }

    private fun initListener() {
        addNewAddressBinding.btnSaveNewAddress.setOnClickListener {
            saveAddress()
        }
        addNewAddressBinding.btnBackAddnew.setOnClickListener {
            finish()
        }
    }
    private fun saveAddress() {
        val name = addNewAddressBinding.newFullName.text.toString()
        val number = addNewAddressBinding.newMobileNumber.text.toString()

        if (name.trim().isEmpty() || number.trim().isEmpty()) {
            Toast.makeText(
                this@AddNewAddress, getString(R.string.add_new_address), Toast.LENGTH_SHORT
            ).show()
        }
        val addressData = Intent()
        addressData.putExtra(EXTRA_NAME, name)
        addressData.putExtra(EXTRA_USERNUMBER, number)
        setResult(RESULT_OK, addressData)
        finish()
    }
}