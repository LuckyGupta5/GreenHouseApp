package com.ripenapps.greenhouse.screen.seller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySellerBusinessDetailsBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.profile.GetUserDetailResponseModel
import com.ripenapps.greenhouse.model.seller.response.EditProfileResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.showCustomToast
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.GOOGLE_MAP_KEY
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.EditProfileViewModel
import com.ripenapps.greenhouse.view_models.UserDetailsViewModel
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class SellerBusinessDetails : BaseActivity() {
    private lateinit var sellerBusinessDetailsBinding: ActivitySellerBusinessDetailsBinding
    private var getUserDetailsViewModel: UserDetailsViewModel? = null
    private var vegetableClick: Boolean = false
    private var fruitsClick: Boolean = false
    private var datesClick: Boolean = false
    private var userType: String? = null
    private var editProfileViewModel: EditProfileViewModel? = null
    private val hashMap = HashMap<Int, String>()
    private var values: List<String>? = null
    var accountBlocked: AccountBlocked? = null
    private val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
    private var mMap: GoogleMap? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var city: String? = null
    private var country: String? = null
    private var postalCode: String? = null
    private lateinit var locLatLong: LatLng
    private var permissionId: Int = 2
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var isBusinessNameValidated = false
    private var isLicenseValidated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@SellerBusinessDetails, R.color.status_bar)
        super.onCreate(savedInstanceState)
        sellerBusinessDetailsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_business_details)

        if (Preferences.getStringPreference(this@SellerBusinessDetails, "language") == "ar") {
            sellerBusinessDetailsBinding.backBusinessDetails.rotation = 180f
        }

        sellerBusinessDetailsBinding.apply {
            businessName.setTitle(resources.getString(R.string.business_name))
            businessName.setHint(resources.getString(R.string.enter_business_name))
            businessName.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            businessName.setWidth()
            businessName.setMaxLength(30)
            businessName.setMaxLines(2)
            businessName.setTypeFace(this@SellerBusinessDetails, R.font.euclid_circular_regular)
            businessName.setValidationValue(isBusinessNameValidated)
            businessName.setImeAction("next")
            tradeLicenceNumber.setTitle(resources.getString(R.string.trade_license_number))
            tradeLicenceNumber.setHint(resources.getString(R.string.enter_trade_license_number))
            tradeLicenceNumber.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            tradeLicenceNumber.setWidth()
            tradeLicenceNumber.setTypeFace(
                this@SellerBusinessDetails, R.font.euclid_circular_regular
            )
            tradeLicenceNumber.setMaxLength(25)
            tradeLicenceNumber.setValidationValue(isLicenseValidated)
            tradeLicenceNumber.setImeAction("next")
            tax.setTitle(resources.getString(R.string.tax_registration_number_optional))
            tax.setHint(resources.getString(R.string.enter_tax_registration_number_optional))
            tax.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            tax.setWidth()
            tax.setTypeFace(
                this@SellerBusinessDetails, R.font.euclid_circular_regular
            )
            tax.setMaxLength(25)
            tax.setImeAction("done")
        }
        initViewModel()
        initListeners()
        mapInitialization()
        initSellerBusinessDetails()

        userType = if (intent.getStringExtra(com.ripenapps.greenhouse.utills.Companion.USER_TYPE)
                .toString() == "1"
        ) {
            "bidder"
        } else {
            "seller"
        }
        getUserDetailsViewModel?.request?.token =
            Preferences.getStringPreference(this@SellerBusinessDetails, Constants.TOKEN)
        getUserDetailsViewModel?.getUserDetailsRequest()
    }

    private fun initSellerBusinessDetails() {
        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        sellerBusinessDetailsBinding.lifecycleOwner = this

        editProfileViewModel?.getEditProfileData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                            ), EditProfileResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Toast.makeText(
                                    this@SellerBusinessDetails, data.message, Toast.LENGTH_SHORT
                                ).show()
                                finish()
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
                                Preferences.removePreference(this@SellerBusinessDetails, "token")
                                Preferences.removePreference(this@SellerBusinessDetails, "user_details")
                                Preferences.removePreference(this@SellerBusinessDetails, "isVerified")
                                Preferences.removePreference(this@SellerBusinessDetails, "roomId")
                                val signin = Intent(this@SellerBusinessDetails, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this)
                    }

                    Status.ERROR -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@SellerBusinessDetails, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initViewModel() {
        getUserDetailsViewModel =
            ViewModelProvider(this@SellerBusinessDetails)[UserDetailsViewModel::class.java]
        sellerBusinessDetailsBinding.lifecycleOwner = this@SellerBusinessDetails
        getUserDetailsViewModel?.getUserDetailsData()?.observe(this@SellerBusinessDetails) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                            ), GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                Log.d("TAG", "userDetailsData: " + data.data?.userDetails)
                                sellerBusinessDetailsBinding.apply {
                                    businessName.setText(data.data?.userDetails?.businessName ?: "")
                                    Businesslocation.text = data.data?.userDetails?.address ?: ""
                                    tradeLicenceNumber.setText(
                                        data.data?.userDetails?.licenceNumber ?: ""
                                    )
                                    expiryDate.setText(
                                        data.data?.userDetails?.licenceExpiry ?: "N/A"
                                    )
                                    tax.setText(data.data?.userDetails?.taxRegistrationNumber ?: "")
                                    val list: ArrayList<String> = ArrayList()
                                    data.data?.userDetails?.categories?.forEachIndexed { index, category ->
                                        Log.d("TAG", "initViewModel: ${category.categoryId ?: ""}")
                                        hashMap[index] = category.categoryId ?: ""
                                        list.add(category.categoryId ?: "")
                                    }
                                    if (list.contains(VEGETABLES)) {
                                        vegetableClick = true
                                        vegetableLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails,
                                            R.drawable.border_rating_seller_green
                                        )
                                        vegetableText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.greenhousetheme
                                            )
                                        )
                                    } else {
                                        vegetableClick = false
                                        vegetableLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails, R.drawable.border_eoeoeo
                                        )
                                        vegetableText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.black
                                            )
                                        )
                                    }

                                    if (list.contains(FRUITS)) {
                                        fruitsClick = true
                                        fruitsLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails,
                                            R.drawable.border_rating_seller_green
                                        )
                                        fruitesText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.greenhousetheme
                                            )
                                        )
                                    } else {
                                        fruitsClick = false
                                        fruitsLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails, R.drawable.border_eoeoeo
                                        )
                                        fruitesText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.black
                                            )
                                        )
                                    }

                                    if (list.contains(DATES)) {
                                        datesClick = true
                                        datesLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails,
                                            R.drawable.border_rating_seller_green
                                        )
                                        datesText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.greenhousetheme
                                            )
                                        )
                                    } else {
                                        datesClick = false
                                        datesLayout.background = ContextCompat.getDrawable(
                                            this@SellerBusinessDetails, R.drawable.border_eoeoeo
                                        )
                                        datesText.setTextColor(
                                            ContextCompat.getColor(
                                                this@SellerBusinessDetails, R.color.black
                                            )
                                        )
                                    }
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
                                Preferences.removePreference(this@SellerBusinessDetails, "token")
                                Preferences.removePreference(this@SellerBusinessDetails, "user_details")
                                Preferences.removePreference(this@SellerBusinessDetails, "isVerified")
                                Preferences.removePreference(this@SellerBusinessDetails, "roomId")
                                val signin = Intent(this@SellerBusinessDetails, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(
                                    this@SellerBusinessDetails, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this@SellerBusinessDetails)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initListeners() {
        sellerBusinessDetailsBinding.saveBtn.setOnClickListener {
            sellerBusinessDetailsBinding.apply {
                if (businessName.getText().isNotEmpty() && Businesslocation.text.toString()
                        .isNotEmpty() && tradeLicenceNumber.getText().isNotEmpty()
                ) {
                    values = hashMap.values.toList()

                    val categoryList: ArrayList<String> = ArrayList()
                    if (vegetableClick) categoryList.add(VEGETABLES)
                    if (fruitsClick) categoryList.add(FRUITS)
                    if (datesClick) categoryList.add(DATES)
                    Log.d("listOfCategories", "initListeners: ${Gson().toJson(categoryList)}")

                    editProfileViewModel?.request?.categories = categoryList
                    editProfileViewModel?.request?.lat = latitude.toString()
                    editProfileViewModel?.request?.long = longitude.toString()
                    editProfileViewModel?.request?.type = "business"
                    editProfileViewModel?.request?.userType = "seller"
                    editProfileViewModel?.request?.businessName =
                        sellerBusinessDetailsBinding.businessName.getText().trim()
                    editProfileViewModel?.request?.address =
                        sellerBusinessDetailsBinding.Businesslocation.text.toString()
                    editProfileViewModel?.request?.licenceNumber =
                        sellerBusinessDetailsBinding.tradeLicenceNumber.getText().trim()
                    editProfileViewModel?.request?.taxRegistrationNumber =
                        sellerBusinessDetailsBinding.tax.getText().trim()
                    editProfileViewModel?.request?.token =
                        Preferences.getStringPreference(this@SellerBusinessDetails, Constants.TOKEN)
                    Log.d("TAG", "initListener: " + editProfileViewModel?.request)
                    editProfileViewModel?.getEditProfileRequest()
                } else {
                    showToast(getString(R.string.fill_all_the_fields))
                }
            }
        }

        sellerBusinessDetailsBinding.backBusinessDetails.setOnClickListener {
            finish()
        }

        sellerBusinessDetailsBinding.Businesslocation.setOnClickListener {
            sellerBusinessDetailsBinding.Businesslocation.isEnabled = false
            Handler().postDelayed({
                sellerBusinessDetailsBinding.Businesslocation.isEnabled = true
            }, 2000)
            onSearch()
        }
        sellerBusinessDetailsBinding.vegetableLayout.setOnClickListener {
            sellerBusinessDetailsBinding.apply {
                if (!vegetableClick) {
                    vegetableClick = true
                    hashMap[0] = VEGETABLES
                    vegetableLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_rating_seller_green
                    )
                    vegetableText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.greenhousetheme
                        )
                    )
                } else {
                    vegetableClick = false
                    hashMap[0] = ""
                    vegetableLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_eoeoeo
                    )
                    vegetableText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.black
                        )
                    )
                }
            }
        }

        sellerBusinessDetailsBinding.fruitsLayout.setOnClickListener {
            sellerBusinessDetailsBinding.apply {
                if (!fruitsClick) {
                    fruitsClick = true
                    hashMap[1] = FRUITS
                    fruitsLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_rating_seller_green
                    )
                    fruitesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.greenhousetheme
                        )
                    )
                } else {
                    fruitsClick = false
                    hashMap[1] = ""
                    fruitsLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_eoeoeo
                    )
                    fruitesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.black
                        )
                    )
                }
            }
        }

        sellerBusinessDetailsBinding.datesLayout.setOnClickListener {
            sellerBusinessDetailsBinding.apply {
                if (!datesClick) {
                    datesClick = true
                    hashMap[2] = DATES
                    datesLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_rating_seller_green
                    )
                    datesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.greenhousetheme
                        )
                    )
                } else {
                    hashMap[2] = ""
                    datesClick = false
                    datesLayout.background = ContextCompat.getDrawable(
                        this@SellerBusinessDetails, R.drawable.border_eoeoeo
                    )
                    datesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerBusinessDetails, R.color.black
                        )
                    )
                }
            }
        }
    }

    private fun onSearch() {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, 100)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            Log.i("TAG", "onActivityResult: $data")
            // User has returned from location settings, check location again
            getLocation()
        }
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
                getAddress(latitude, longitude)
                sellerBusinessDetailsBinding.Businesslocation.text = place.address?.toString() ?: ""
                //               mapData = MapData(lat.toString(),lon.toString(),address,binding.name.text.toString(),binding.mobNumber.text.toString().trim(),binding.fullAddress.text.toString(),adr!![0].postalCode)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }
            }
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            ), permissionId
        )
    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    city = address.subLocality // City name
                    country = address.countryName // Country name
                    postalCode = address.postalCode // ZIP code
                    sellerBusinessDetailsBinding.Businesslocation.text =
                        "$city  $country $postalCode"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getLocation() {
        if (CommonUtils.checkPermissions(this)) {
            if (CommonUtils.isLocationEnabled(this)) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                Log.i("TAG", "getLocation: mFusedLocationClient $mFusedLocationClient")
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mFusedLocationClient?.lastLocation?.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    Log.i("TAG", "getLocation: $task")
                    Log.i("TAG", "getLocation: " + task.result)
                    latitude = task.result.latitude
                    longitude = task.result.longitude
                    getAddress(latitude = latitude, longitude = longitude)
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        sellerBusinessDetailsBinding.apply {
                            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                            locLatLong = LatLng(list[0].latitude, list[0].longitude)
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
                            mMap?.addMarker(
                                MarkerOptions().position(locLatLong).title("").snippet("snippet")
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED
                                        )
                                    )
                            )
                        }
                    } else {
                        forceFetchLocation()
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, EditAddress.REQUEST_LOCATION_SETTINGS)
            }
        } else {
            requestPermissions()
            showCustomToast("You need to enable the location permission.!")
        }
    }

    private fun forceFetchLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 2500
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mostRecentLocation = locationResult.lastLocation
                if (mostRecentLocation != null) {
                    if (isFinishing) {
                        LocationServices.getFusedLocationProviderClient(this@SellerBusinessDetails)
                            .removeLocationUpdates(this)
                        getLocation()
                    }

                }

            }
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun mapInitialization() {
        if (!Places.isInitialized()) {
            Places.initialize(this, GOOGLE_MAP_KEY)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    companion object {
        const val REQUEST_LOCATION_SETTINGS = 123
    }
}


