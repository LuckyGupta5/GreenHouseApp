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
import com.ripenapps.greenhouse.databinding.FragmentDeleteAddressBinding
import com.ripenapps.greenhouse.model.seller.response.SellerProfileDeleteResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.ProfileDeleteViewModel

class DeleteAddress(
    private val addressId: String?, private val yesClickAction: (String) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var deleteAddressBinding: FragmentDeleteAddressBinding
    private var profileDeleteViewModel: ProfileDeleteViewModel? = null
    private lateinit var mcontext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        deleteAddressBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_delete_address, container, false)

        initViewModel()
        // Inflate the layout for this fragment
        isCancelable = false
        initListener()


        return deleteAddressBinding.root
    }

    private fun initViewModel() {
        profileDeleteViewModel = ViewModelProvider(this)[ProfileDeleteViewModel::class.java]
        deleteAddressBinding.lifecycleOwner = this
        profileDeleteViewModel?.getProfileDeleteData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerProfileDeleteResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()
                                yesClickAction.invoke(addressId.toString())
                                dismiss()
                            }

                            403,401 -> {
                                val signin = Intent(mcontext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(mcontext, data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(mcontext)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
//                        cancelProgressDialog()
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
//                cancelProgressDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        deleteAddressBinding.btnCancel.setOnClickListener {
            dismiss()
        }
        deleteAddressBinding.btnDelete.setOnClickListener {
            profileDeleteViewModel?.request?.addressId = addressId
            profileDeleteViewModel?.request?.token =
                Preferences.getStringPreference(mcontext, TOKEN)
            profileDeleteViewModel?.getProfileDeleteRequest()
        }
    }

}