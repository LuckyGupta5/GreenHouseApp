package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentEndAuctionBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerProductDeleteResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.SellerOrderDetailsSelfPickup
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.SellerProductDeleteViewModel

class EndAuctionFragment(
    val productId: String?,
    private val successCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentEndAuctionBinding
    private lateinit var productDeleteViewModel: SellerProductDeleteViewModel
    private lateinit var mcontext: Context
    var accountBlocked: AccountBlocked? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_end_auction, container, false)
        initListeners()

        initViewModel()
        return binding.root
    }

    private fun initViewModel() {
        productDeleteViewModel = ViewModelProvider(this)[SellerProductDeleteViewModel::class.java]
        binding.lifecycleOwner = this
        productDeleteViewModel.getProductDeleteData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data), SellerProductDeleteResponseModel::class.java)
                        
                        when (data.status) {
                            200 -> {
                                successCallBack.invoke()
                                ProcessDialog.dismissDialog()
                                dismiss()
                            }
                            422 -> {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
                                }
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

    private fun initListeners() {
        binding.apply {
            cancleLogoutButton.setOnClickListener {
                dismiss()
            }
            confirmLogoutButton.setOnClickListener {
                productDeleteViewModel.request.productId = productId
                productDeleteViewModel.request.token =
                    Preferences.getStringPreference(mcontext, Constants.TOKEN)
                productDeleteViewModel.getProductDeleteRequest()
            }
        }
    }

}