package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.RecyclerBankAccountAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerBankAccountBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.BankAccountModel
import com.ripenapps.greenhouse.fragment.sellerfragemnt.DeleteBankAccount
import com.ripenapps.greenhouse.utills.Companion.Companion.ADD_BANK_ACCOUNT_REQUEST

class SellerBankAccount : BaseActivity(), RecyclerBankAccountAdapter.onClickBank {
    private lateinit var sellerBankAccountBinding: ActivitySellerBankAccountBinding
    private val arrBankAccount: ArrayList<BankAccountModel> = arrayListOf()
    private lateinit var bankAccountAdapter: RecyclerBankAccountAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sellerBankAccountBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_bank_account)

        initListener()

        arrBankAccount.add(BankAccountModel(R.drawable.ic_bank1, "Riyad Bank", "2562 xxxx 2569"))

        bankAccountAdapter = RecyclerBankAccountAdapter(this, arrBankAccount)
        bankAccountAdapter.onClickListner(this)
        sellerBankAccountBinding.recyclerBankAccounts.adapter = bankAccountAdapter

    }

    private fun initListener() {
        sellerBankAccountBinding.btnAddBankAccount.setOnClickListener {
            val toAddBankAcc = Intent(this, AddBankAccount::class.java)
            startActivityForResult(toAddBankAcc, ADD_BANK_ACCOUNT_REQUEST)
        }
        sellerBankAccountBinding.btnBackBankAccount.setOnClickListener {
            finish()
        }
    }

    override fun onDeleteBank(position: Int) {
        val deleteBankAccount = DeleteBankAccount()
        deleteBankAccount.show(supportFragmentManager, deleteBankAccount.tag)
    }
}