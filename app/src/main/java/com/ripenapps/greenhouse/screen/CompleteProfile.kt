package com.ripenapps.greenhouse.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
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
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityCompleteProfileBinding
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.EditAddress
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.isButtonEnable
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.CompleteProfileViewModel
import java.io.IOException
import java.util.Locale

class CompleteProfile : BaseActivity() {
    private lateinit var completeProfileBinding: ActivityCompleteProfileBinding
    private lateinit var completeProfileModel: CompleteProfileViewModel
    private lateinit var loginData: LoginResponseModel.LoginData
    private var isName = false
    private var isLocation = false
    private var isPassword = false
    private var isConfirm = false
    private var latLng: LatLng? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var city: String? = null
    private var permissionId: Int = 2
    private var mMap: GoogleMap? = null
    private var country: String? = null
    private var postalCode: String? = null
    private lateinit var locLatLong: LatLng
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@CompleteProfile, R.color.status_bar)
        completeProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_complete_profile)
        initListner()
        initViewModel()
        validationObserver()
        getLocation()
        getAddress(latitude = latitude, longitude = longitude)
        completeProfileBinding.mobileNumberComplete.setText(
            intent.getStringExtra("NUMBER1").toString()
        )
        completeProfileBinding.mobileNumberComplete.textDirection(View.TEXT_DIRECTION_LTR)
        completeProfileBinding.fullname.setTitle(getString(R.string.full_name))
        completeProfileBinding.fullname.setHint(getString(R.string.enter_full_name))
        completeProfileBinding.fullname.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        completeProfileBinding.fullname.setWidth()
        completeProfileBinding.fullname.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_regular
        )
        completeProfileBinding.fullname.setImeAction("next")

        completeProfileBinding.tax.setTitle(getString(R.string.tax_registration_number))
        completeProfileBinding.tax.setHint(getString(R.string.enter_tax_registration_number_optional))
        completeProfileBinding.tax.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
        completeProfileBinding.tax.setWidth()
        completeProfileBinding.tax.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_regular
        )
        completeProfileBinding.tax.setImeAction("next")

        completeProfileBinding.password.setTitle(getString(R.string.password))
        completeProfileBinding.password.setHint(getString(R.string.enter_password))
        completeProfileBinding.password.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
        completeProfileBinding.password.setWidth()
        completeProfileBinding.password.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_regular
        )
        completeProfileBinding.password.eyeIcon(true)
        completeProfileBinding.password.setImeAction("next")

        completeProfileBinding.confirmPassword.setTitle(getString(R.string.conformpassword))
        completeProfileBinding.confirmPassword.setHint(getString(R.string.enter_conformpassword))
        completeProfileBinding.confirmPassword.setInputType(
            true, InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        completeProfileBinding.confirmPassword.setWidth()
        completeProfileBinding.confirmPassword.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_regular
        )
        completeProfileBinding.confirmPassword.eyeIcon(true)
        completeProfileBinding.confirmPassword.setImeAction("finish")

        completeProfileBinding.email.setTitle(getString(R.string.email_address_optional))
        completeProfileBinding.email.setHint(getString(R.string.enter_email_address_optional))
        completeProfileBinding.email.setInputType(
            false, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        completeProfileBinding.email.setWidth()
        completeProfileBinding.email.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_regular
        )
        completeProfileBinding.email.setImeAction("next")

        completeProfileBinding.mobileNumberComplete.setTitle(getString(R.string.mobile_number))
        completeProfileBinding.mobileNumberComplete.setHint(getString(R.string.enter_mobil))
        completeProfileBinding.mobileNumberComplete.setInputType(
            false, InputType.TYPE_CLASS_NUMBER
        )
        completeProfileBinding.mobileNumberComplete.setWidth()
        completeProfileBinding.mobileNumberComplete.setTypeFace(
            this@CompleteProfile, R.font.euclid_circular_medium
        )
        completeProfileBinding.mobileNumberComplete.isEnable(false)
        completeProfileBinding.mobileNumberComplete.setImeAction("next")

        val terms1 = completeProfileBinding.termAndCondition
        val textLine = getString(R.string.terms_and_condition_privacy_policy)
        val ss = SpannableString(textLine)
        val termsText = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val toTermCondition = Intent(this@CompleteProfile, TermCondition::class.java)
                toTermCondition.putExtra("fromBidder","1")
                startActivity(toTermCondition)
            }
        }
        val dataText = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val toPrivacyPolicy = Intent(this@CompleteProfile, PrivacyPolicy::class.java)
                toPrivacyPolicy.putExtra("fromBidder","1")
                startActivity(toPrivacyPolicy)
            }
        }
        // Adjust indices based on your actual string content
        ss.setSpan(termsText, 33, 52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(dataText, 56, 70, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        terms1.movementMethod = LinkMovementMethod.getInstance()
        terms1.text = ss
    }
    override fun onRestart() {
        super.onRestart()
        getLocation()
    }

    private fun getLocation() {
        if (CommonUtils.checkPermissions(this)) {
            isLocation = true
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
                    Log.d("TAG", "latLng: $latitude $longitude")
                    mMap?.isMyLocationEnabled
                    if (intent.hasExtra("fromEditAddress")) {
                        val latLng = LatLng(latitude, longitude)
                        mMap?.let {
                            it.clear()
                            it.addMarker(latLng.let { ll ->
                                MarkerOptions().position(ll).icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_RED
                                    )
                                )
                            })
                            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        }
                        getAddress(latitude = latitude, longitude = longitude)
                    } else {
                        if (task.result != null) {
                            latitude = task.result.latitude
                            longitude = task.result.longitude
                        } else {
                            mFusedLocationClient?.lastLocation?.addOnSuccessListener {
                                if (it != null) {
                                    latitude = it.latitude
                                    longitude = it.longitude
                                } else {
//                                    showToast("Enable Location Permission")
//                                    showSettingLocationDialog(context = this, "Location")
                                }
                            }
                        }
                        getAddress(latitude = latitude, longitude = longitude)
                        if (location != null) {
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val list: List<Address> =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                            completeProfileBinding.apply {
                                mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                                locLatLong = LatLng(list[0].latitude, list[0].longitude)
                                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
                                mMap?.addMarker(
                                    MarkerOptions().position(locLatLong).title("")
                                        .snippet("snippet").icon(
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
                }
            } else {
                Toast.makeText(
                    this, this.getString(R.string.please_turn_on_location), Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, EditAddress.REQUEST_LOCATION_SETTINGS)
            }
        } else {
            requestPermissions()
//            showCustomToast("You need to enable the location permission.!")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.you_need_to_enable_the_location_permission),
                    Toast.LENGTH_LONG
                ).show()
            }
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
                        LocationServices.getFusedLocationProviderClient(this@CompleteProfile)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EditAddress.REQUEST_LOCATION_SETTINGS) {
            Log.i("TAG", "onActivityResult: $data")
            // User has returned from location settings, check location again
            getLocation()
        }
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                latitude = place.latLng?.latitude ?: 0.0
                longitude = place.latLng?.longitude ?: 0.0
                getAddress(latitude, longitude)
                //               mapData = MapData(lat.toString(),lon.toString(),address,binding.name.text.toString(),binding.mobNumber.text.toString().trim(),binding.fullAddress.text.toString(),adr!![0].postalCode)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
            }
        }
    }

    private var permissionPopUpShowed = false

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                CommonUtils.showSettingLocationDialog(
                    context = this,
                    askedPermission = getString(R.string.media)
                )
                completeProfileBinding.continueBtn.isButtonEnable(false)
                isLocation = false
            } else {
                permissionPopUpShowed = true
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), permissionId
                )
                isLocation = true
            }
        } else {
            getLocation()
            isLocation = true
        }
    }


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
                    var addressNew = ""
                    if (city?.isNotEmpty() == true) {
                        addressNew += "$city "
                    }
                    if (country?.isNotEmpty() == true) {
                        addressNew += "$country "
                    }
                    if (postalCode?.isNotEmpty() == true) {
                        addressNew += "$postalCode "
                    }
//                    completeProfileBinding.continueBtn.text = addressNew
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initViewModel() {
        completeProfileModel = ViewModelProvider(this)[CompleteProfileViewModel::class.java]
        completeProfileBinding.lifecycleOwner = this

        completeProfileModel.getProfileData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    completeProfileBinding.continueBtn.isEnabled = true
                    val data = Gson().fromJson(
                        AESHelper.decrypt(SECRET_KEY, it.data), LoginResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            loginData = data?.data ?: LoginResponseModel.LoginData()
                            //saving user details in local storage.
                            Preferences.setStringPreference(
                                this@CompleteProfile, "user_details", Gson().toJson(loginData)
                            )
                            Preferences.setBooleanPreference(this@CompleteProfile, "isBidder", true)
                            Preferences.setStringPreference(
                                this@CompleteProfile, "token", data.data?.token
                            )
                            val toHomePage = Intent(this, Home::class.java)
                            startActivity(toHomePage)
                            finishAffinity()
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@CompleteProfile, "token")
                            Preferences.removePreference(this@CompleteProfile, "user_details")
                            Preferences.removePreference(this@CompleteProfile, "isVerified")
                            Preferences.removePreference(this@CompleteProfile, "roomId")
                            val signin = Intent(this@CompleteProfile, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    completeProfileBinding.continueBtn.isEnabled = true
                    Log.e("TAG", "initViewModel: ${it.message}")
                    Toast.makeText(this@CompleteProfile, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListner() {
        completeProfileBinding.apply {
            fullname.initListener("other")
            email.initListener("email")
            password.initListener("password")
            confirmPassword.initListener("password", password.getText())
        }

        completeProfileBinding.continueBtn.setOnClickListener {
//            completeProfileBinding.continueBtn.isEnabled = false
            if (!isLocation) {
                showToast("Location Permission Mandatory")
                completeProfileBinding.apply {
                    continueBtn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@CompleteProfile, R.color.inactive_background
                        )
                    )
                }
                return@setOnClickListener
            }
            val pass = completeProfileBinding.password.getText()
            val confirmPass = completeProfileBinding.confirmPassword.getText()
            if (pass != confirmPass) {
                completeProfileBinding.confirmPassword.setError("Password does not match")
            } else {
                completeProfileModel.request.deviceToken = Preferences.getStringPreference(
                    this@CompleteProfile, "deviceToken"
                )
                Log.d(
                    "TAG", "deviceTokenBidder: ${
                        Preferences.getStringPreference(
                            this@CompleteProfile, "deviceToken"
                        )
                    }"
                )
                completeProfileModel.request.countryCode = CC
                completeProfileModel.request.mobile =
                    completeProfileBinding.mobileNumberComplete.getText()
                completeProfileModel.request.email = completeProfileBinding.email.getText()
                completeProfileModel.request.laguageName =
                    Preferences.getStringPreference(this@CompleteProfile, "language")
                completeProfileModel.request.userType = "bidder"

                completeProfileModel.request.taxRegistrationNumber =
                    completeProfileBinding.tax.getText().trim()
                completeProfileModel.request.password =
                    completeProfileBinding.password.getText().trim()
                completeProfileModel.request.confirm_password =
                    completeProfileBinding.confirmPassword.getText().trim()
                completeProfileModel.request.lat = latitude.toString()
                completeProfileModel.request.long = longitude.toString()
                completeProfileModel.request.name = completeProfileBinding.fullname.getText().trim()
                Log.d("TAG", "latLong: " + completeProfileModel.request)
                completeProfileModel.getProfileRequest("")
            }
        }
        completeProfileBinding.cross.setOnClickListener {
            val backToSignUp = Intent(this, SignUp::class.java)
            startActivity(backToSignUp)
            finish()
        }
    }

    private fun validationObserver() {
        completeProfileBinding.apply {
            fullname.getValidatedValue().observe(this@CompleteProfile) { validated ->
                isName = validated
                checkValidation()
            }
            password.getValidatedValue().observe(this@CompleteProfile) { validated ->
                isPassword = validated
                checkValidation()
            }
            confirmPassword.getValidatedValue().observe(this@CompleteProfile) { validated ->
                isConfirm = validated
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        if (!isLocation) {
            completeProfileBinding.continueBtn.setBackgroundColor(
                ContextCompat.getColor(
                    this@CompleteProfile, R.color.light_gray
                )
            )
            completeProfileBinding.continueBtn.isEnabled = true
            return
        }
        if (isName && isPassword && isConfirm) {
            completeProfileBinding.continueBtn.isEnabled = true
            changeButtonColor()
        } else {
            completeProfileBinding.continueBtn.isEnabled = false
        }
    }


    private fun changeButtonColor() {
        completeProfileBinding.apply {
            continueBtn.setBackgroundColor(ContextCompat.getColor(this@CompleteProfile, R.color.greenhousetheme))
        }
    }
}