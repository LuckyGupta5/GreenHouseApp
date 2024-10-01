@file:Suppress("DEPRECATION")

package com.ripenapps.greenhouse.screen.seller

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.seller.SellerMyNewAddressprofileAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerMyAddressBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMyAddressModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.DeleteAddress
import com.ripenapps.greenhouse.model.address.response.AddressListResponseModel
import com.ripenapps.greenhouse.model.product_details.Location
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.BidderEditAddress
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Companion.Companion.ADD_ADDRESS_REQUEST
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.isValidList
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.AddressListViewModel

class SellerMyAddress : BaseActivity(), SellerMyNewAddressprofileAdapter.onClickListner {
    private lateinit var binding: ActivitySellerMyAddressBinding
    private var recyclerAddressAdapter: SellerMyNewAddressprofileAdapter? = null
    private lateinit var addressListViewModel: AddressListViewModel
    var accountBlocked: AccountBlocked? = null
    private var isPaging: Boolean = false
    private var deleteAddress: DeleteAddress? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@SellerMyAddress, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_my_address)
        if (Preferences.getStringPreference(this@SellerMyAddress, "language") == "ar") {
            binding.btnBackAddresses.rotation = 180f
        }
        initListener()
    }

    override fun onStart() {
        super.onStart()
        recyclerAddressAdapter?.clearData()
        addressListViewModel()
        if (intent.hasExtra("fromBidder")) {
            addressListViewModel.request.userType = "bidder"
            addressListViewModel.request.token =
                Preferences.getStringPreference(this@SellerMyAddress, TOKEN)
            addressListViewModel.getAddressListRequest()
        } else {
            addressListViewModel.request.userType = "seller"
            addressListViewModel.request.token =
                Preferences.getStringPreference(this@SellerMyAddress, TOKEN)
            addressListViewModel.getAddressListRequest()
        }
    }

    private fun addressListViewModel() {
        addressListViewModel = ViewModelProvider(this)[AddressListViewModel::class.java]
        binding.lifecycleOwner = this
        addressListViewModel.getAddressListData().observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.addressShimmer.addressMainShimmer.setVisibility(false)
                        binding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            AddressListResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                binding.idRefreshLayout.isRefreshing = false
                                addressListViewModel.isLastPage = (data.data?.addresses?.size
                                    ?: 0) < (addressListViewModel.limit.toInt())
                                addressListViewModel.isDataLoading = false
                                if (data.data?.addresses?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        recyclerSellerMyAddresses.visibility = View.GONE
                                        placeHolder.visibility = View.VISIBLE
                                        headingPlaceHolder.visibility = View.VISIBLE
                                        placeHolderDescription.visibility = View.VISIBLE
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        recyclerSellerMyAddresses.visibility = View.VISIBLE
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                    }
                                }
                                val arrAddress: ArrayList<SellerMyAddressModel> = arrayListOf()
                                data.data?.addresses?.forEachIndexed { _, address ->
                                    arrAddress.add(
                                        SellerMyAddressModel(
                                            addressId = address.id ?: "",
                                            userName = address.name,
                                            userNumber = address.mobile,
                                            userAddress = address.address.orEmpty().trim(),
                                            userAddress2 = address.city + "," + address.country + "-" + address.zipcode,
                                            location = address.location
                                        )
                                    )
                                }

                                if (recyclerAddressAdapter == null || recyclerAddressAdapter?.arrAddress?.isEmpty() == true || addressListViewModel.page == 1) {
                                    recyclerAddressAdapter = SellerMyNewAddressprofileAdapter()
                                    recyclerAddressAdapter?.onClickListner(this@SellerMyAddress)
                                    binding.recyclerSellerMyAddresses.adapter =
                                        recyclerAddressAdapter
                                    recyclerAddressAdapter?.addItems(arrAddress)
                                } else {
                                    recyclerAddressAdapter?.updateItemList(arrAddress)
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@SellerMyAddress, "token")
                                Preferences.removePreference(this@SellerMyAddress, "user_details")
                                Preferences.removePreference(this@SellerMyAddress, "isVerified")
                                Preferences.removePreference(this@SellerMyAddress, "roomId")
                                val signin = Intent(this@SellerMyAddress, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(
                                    this@SellerMyAddress, data.message, Toast.LENGTH_SHORT
                                ).show()
                                binding.addressShimmer.addressMainShimmer.setVisibility(false)
                                binding.idRefreshLayout.setVisibility(true)
                            }

                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging && !binding.idRefreshLayout.isRefreshing) {
                            binding.addressShimmer.addressMainShimmer.setVisibility(true)
                            binding.idRefreshLayout.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.placeHolderDescription.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        binding.addressShimmer.addressMainShimmer.setVisibility(true)
                        binding.idRefreshLayout.setVisibility(false)
                        binding.placeHolder.setVisibility(false)
                        binding.placeHolderDescription.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@SellerMyAddress, it.message, Toast.LENGTH_SHORT).show()
                        binding.idRefreshLayout.isRefreshing = false
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {
        binding.btnBackAddresses.setOnClickListener { finish() }
        binding.btnAddNewAddress.setOnClickListener {
            if (intent.hasExtra("fromBidder")) {

                val toBidderEditAddress =
                    Intent(this@SellerMyAddress, BidderEditAddress::class.java)
                startActivityForResult(toBidderEditAddress, ADD_ADDRESS_REQUEST)

            } else {
                val intent = Intent(this, EditAddress::class.java)
                startActivityForResult(intent, ADD_ADDRESS_REQUEST)
            }
        }
        binding.idRefreshLayout.setOnRefreshListener {
            addressListViewModel.page = 1
            addressListViewModel.request.page = addressListViewModel.page.toString()
            addressListViewModel.limit = addressListViewModel.limit
            addressListViewModel.request.token =
                Preferences.getStringPreference(this@SellerMyAddress, TOKEN)
            addressListViewModel.getAddressListRequest()

        }
    }

    override fun onEdit(position: Int) {
        if (intent.hasExtra("fromBidder")) {
            val toBidderEditAddress = Intent(this@SellerMyAddress, BidderEditAddress::class.java)
            toBidderEditAddress.putExtra(
                "addressId", recyclerAddressAdapter?.arrAddress?.getOrNull(position)?.addressId
            )
            toBidderEditAddress.putExtra("fromEditAddress", 1)
            toBidderEditAddress.putExtra("fromEdit", "1")
            toBidderEditAddress.putExtra("dataModel", Gson().toJson(recyclerAddressAdapter?.arrAddress?.getOrNull(position)))
            startActivityForResult(toBidderEditAddress, ADD_ADDRESS_REQUEST)
        } else {
            val intent = Intent(this, EditAddress::class.java)
            Log.d("TAG", "onEdit: ${Gson().toJson(recyclerAddressAdapter?.arrAddress?.getOrNull(position))}")
            intent.putExtra("addressId", recyclerAddressAdapter?.arrAddress?.getOrNull(position)?.addressId)
            intent.putExtra("fromEditAddress", 1)
            intent.putExtra("fromEdit", "1")
            intent.putExtra("dataModel", Gson().toJson(recyclerAddressAdapter?.arrAddress?.getOrNull(position)))
            startActivityForResult(intent, ADD_ADDRESS_REQUEST)
        }
    }

    override fun onDelete(position: Int) {
        if (deleteAddress?.isAdded != true) {
            deleteAddress =
                DeleteAddress(recyclerAddressAdapter?.arrAddress?.getOrNull(position)?.addressId) {
                    recyclerAddressAdapter?.apply {
                        if (arrAddress.isValidList(position)) {
                            arrAddress?.removeAt(position)
                            notifyItemRemoved(position)
                            if (arrAddress?.isEmpty() == true) {
                                binding.apply {
                                    recyclerSellerMyAddresses.visibility = View.GONE
                                    placeHolder.visibility = View.VISIBLE
                                    headingPlaceHolder.visibility = View.VISIBLE
                                    placeHolderDescription.visibility = View.VISIBLE
                                }
                            } else {
                                binding.apply {
                                    recyclerSellerMyAddresses.visibility = View.VISIBLE
                                    placeHolder.visibility = View.GONE
                                    headingPlaceHolder.visibility = View.GONE
                                    placeHolderDescription.visibility = View.GONE
                                }
                            }

                        } else {
                            Toast.makeText(
                                this@SellerMyAddress,
                                "Something went wrong,Please try again later!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            deleteAddress?.show(supportFragmentManager, deleteAddress?.tag)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ADDRESS_REQUEST && resultCode == RESULT_OK) {
            val fullAddress = data?.extras?.getString("EXTRA_ADDRESS") ?: "RipenApps Sector 63"
            val fullName = data?.extras?.getString("EXTRA_NAME") as String
            val userNumber = data.extras?.getString("EXTRA_USERNUMBER") as String
            val addressId = data.extras?.getString("addressId")
            val latitude = data.extras?.getString("latitude")
            val longitude = data.extras?.getString("longitude")
            val isAddressUpdated = data.extras?.getString("isAddressUpdated")

            if (recyclerAddressAdapter == null) {
                initAdapter()
            }
            if (isAddressUpdated?.isNotEmpty() == true) {
                recyclerAddressAdapter?.apply {
                    val position = this.arrAddress?.indexOfFirst { it.addressId == addressId } ?: -1
                    if (position != -1) {
                        val addressModel = arrAddress?.getOrNull(position)
                        addressModel?.userAddress = fullAddress
                        addressModel?.userName = fullName
                        addressModel?.userNumber = userNumber
                        addressModel?.userAddress2 = fullAddress
                        notifyItemChanged(position)
                    }
                }
            } else {

                recyclerAddressAdapter?.arrAddress?.add(
                    0,
                    SellerMyAddressModel(
                        addressId = addressId,
                        userName = fullName,
                        userNumber = userNumber,
                        userAddress = fullAddress,
                        userAddress2 = fullAddress,
                        location = Location(
                            coordinates = listOf(
                                (longitude ?: "0.0").toDouble(),
                                (latitude ?: "0.0").toDouble(),
                            )
                        )
                    )
                )
                recyclerAddressAdapter?.notifyDataSetChanged()
            }
//            recyclerAddressAdapter?.arrAddress?.let { recyclerAddressAdapter?.addItems(it) }
            if (recyclerAddressAdapter?.arrAddress?.isNotEmpty() == true) {
                binding.recyclerSellerMyAddresses.setVisibility(true)
                binding.placeHolder.setVisibility(false)
                binding.headingPlaceHolder.setVisibility(false)
                binding.placeHolderDescription.setVisibility(false)
            } else {
                binding.recyclerSellerMyAddresses.setVisibility(false)
                binding.placeHolder.setVisibility(true)
                binding.headingPlaceHolder.setVisibility(true)
                binding.placeHolderDescription.setVisibility(true)
            }
        }
    }

    private fun initAdapter() {
        recyclerAddressAdapter = SellerMyNewAddressprofileAdapter()
        recyclerAddressAdapter?.onClickListner(this@SellerMyAddress)
        binding.recyclerSellerMyAddresses.adapter =
            recyclerAddressAdapter
    }

    private fun initPagination() {
        binding.recyclerSellerMyAddresses.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!addressListViewModel.isLastPage && !addressListViewModel.isDataLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    addressListViewModel.page = addressListViewModel.page.plus(1)
                    addressListViewModel.limit = addressListViewModel.limit

//                        binding.loader.setVisibility(addressListViewModel?.limit != 0f)
                    addressListViewModel.request.token = Preferences.getStringPreference(this@SellerMyAddress, TOKEN)
                    addressListViewModel.request.apply {
                        this.page = addressListViewModel.page.toString()
                        this.limit = addressListViewModel.limit.toString()
                    }
                    addressListViewModel.getAddressListRequest()
                    addressListViewModel.isDataLoading = true
                    isPaging = true
                }
            }
        })
    }
}