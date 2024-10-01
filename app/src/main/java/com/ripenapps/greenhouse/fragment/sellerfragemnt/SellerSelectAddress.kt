package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.RecyclerSelectAddress
import com.ripenapps.greenhouse.databinding.FragmentSellerSelectAddressBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMyAddressModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.address.response.AddressListResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.seller.EditAddress
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddressListViewModel

class SellerSelectAddress() : BottomSheetDialogFragment() {
    lateinit var callBack: (SellerMyAddressModel) -> (Unit)
    private lateinit var mContext:Context
    companion object {
        fun newInstance(selectedAddressCallback: (SellerMyAddressModel) -> (Unit)): SellerSelectAddress {
            return SellerSelectAddress().apply {
                callBack = selectedAddressCallback
            }
        }
    }

    private lateinit var binding: FragmentSellerSelectAddressBinding
    private lateinit var addressListViewModel: AddressListViewModel
    private val arrAddress: ArrayList<SellerMyAddressModel> = arrayListOf()
    private var recyclerAddressAdapter: RecyclerSelectAddress? = null
    var accountBlocked: AccountBlocked? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_select_address, container, false
        )
        initAdapter()
        initViewModel()
        binding.addAddress.setOnClickListener {
            val addAddress = Intent(mContext, EditAddress::class.java)
            startActivityForResult(addAddress, 1001)
            startActivity(addAddress)
        }
        addressListViewModel.request.userType = "seller"
        addressListViewModel.request.token =
            Preferences.getStringPreference(mContext, Constants.TOKEN)
        addressListViewModel.getAddressListRequest()
        return binding.root

    }

    private fun initViewModel() {
        addressListViewModel = ViewModelProvider(this)[AddressListViewModel::class.java]
        binding.lifecycleOwner = this
        addressListViewModel.getAddressListData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY,
                                it.data
                            ),
                            AddressListResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {

                                arrAddress.clear()
                                ProcessDialog.dismissDialog()
                                if (data.data?.addresses?.isEmpty() == true) {
                                    binding.apply {
                                        recycler.setVisibility(false)
                                        placeHolder.setVisibility(true)
                                        headingPlaceHolder.setVisibility(true)
                                        addAddress.setVisibility(true)
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        recycler.setVisibility(true)
                                        placeHolder.setVisibility(false)
                                        headingPlaceHolder.setVisibility(false)
                                        addAddress.setVisibility(true)
                                    }
                                }

                                data.data?.addresses?.forEachIndexed { _, addresse ->
                                    arrAddress.add(
                                        SellerMyAddressModel(
                                            addressId = addresse.id ?: "",
                                            userAddress = addresse.address.orEmpty().trim(),
                                            long = addresse.location?.coordinates?.getOrNull(0),
                                            lat = addresse.location?.coordinates?.getOrNull(1)
                                        )
                                    )
                                }
                                initAdapter()
                            }

                            403, 401 -> {
                                Preferences.removePreference(mContext, "token")
                                Preferences.removePreference(mContext, "user_details")
                                Preferences.removePreference(mContext, "isVerified")
                                Preferences.removePreference(mContext, "roomId")
                                val signin = Intent(mContext, SignIn::class.java)
                                startActivity(signin)
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


                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(
                                    mContext, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }

                    Status.LOADING -> {

                    }

                    Status.ERROR -> {
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }

    }

    private fun initAdapter() {
        recyclerAddressAdapter = RecyclerSelectAddress {
            Log.d("TAG", "initAdapter: ${Gson().toJson(it)}")
            callBack.invoke(it)
            dismiss()
        }
        recyclerAddressAdapter?.addAddress(arrAddress)
        binding.recycler.adapter = recyclerAddressAdapter
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            val fullAddress = data?.extras?.getString("EXTRA_ADDRESS")
            val fullName = data?.extras?.getString("EXTRA_NAME") as String
            val userNumber = data.extras?.getString("EXTRA_USERNUMBER") as String
            val latitude = (data.extras?.getString("latitude") ?: "0.0").toDouble()
            val longitude = (data.extras?.getString("longitude") ?: "0.0").toDouble()

            arrAddress.add(
                SellerMyAddressModel(
                    userName = fullName,
                    userNumber = userNumber,
                    userAddress = fullAddress,
                    userAddress2 = fullAddress,
                    lat = latitude,
                    long = longitude
                )
            )
            recyclerAddressAdapter?.addAddress(arrAddress)
            if (arrAddress.isNotEmpty()) {
                binding.recycler.setVisibility(true)
            } else {
                binding.recycler.setVisibility(false)

            }
        }
    }
}