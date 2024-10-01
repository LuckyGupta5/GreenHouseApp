package com.ripenapps.greenhouse.screen.seller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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
import com.ripenapps.greenhouse.databinding.ActivitySellerCompleteProfile2Binding
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.model.switch_profile.SwitchProfileResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.compressImage
import com.ripenapps.greenhouse.utills.CommonUtils.getRealPathFromURI
import com.ripenapps.greenhouse.utills.CommonUtils.showCustomToast
import com.ripenapps.greenhouse.utills.CommonUtils.showSettingDialog
import com.ripenapps.greenhouse.utills.Companion.Companion.SECRET_KEY
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_CONFIRM_PASSWORD
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_MAIL
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_NAME
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_NUMBER
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_PASSWORD
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.GOOGLE_MAP_KEY
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.CompleteProfileViewModel
import com.ripenapps.greenhouse.view_models.SwitchProfileViewModel
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class SellerCompleteProfile2 : BaseActivity() {
    private lateinit var sellerCompleteProfile2Binding: ActivitySellerCompleteProfile2Binding
    private var imagePath: String = ""
    private lateinit var loginData: LoginResponseModel.LoginData
    private lateinit var completeProfileModel: CompleteProfileViewModel
    private lateinit var switchProfileViewModel: SwitchProfileViewModel
    var name: String? = null
    var number: String? = null
    private var mail: String? = null
    var password: String? = null
    private var confirmPassword: String? = null
    private var vegetableClick: Boolean = true
    private var fruitsClick: Boolean = true
    private var datesClick: Boolean = true
    private val map = HashMap<Int, String>()
    private var values: List<String>? = null
    private var isBusinessNameValidated = false
    private var isAddressValidated = false
    private var isLicenseValidated = false
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
    private val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
    private var mCurrLocationMarker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SellerCompleteProfile2, R.color.status_bar)
        sellerCompleteProfile2Binding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_complete_profile2)

        name = intent.getStringExtra(USER_NAME)
        number = intent.getStringExtra(USER_NUMBER)
        mail = intent.getStringExtra(USER_MAIL)
        password = intent.getStringExtra(USER_PASSWORD)
        confirmPassword = intent.getStringExtra(USER_CONFIRM_PASSWORD)
        mapInitialization()
        initListener()
        initViewModel()
        validationObserver()
        initSwitchProfileViewModel()


        if (intent.getIntExtra("fromBidder", 0) == 1) {
            sellerCompleteProfile2Binding.personalDetail.text =
                getString(R.string.enter_your_business_details)
        }


        sellerCompleteProfile2Binding.apply {
            businessName.setTitle(getString(R.string.business_name))
            businessName.setHint(getString(R.string.enter_business_name))
            businessName.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            businessName.setWidth()
            businessName.setTypeFace(
                this@SellerCompleteProfile2, R.font.euclid_circular_regular
            )
            businessName.setValidationValue(isBusinessNameValidated)
            businessName.setImeAction("next")

            tradeLicenceNumber.setTitle("")
            tradeLicenceNumber.setHint(getString(R.string.enter_trade_license_number))
            tradeLicenceNumber.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            tradeLicenceNumber.setWidth()
            tradeLicenceNumber.setTypeFace(
                this@SellerCompleteProfile2, R.font.euclid_circular_regular
            )
            tradeLicenceNumber.setMaxLength(25)
            tradeLicenceNumber.setValidationValue(isLicenseValidated)
            tradeLicenceNumber.setImeAction("next")


            tax.setTitle(getString(R.string.tax_registration_number_optional))
            tax.setHint(getString(R.string.enter_tax_registration_number_optional))
            tax.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            tax.setWidth()
            tax.setTypeFace(
                this@SellerCompleteProfile2, R.font.euclid_circular_regular
            )
            tax.setMaxLength(25)
            tax.setImeAction("done")
        }
    }

    private fun validationObserver() {
        sellerCompleteProfile2Binding.apply {
            businessName.getValidatedValue().observe(this@SellerCompleteProfile2) { validated ->
                isBusinessNameValidated = validated
                checkValidation()
            }

            tradeLicenceNumber.getValidatedValue()
                .observe(this@SellerCompleteProfile2) { validated ->
                    isLicenseValidated = validated
                    checkValidation()
                }
        }
    }

    private fun checkValidation() {
        if (isBusinessNameValidated && isLicenseValidated) {
            sellerCompleteProfile2Binding.continueBtn.isEnabled = true
            changeButtonColor()
        } else {
            sellerCompleteProfile2Binding.continueBtn.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        sellerCompleteProfile2Binding.apply {
            continueBtn.setBackgroundColor(
                ContextCompat.getColor(
                    this@SellerCompleteProfile2, R.color.greenhousetheme
                )
            )
        }
    }

    private fun initSwitchProfileViewModel() {
        switchProfileViewModel = ViewModelProvider(this)[SwitchProfileViewModel::class.java]
        sellerCompleteProfile2Binding.lifecycleOwner = this
        switchProfileViewModel.isPhotoSelected = false
        Log.e("TAG", "initViewModel: " + Gson().toJson(switchProfileViewModel.request))
        switchProfileViewModel.getSwitchProfileData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(SECRET_KEY, it.data),
                        SwitchProfileResponseModel::class.java
                    )
                    when (data.status) {
                        424 -> {
                            ProcessDialog.dismissDialog()
                            Preferences.setStringPreference(
                                this@SellerCompleteProfile2,
                                "mobile",
                                data.data?.userDetails?.mobile
                            )
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            val adminVerify = Intent(
                                this@SellerCompleteProfile2, BackgroundVerficationAdmin::class.java
                            )
                            adminVerify.putExtra("type", "seller")
                            startActivity(adminVerify)
                            finish()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            Toast.makeText(this, "${data.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "onErrorCallBack: ${it.message}")
                    Toast.makeText(this@SellerCompleteProfile2, it.message, Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }

    private fun initViewModel() {
        completeProfileModel = ViewModelProvider(this)[CompleteProfileViewModel::class.java]
        sellerCompleteProfile2Binding.lifecycleOwner = this
        completeProfileModel.isPhotoSelected = false
        Log.e("TAG", "initViewModel: " + Gson().toJson(completeProfileModel.request))
        completeProfileModel.getProfileData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(SECRET_KEY, it.data), LoginResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            loginData = data?.data ?: LoginResponseModel.LoginData()
                            Preferences.setStringPreference(
                                this@SellerCompleteProfile2,
                                "user_details",
                                Gson().toJson(loginData)
                            )
                            Preferences.setBooleanPreference(
                                this@SellerCompleteProfile2, "isVerified", true
                            )
                            Preferences.setStringPreference(
                                this@SellerCompleteProfile2,
                                "mobile",
                                data.data?.userDetails?.mobile
                            )
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            val adminVerify = Intent(this, BackgroundVerficationAdmin::class.java)
                            startActivity(adminVerify)
                            finish()
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@SellerCompleteProfile2, "token")
                            Preferences.removePreference(
                                this@SellerCompleteProfile2,
                                "user_details"
                            )
                            Preferences.removePreference(this@SellerCompleteProfile2, "isVerified")
                            Preferences.removePreference(this@SellerCompleteProfile2, "roomId")
                            val signin = Intent(this@SellerCompleteProfile2, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            Toast.makeText(this, "${data.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this)

                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "onErrorCallBack: ${it.message}")
                    Toast.makeText(this@SellerCompleteProfile2, it.message, Toast.LENGTH_SHORT)
                        .show()

                }

            }
        }
    }

    private fun showDefaultDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.apply {
            setMessage(getString(R.string.trade_license_number_desc))
            setPositiveButton(context.getString(R.string.ok)) { _: DialogInterface?, _: Int ->
            }
        }.create().show()
    }

    private fun initListener() {
        sellerCompleteProfile2Binding.apply {
            businessName.initListener("other")
            tradeLicenceNumber.initListener("other")
            tax.initListener("other")
        }

         sellerCompleteProfile2Binding.tradeLicenceNumberHeading.setOnClickListener {
             showDefaultDialog(this@SellerCompleteProfile2)
         }

        sellerCompleteProfile2Binding.continueBtn.setOnClickListener {
            sellerCompleteProfile2Binding.continueBtn.isEnabled = false
            Handler().postDelayed({
                sellerCompleteProfile2Binding.continueBtn.isEnabled = true
            }, 2000)

            if (map.isEmpty()) {
                Toast.makeText(this, "Select at least one category", Toast.LENGTH_SHORT).show()
            } else {
                sellerCompleteProfile2Binding.continueBtn.isEnabled = true
                if (intent.hasExtra("fromBidder")) {
                    sellerCompleteProfile2Binding.apply {

                        values = map.values.toList()

                        val list2: ArrayList<String> = ArrayList()
                        for (number in values!!) {
                            if (number != "") list2.add(number)
                        }
                        switchProfileViewModel.request.token = Preferences.getStringPreference(
                            this@SellerCompleteProfile2, TOKEN
                        )
                        switchProfileViewModel.request.deviceToken =
                            Preferences.getStringPreference(
                                this@SellerCompleteProfile2, "deviceToken"
                            )
                        switchProfileViewModel.request.categories = list2
                        switchProfileViewModel.request.lat = latitude.toString()
                        switchProfileViewModel.request.long = longitude.toString()
                        switchProfileViewModel.request.userType = "seller"
                        switchProfileViewModel.request.businessName =
                            sellerCompleteProfile2Binding.businessName.getText().trim()
                        switchProfileViewModel.request.address =
                            sellerCompleteProfile2Binding.Businesslocation.text.toString()
                        switchProfileViewModel.request.licenceNumber =
                            sellerCompleteProfile2Binding.tradeLicenceNumber.getText().trim()
                        Log.d("TAG", "initListener: " + switchProfileViewModel.request)

                        if (imagePath.isEmpty()) {
                            Toast.makeText(
                                this@SellerCompleteProfile2,
                                "Please select the image",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            switchProfileViewModel.getSwitchProfileRequest(imagePath)
                        }
                        Log.d("TAG", "imagePath: $imagePath")
                    }
                } else {
                    sellerCompleteProfile2Binding.apply {
                        values = map.values.toList()
                        completeProfileModel.request.deviceToken = Preferences.getStringPreference(
                            this@SellerCompleteProfile2, "deviceToken"
                        )
                        completeProfileModel.request.categories = values
                        completeProfileModel.request.lat = latitude.toString()
                        completeProfileModel.request.long = longitude.toString()
                        completeProfileModel.request.laguageName =
                            Preferences.getStringPreference(this@SellerCompleteProfile2, "language")
                        completeProfileModel.request.userType = "seller"
                        completeProfileModel.request.name = name
                        completeProfileModel.request.mobile = number
                        completeProfileModel.request.email = mail
                        completeProfileModel.request.countryCode = CC
                        completeProfileModel.request.password = password
                        completeProfileModel.request.confirm_password = confirmPassword
                        completeProfileModel.request.businessName =
                            sellerCompleteProfile2Binding.businessName.getText().trim()
                        completeProfileModel.request.address =
                            sellerCompleteProfile2Binding.Businesslocation.text.toString()
                        completeProfileModel.request.licenceNumber =
                            sellerCompleteProfile2Binding.tradeLicenceNumber.getText().trim()
                        completeProfileModel.request.licenceExpiry = ""
                        completeProfileModel.request.taxRegistrationNumber =
                            sellerCompleteProfile2Binding.tax.getText().trim()
                        Log.d("TAG", "initListener: " + completeProfileModel.request)

                        if (imagePath.isEmpty()) {
                            Toast.makeText(
                                this@SellerCompleteProfile2,
                                "Please select the image",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            completeProfileModel.getProfileRequest(imagePath)
                        }
                    }
                }
            }
        }
        sellerCompleteProfile2Binding.cross.setOnClickListener {
            finish()
        }


        sellerCompleteProfile2Binding.vegetableLayout.setOnClickListener {
            sellerCompleteProfile2Binding.apply {
                if (vegetableClick) {
                    vegetableClick = false

                    map[0] = VEGETABLES
                    vegetableLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_rating_seller_green
                    )
                    vegetableText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.greenhousetheme
                        )
                    )
                } else {
                    vegetableClick = true
                    map[0] = ""

                    vegetableLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_eoeoeo
                    )
                    vegetableText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.black
                        )
                    )
                }


            }
        }

        sellerCompleteProfile2Binding.fruitsLayout.setOnClickListener {
            sellerCompleteProfile2Binding.apply {
                if (fruitsClick) {
                    map[1] = FRUITS

                    fruitsLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_rating_seller_green
                    )
                    fruitesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.greenhousetheme
                        )
                    )
                    fruitsClick = false
                } else {
                    fruitsClick = true
                    map[1] = ""

                    fruitsLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_eoeoeo
                    )

                    fruitesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.black
                        )
                    )
                }
            }
        }

        sellerCompleteProfile2Binding.datesLayout.setOnClickListener {
            sellerCompleteProfile2Binding.apply {
                if (datesClick) {
                    map[2] = DATES
                    datesLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_rating_seller_green
                    )
                    datesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.greenhousetheme
                        )
                    )
                    datesClick = false
                } else {
                    map[2] = ""

                    datesClick = true
                    datesLayout.background = ContextCompat.getDrawable(
                        this@SellerCompleteProfile2, R.drawable.border_eoeoeo
                    )

                    datesText.setTextColor(
                        ContextCompat.getColor(
                            this@SellerCompleteProfile2, R.color.black
                        )
                    )
                }
            }
        }

        sellerCompleteProfile2Binding.uploadDoc.setOnClickListener {
//            selectImage()
            requestForGallery()
        }

        sellerCompleteProfile2Binding.Businesslocation.setOnClickListener {
            sellerCompleteProfile2Binding.Businesslocation.isEnabled = false
            Handler().postDelayed({
                sellerCompleteProfile2Binding.Businesslocation.isEnabled = true
            }, 2000)
            onSearch()
        }
        sellerCompleteProfile2Binding.deleteDoc.setOnClickListener {
            imagePath = ""
            sellerCompleteProfile2Binding.apply {
                uploadDoc.setVisibility(true)
                deleteDoc.setVisibility(false)
                docImage.setVisibility(false)
            }
        }
    }

    private val startForImageGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        val imageUri = data.clipData!!.getItemAt(i).uri

                        imagePath =
                            getRealPathFromURI(this@SellerCompleteProfile2, imageUri)

                        val compressedImgUrl = compressImage(imagePath, quality = 50)
                        imagePath = compressedImgUrl.ifEmpty { imagePath }
                        Log.d("TAG", "compressed $compressedImgUrl")
                        Glide.with(this@SellerCompleteProfile2).load(compressedImgUrl)
                            .into(sellerCompleteProfile2Binding.docImage)
                        sellerCompleteProfile2Binding.docImage.setVisibility(true)
                        sellerCompleteProfile2Binding.uploadDoc.setVisibility(false)
                        sellerCompleteProfile2Binding.deleteDoc.setVisibility(true)
                        completeProfileModel.isPhotoSelected = true
                        switchProfileViewModel.isPhotoSelected = true
                    }
                } else {
                    val fileUri = data?.data
                    if (fileUri?.path?.isNotEmpty() == true) imagePath =
                        getRealPathFromURI(this@SellerCompleteProfile2, fileUri)
                    val compressedImgUrl = compressImage(imagePath, quality = 50)
                    imagePath = compressedImgUrl.ifEmpty { imagePath }
                    Log.d("TAG", "compressed $compressedImgUrl")
                    Log.d("image Path", imagePath)
                    Glide.with(this@SellerCompleteProfile2).load(compressedImgUrl)
                        .into(sellerCompleteProfile2Binding.docImage)
                    completeProfileModel.isPhotoSelected = true
                    switchProfileViewModel.isPhotoSelected = true
                    sellerCompleteProfile2Binding.docImage.visibility = View.VISIBLE
                    sellerCompleteProfile2Binding.uploadDoc.visibility = View.GONE
                    sellerCompleteProfile2Binding.deleteDoc.visibility = View.VISIBLE
                }
            }
        }

    private fun selectImage() {
        val permission: Array<String?> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            else arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (CommonUtils.requestPermissions(this@SellerCompleteProfile2, 100, permission)) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForImageGallery.launch(intent)
        } else {
//            Toast.makeText(
//                this@SellerCompleteProfile2,
//                getString(R.string.green_house_requires_this_permission),
//                Toast.LENGTH_SHORT
//            ).show()
            showSettingDialog(this@SellerCompleteProfile2)
        }
    }

    private fun requestForGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ActivityCompat.checkSelfPermission(
                this, permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showSettingDialog(context = this, askedPermission = "Media")
            } else {
                permissionPopUpShowed = true
                storagePermission.launch(permission)
            }
        } else {
            openGallery()
        }
    }

    private var permissionPopUpShowed: Boolean = false
    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                openGallery()
            } else {
                if (!isFinishing && !permissionPopUpShowed) {
                    showSettingDialog(this, askedPermission = "Media")
                }
                showSettingDialog(this, askedPermission = "Media")
            }
        }

    private fun openGallery() {
        try {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForImageGallery.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showCustomToast(getString(R.string.did_not_find_activity))
        }
    }

    private fun onSearch() {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, 100)
    }

    private fun mapInitialization() {
        if (!Places.isInitialized()) {
            Places.initialize(this, GOOGLE_MAP_KEY)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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
                latitude = place.latLng?.latitude ?: 0.0
                longitude = place.latLng?.longitude ?: 0.0
                getAddress(latitude, longitude)
                isAddressValidated = true
                sellerCompleteProfile2Binding.Businesslocation.text =
                    place.address?.toString() ?: ""
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if (grantResults.isNotEmpty() && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                showSettingDialog(this@SellerCompleteProfile2, "Location")
            }
        }
    }

    companion object {
        const val REQUEST_LOCATION_SETTINGS = 123
    }

//    fun onMapReady(p0: GoogleMap) {
//        mMap = p0
//        mMap?.setOnMarkerClickListener { marker ->
//            Log.i("TAG", "onMapReady: " + marker.title)
//            latitude = marker.position.latitude
//            longitude = marker.position.longitude
//            true
//        }
//    }

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
                    sellerCompleteProfile2Binding.Businesslocation.text = addressNew
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
                        sellerCompleteProfile2Binding.apply {
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
            showCustomToast(getString(R.string.you_need_to_enable_the_location_permission))
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
                        LocationServices.getFusedLocationProviderClient(this@SellerCompleteProfile2)
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


}
