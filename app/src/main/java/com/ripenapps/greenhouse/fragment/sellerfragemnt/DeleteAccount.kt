package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentDeleteAccountBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.commonModels.delete_account.AccountDeleteResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.AccountDeleteViewModel

class DeleteAccount : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentDeleteAccountBinding
    private var deleteAccountViewModel: AccountDeleteViewModel? = null
    private lateinit var mContext: Context
    var accountBlocked: AccountBlocked? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_delete_account, container, false)

        accountDeleteViewModel()
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.apply {
            cancleButton.setOnClickListener { dismiss() }
            confirmButton.setOnClickListener {
                deleteAccountViewModel?.request?.token =
                    Preferences.getStringPreference(mContext, TOKEN)
                deleteAccountViewModel?.accountDeleteRequest()
            }
        }
    }

    private fun accountDeleteViewModel() {
        deleteAccountViewModel =
            ViewModelProvider(this@DeleteAccount)[AccountDeleteViewModel::class.java]
        binding.lifecycleOwner = this@DeleteAccount
        deleteAccountViewModel?.accountDeleteData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            AccountDeleteResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                                val toSignIn = Intent(mContext, SignIn::class.java)
                                startActivity(toSignIn)
                                dismiss()
                                Preferences.removeAllPreference(mContext)
                                activity?.finishAffinity()
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(mContext)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }
}