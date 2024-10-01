package com.ripenapps.greenhouse.fragment.bidderfragment

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
import com.ripenapps.greenhouse.databinding.FragmentLogoutBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.register_user.response.LogoutResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.LogoutViewModel

class Logout : BottomSheetDialogFragment() {

    private lateinit var logoutBinding: FragmentLogoutBinding
    private var logoutViewModel: LogoutViewModel? = null
    private lateinit var loginData: LoginResponseModel.LoginData
    var accountBlocked: AccountBlocked? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        logoutBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_logout, container, false)

        loginData = Gson().fromJson(
            Preferences.getStringPreference(mContext, "user_details"),
            LoginResponseModel.LoginData::class.java
        )
        initListeners()
        initViewModel()

        return logoutBinding.root
    }

    private fun initViewModel() {
        logoutViewModel = ViewModelProvider(this)[LogoutViewModel::class.java]
        logoutBinding.lifecycleOwner = this

        logoutViewModel?.getLogoutData()?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    ProcessDialog.dismissDialog()
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        LogoutResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            Preferences.setBooleanPreference(mContext, "signin", true)
                            Preferences.setBooleanPreference(mContext, "isBidder", false)
                            Preferences.setBooleanPreference(mContext, "isSeller", false)
                            Preferences.removePreference(mContext, "token")
                            Preferences.removePreference(mContext, "user_details")
                            Preferences.removePreference(mContext, "isVerified")
                            val toSignIn = Intent(mContext, SignIn::class.java)
                            startActivity(toSignIn)
                            dismiss()
                            activity?.finishAffinity()
                        }

                        403, 401 -> {
                            Preferences.removePreference(mContext, "token")
                            Preferences.removePreference(mContext, "user_details")
                            Preferences.removePreference(mContext, "isVerified")
                            Preferences.removePreference(mContext, "roomId")
                            val signin = Intent(mContext, SignIn::class.java)
                            startActivity(signin)
                            dismiss()
                            activity?.finishAffinity()
                        }


                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(parentFragmentManager, accountBlocked?.tag)
                            }
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
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {
        logoutBinding.cancleLogoutButton.setOnClickListener {
            dismiss()
        }
        logoutBinding.confirmLogoutButton.setOnClickListener {
            logoutViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
            logoutViewModel?.request?.userId = loginData.userDetails?.id
            logoutViewModel?.getLogoutRequest()
        }
    }
}