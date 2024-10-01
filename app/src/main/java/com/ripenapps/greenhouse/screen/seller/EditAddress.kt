package com.ripenapps.greenhouse.screen.seller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.InputType
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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityEditAddressesBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMyAddressModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.seller.response.SellerProfileAddressAddEditResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.showCustomToast
import com.ripenapps.greenhouse.utills.CommonUtils.showSettingLocationDialog
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Constants.GOOGLE_MAP_KEY
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.ProfileAddressAddEditViewModel
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class EditAddress : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityEditAddressesBinding
    private var mMap: GoogleMap? = null
    private var profileAddressAddEditViewModel: ProfileAddressAddEditViewModel? = null
    private var latLng: LatLng? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var addressModel: SellerMyAddressModel? = null
    private var city: String? = null
    private var country: String? = null
    private var postalCode: String? = null
    private lateinit var locLatLong: LatLng
    private var permissionId: Int = 2
    private var comingFrom = ""
    private var isNumber = false
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var accountBlocked: AccountBlocked? = null

    private val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
    private var mCurrLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@EditAddress, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_addresses)

        if (Preferences.getStringPreference(this@EditAddress, "language").equals("ar")) {
            binding.btnBackEditAdd.rotation = 180f
        }

        comingFrom = intent.getStringExtra("fromEditAddress") ?: ""
        binding.apply {
            address.setTitle(resources.getString(R.string.confirm_address))
            address.setHint(resources.getString(R.string.enter_address))
            address.setInputType(false, InputType.TYPE_CLASS_TEXT)
            address.setWidth()
            address.setTypeFace(
                this@EditAddress, R.font.euclid_circular_regular
            )
            address.setImeAction("next")
            address.isFocusable(isFocused = true)
//            address.isEnable(false)
            address.setTextColor(R.color.black)

            name.setTitle(resources.getString(R.string.full_name))
            name.setHint(resources.getString(R.string.enter_full_name))
            name.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            name.setWidth()
            name.setTypeFace(
                this@EditAddress, R.font.euclid_circular_regular
            )
            name.setMaxLength(30)
            name.setImeAction("next")
            mobileNumberComplete.setTitle(resources.getString(R.string.mobile_number))
            mobileNumberComplete.setHint(resources.getString(R.string.enter_mobil))
            mobileNumberComplete.setInputType(false, InputType.TYPE_CLASS_NUMBER)
            mobileNumberComplete.setWidth()
            mobileNumberComplete.setTypeFace(this@EditAddress, R.font.euclid_circular_medium)
            mobileNumberComplete.setImeAction("done")
            mobileNumberComplete.textDirection(View.TEXT_DIRECTION_LTR)
            mobileNumberComplete.setMaxLength(9)
        }

        initMap()
        mapInitialization()

        if (intent.hasExtra("isFromOrder")) {
            binding.scrollviewLayout.setVisibility(false)
            binding.idGoogleAddress.isEnabled = false
        }
        if (intent.hasExtra("fromEditAddress")) {
            if (intent.hasExtra("dataModel")) {
                addressModel = Gson().fromJson(
                    intent.getStringExtra("dataModel"), SellerMyAddressModel::class.java
                )

                binding.address.setText(addressModel?.userAddress.toString())
                binding.mobileNumberComplete.setText(addressModel?.userNumber.toString())
                binding.name.setText(addressModel?.userName.toString())
                binding.idGoogleAddress.text = addressModel?.userAddress
                getAddress(latitude = latitude, longitude = longitude)
                longitude = addressModel?.location?.coordinates?.getOrNull(0) ?: 0.0
                latitude = addressModel?.location?.coordinates?.getOrNull(1) ?: 0.0
                Log.d("TAG", "editAddress: $latitude $longitude")
            } else {
                showCustomToast("error")
            }
        }
        initListeners()
        validationObserver()
        initViewModel()
    }

    override fun onRestart() {
        super.onRestart()
        getLocation()
    }

    private fun checkValidation() {
        if(isNumber) {
            binding.btnSave.isEnabled = true
        } else {
            binding.btnSave.isEnabled = false
        }
    }

    private fun validationObserver() {
        binding.apply {
            mobileNumberComplete.getValidatedValue().observe(this@EditAddress) { validated ->
                isNumber = validated
                if (intent.getStringExtra("fromEdit") == "1") {
                    isNumber = true
                }
                checkValidation()
            }
        }
    }

    private fun mapInitialization() {
        if (!Places.isInitialized()) {
            Places.initialize(this, GOOGLE_MAP_KEY)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun initListeners() {

        binding.mobileNumberComplete.initListener("mobile")

        binding.btnBackEditAdd.setOnClickListener {
            finish()
        }
        binding.idGoogleAddress.setOnClickListener {
            binding.idGoogleAddress.isEnabled = false
            Handler().postDelayed({
                binding.idGoogleAddress.isEnabled = true
            }, 2000)
            onSearch()
        }
        binding.icLocMark.setOnClickListener {
            getLocation()
        }
        binding.btnSave.setOnClickListener {
            if (intent.hasExtra("fromEditAddress")) {
                editAddress()
            } else {
                addAddress()
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
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
                            binding.apply {
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
                startActivityForResult(intent, REQUEST_LOCATION_SETTINGS)
            }
        } else {
            requestPermissions()
//            showCustomToast("You need to enable the location permission.!")
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
                        LocationServices.getFusedLocationProviderClient(this@EditAddress)
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
                binding.idGoogleAddress.text = place.address?.toString() ?: ""
                binding.address.setText(place.address?.toString() ?: "")
                Log.d("TAG", "latLng: $latitude $longitude")
                //               mapData = MapData(lat.toString(),lon.toString(),address,binding.name.text.toString(),binding.mobNumber.text.toString().trim(),binding.fullAddress.text.toString(),adr!![0].postalCode)
                moveCamera()
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }
            }
        }
    }

    private var permissionPopUpShowed = false

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showSettingLocationDialog(
                    context = this, askedPermission = getString(R.string.media)
                )
            } else {
                permissionPopUpShowed = true
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), permissionId
                )
            }
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if (grantResults.isNotEmpty() && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                if (!isFinishing && !permissionPopUpShowed) showSettingLocationDialog(
                    this, "Location"
                )
            }
        }
    }


    companion object {
        const val REQUEST_LOCATION_SETTINGS = 123
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getLocation()
        checkForEditLocation()
        mMap?.setOnMarkerClickListener { marker ->
            Log.i("TAG", "onMapReady: " + marker.title)
            latitude = marker.position.latitude
            longitude = marker.position.longitude
            true
        }
    }

    private fun checkForEditLocation() {
        if (intent.hasExtra("fromEditAddress")) {
            mMap?.setOnMapClickListener { latLng -> // latLng contains the tapped coordinates
                latitude = latLng.latitude
                longitude = latLng.longitude
                // Use latitude and longitude as needed
                Log.d("MapClick", "Latitude: $latitude, Longitude: $longitude")
                if (latitude == 0.0 && longitude == 0.0) {
                    val gpsTracker = GPSTracker(this)
                    latitude = gpsTracker.getLatitude()
                    longitude = gpsTracker.getLongitude()
                }
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ), 100
                )
                try {
                    getAddress(latitude, longitude)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mMap?.clear()
                mMap?.addMarker(
                    MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                moveCamera()
            }
        }
    }

    private fun addAddress() {
        profileAddressAddEditViewModel?.request?.name = binding.name.getText()
        profileAddressAddEditViewModel?.request?.mobile = binding.mobileNumberComplete.getText()
        profileAddressAddEditViewModel?.request?.countryCode = CC
        profileAddressAddEditViewModel?.request?.type = "add"
        profileAddressAddEditViewModel?.request?.userType = "seller"
        profileAddressAddEditViewModel?.request?.address = binding.address.getText()
        profileAddressAddEditViewModel?.request?.latitude = latitude.toString()
        profileAddressAddEditViewModel?.request?.longitude = longitude.toString()
        profileAddressAddEditViewModel?.request?.city = city.toString()
        profileAddressAddEditViewModel?.request?.country = country.toString()
        profileAddressAddEditViewModel?.request?.zipcode = postalCode.toString()
        profileAddressAddEditViewModel?.request?.token =
            Preferences.getStringPreference(this@EditAddress, TOKEN)
        Log.d("TAG", "addAddress: ${Gson().toJson(profileAddressAddEditViewModel?.request)}")
        profileAddressAddEditViewModel?.getProfileAddressAddEditRequest()
    }

    private fun editAddress() {
        profileAddressAddEditViewModel?.request?.name = binding.name.getText()
        profileAddressAddEditViewModel?.request?.mobile = binding.mobileNumberComplete.getText()
        profileAddressAddEditViewModel?.request?.countryCode = CC
        profileAddressAddEditViewModel?.request?.type = "edit"
        profileAddressAddEditViewModel?.request?.userType = "seller"
        profileAddressAddEditViewModel?.request?.address = binding.address.getText()
        profileAddressAddEditViewModel?.request?.latitude = latitude.toString()
        profileAddressAddEditViewModel?.request?.longitude = longitude.toString()
        profileAddressAddEditViewModel?.request?.city = city.toString()
        profileAddressAddEditViewModel?.request?.country = country.toString()
        profileAddressAddEditViewModel?.request?.addressId = addressModel?.addressId
        profileAddressAddEditViewModel?.request?.zipcode = postalCode.toString()
        profileAddressAddEditViewModel?.request?.token =
            Preferences.getStringPreference(this@EditAddress, TOKEN)
        profileAddressAddEditViewModel?.getProfileAddressAddEditRequest()
    }

    private fun initViewModel() {
        profileAddressAddEditViewModel =
            ViewModelProvider(this@EditAddress)[ProfileAddressAddEditViewModel::class.java]
        binding.lifecycleOwner = this

        profileAddressAddEditViewModel?.getProfileAddressAddEditData()?.observe(this@EditAddress) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                            ), SellerProfileAddressAddEditResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d("TAG", "responseInitViewModel: ${Gson().toJson(data.data)}")
                                if (intent.hasExtra("fromEditAddress")) {
                                    Toast.makeText(
                                        this@EditAddress,
                                        getString(R.string.address_edit_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    saveAddress(true)
                                } else {
                                    Toast.makeText(
                                        this@EditAddress, data.message, Toast.LENGTH_SHORT
                                    ).show()
                                    profileAddressAddEditViewModel?.request?.addressId =
                                        data.data?._id

                                    saveAddress(false)
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
                                Preferences.removePreference(this@EditAddress, "token")
                                Preferences.removePreference(this@EditAddress, "user_details")
                                Preferences.removePreference(this@EditAddress, "isVerified")
                                Preferences.removePreference(this@EditAddress, "roomId")
                                val signIn = Intent(this@EditAddress, SignIn::class.java)
                                startActivity(signIn)
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
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun saveAddress(isEdit: Boolean) {
        val address = binding.address.getText()
        val name = binding.name.getText()
        val number = binding.mobileNumberComplete.getText()

        Log.d("TAG", "saveAddress: $number")
        if (address.trim().isEmpty() || name.trim().isEmpty() || number.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT)
                .show()
        }
        val addressData = Intent()
        addressData.putExtra("EXTRA_ADDRESS", address)
        addressData.putExtra("EXTRA_NAME", name)
        addressData.putExtra("EXTRA_USERNUMBER", number)
        addressData.putExtra("latitude", latitude.toString())
        addressData.putExtra("longitude", longitude.toString())
        addressData.putExtra("addressId", profileAddressAddEditViewModel?.request?.addressId)
        addressData.putExtra("isAddressUpdated", if (isEdit) "1" else "")
        setResult(RESULT_OK, addressData)
        finish()
    }

    private fun onSearch() {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, 100)
    }

    private fun moveCamera() {
        latLng = LatLng(latitude, longitude)
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng!!))
        mCurrLocationMarker = mMap?.addMarker(
            MarkerOptions().position(latLng!!)
                .icon(bitmapDescriptorFromVector(R.drawable.ic_map_pin)).draggable(true)
        )
        mMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                mMap?.clear()
                val position = marker.position
                latitude = position.latitude
                longitude = position.longitude
                getAddress(latitude, longitude)
                moveCamera()
            }
        })
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(this, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
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
                    binding.idGoogleAddress.text = addressNew
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}