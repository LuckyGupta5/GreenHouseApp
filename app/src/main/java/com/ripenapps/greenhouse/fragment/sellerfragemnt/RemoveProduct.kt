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
import com.ripenapps.greenhouse.databinding.FragmentRemoveProductBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerProductDeleteResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.view_models.SellerProductDeleteViewModel

class RemoveProduct(
    private val productId: String,
    private val successCallBack: () -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var removeProductBinding: FragmentRemoveProductBinding
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
        removeProductBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_remove_product, container, false)
        initListeners()

        initViewModel()
        return removeProductBinding.root
    }

    private fun initViewModel() {
        productDeleteViewModel = ViewModelProvider(this)[SellerProductDeleteViewModel::class.java]
        removeProductBinding.lifecycleOwner = this
        productDeleteViewModel.getProductDeleteData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerProductDeleteResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                successCallBack.invoke()
                                ProcessDialog.dismissDialog()
                                dismiss()
                                activity?.finish()
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
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mcontext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListeners() {
        removeProductBinding.apply {
            cancelButton.setOnClickListener {
                dismiss()
            }
            yesButton.setOnClickListener {
                productDeleteViewModel.request.productId = productId
                productDeleteViewModel.request.token =
                    Preferences.getStringPreference(mcontext, Constants.TOKEN)
                productDeleteViewModel.getProductDeleteRequest()
            }
        }
    }

}