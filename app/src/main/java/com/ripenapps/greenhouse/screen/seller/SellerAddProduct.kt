package com.ripenapps.greenhouse.screen.seller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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
import com.ripenapps.greenhouse.adapter.seller.ImageDeleteListener
import com.ripenapps.greenhouse.adapter.seller.RecyclerMultipleImageAdapter
import com.ripenapps.greenhouse.databinding.ActivitySellerAddProductBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectCategoryModel
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectSoldProductModel
import com.ripenapps.greenhouse.datamodels.sellermodel.SellerMultipleImagesModel
import com.ripenapps.greenhouse.enums.UnitEnum
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SelectSoldProduct
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SelectUnit
import com.ripenapps.greenhouse.fragment.sellerfragemnt.SellerSelectSubCategory
import com.ripenapps.greenhouse.model.seller.response.AddProductResponseModel
import com.ripenapps.greenhouse.model.seller.response.CategoryResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerSoldProductResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.isPhoneNumberValid
import com.ripenapps.greenhouse.utills.CommonUtils.showCustomToast
import com.ripenapps.greenhouse.utills.CommonUtils.showSettingDialog
import com.ripenapps.greenhouse.utills.Constants.CC
import com.ripenapps.greenhouse.utills.Constants.GOOGLE_MAP_KEY
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.AddProductViewModel
import com.ripenapps.greenhouse.view_models.SellerSoldProductViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Suppress("DEPRECATION")
class SellerAddProduct : BaseActivity(), ImageDeleteListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var sellerAddProductBinding: ActivitySellerAddProductBinding
    private val calendar = Calendar.getInstance()
    private val calendar2 = Calendar.getInstance()
    private lateinit var addProductViewModel: AddProductViewModel
    private var soldProductViewModel: SellerSoldProductViewModel? = null
    private var imagePath: MutableList<String> = mutableListOf()
    private var adapter: RecyclerMultipleImageAdapter? = null
    private var listOfUri = mutableListOf<SellerMultipleImagesModel>()
    private var selectedCategoryPosition: Int = -1
    private var selectSoldProduct: SelectSoldProduct? = null
    private var sellerSelectSubCategory: SellerSelectSubCategory? = null
    private var selectCategory: SelectCategory? = null
    var accountBlocked: AccountBlocked? = null
    private var mCurrLocationMarker: Marker? = null
    private var mMap: GoogleMap? = null
    private var latLng: LatLng? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var city: String? = null
    private var country: String? = null
    private var postalCode: String? = null
    private lateinit var locLatLong: LatLng
    private var permissionId: Int = 2
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var categoryData: CategoryResponseModel = CategoryResponseModel()
    var startDate: String = ""
    var endDate: String = ""
    var startTime: String = ""
    var endTime: String = ""
    private var spinner2Open = false

    private val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@SellerAddProduct, R.color.status_bar)
        super.onCreate(savedInstanceState)
        sellerAddProductBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_add_product)
        if (Preferences.getStringPreference(this@SellerAddProduct, "language").equals("ar")) {
            sellerAddProductBinding.backwardImage.rotation = 180f
            sellerAddProductBinding.number.textDirection = View.TEXT_DIRECTION_LTR
        }
        initListeners()
        initViewModel()

        mapInitialization()
        Log.d("TAG", "onCreate: ${addProductViewModel.dataModel}")
    }


    private fun initViewModel() {
        addProductViewModel = ViewModelProvider(this)[AddProductViewModel::class.java]
        sellerAddProductBinding.lifecycleOwner = this@SellerAddProduct
        addProductViewModel.getCategoryRequest(this@SellerAddProduct)
        subCategoryViewModel()
        Log.e("TAG", "initViewModel: " + Gson().toJson(addProductViewModel.request))
        addProductViewModel.getAddProductData().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(
                            com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                        ), AddProductResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            Toast.makeText(this@SellerAddProduct, data.message, Toast.LENGTH_SHORT)
                                .show()
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("status", "update")
                            })
                            finish()
                        }

                        402 -> {
                            if (accountBlocked?.isAdded != true) {
                                accountBlocked = AccountBlocked(data.message.toString())
                                accountBlocked?.show(supportFragmentManager, accountBlocked?.tag)
                            }
                        }

                        403, 401 -> {
                            Preferences.removePreference(this@SellerAddProduct, "token")
                            Preferences.removePreference(this@SellerAddProduct, "user_details")
                            Preferences.removePreference(this@SellerAddProduct, "isVerified")
                            val signIn = Intent(this@SellerAddProduct, SignIn::class.java)
                            startActivity(signIn)
                            finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            Toast.makeText(this, "${data.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    sellerAddProductBinding.btnAdd.isEnabled = true
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this@SellerAddProduct)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Log.e("TAG", "onErrorCallBack: ${it.message}")
                    Toast.makeText(this@SellerAddProduct, it.message, Toast.LENGTH_SHORT).show()
                    sellerAddProductBinding.btnAdd.isEnabled = true
                }
            }
        }
    }

    private fun subCategoryViewModel() {
        sellerAddProductBinding.lifecycleOwner = this
        addProductViewModel.getCategoryData().observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                            ), CategoryResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                ProcessDialog.dismissDialog()
                                if (data.data != null) {
                                    categoryData = data
                                    addProductViewModel.dataModel = data.data
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
                                Preferences.removePreference(this@SellerAddProduct, "token")
                                Preferences.removePreference(this@SellerAddProduct, "user_details")
                                Preferences.removePreference(this@SellerAddProduct, "isVerified")
                                Preferences.removePreference(this@SellerAddProduct, "roomId")
                                val signIn = Intent(this@SellerAddProduct, SignIn::class.java)
                                startActivity(signIn)
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
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@SellerAddProduct, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.AppTheme_DatePickerDialog,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateExpiryDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set minimum date as the current date
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun showDatePickerDialog2() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.AppTheme_DatePickerDialog,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar2.set(Calendar.YEAR, year)
                calendar2.set(Calendar.MONTH, monthOfYear)
                calendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateExpiryDate2()
            },
            calendar2.get(Calendar.YEAR),
            calendar2.get(Calendar.MONTH),
            calendar2.get(Calendar.DAY_OF_MONTH)
        )
        // Set minimum date as the current date
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.datePicker.minDate = System.currentTimeMillis().plus(30)
        // Set maximum date as 30 days in the future
        calendar2.add(Calendar.DAY_OF_MONTH, 29)
//        datePickerDialog.datePicker.maxDate = calendar2.timeInMillis

//        val maxDate = Calendar.getInstance()
//        maxDate.add(Calendar.DAY_OF_MONTH, 30)
//        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog.show()
    }

    private fun updateExpiryDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selectedDate = dateFormat.format(calendar.time)
        sellerAddProductBinding.startDateText.text = selectedDate
        sellerAddProductBinding.startDateText.error = null
        startDate = selectedDate
    }

    private fun updateExpiryDate2() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selectedDate = dateFormat.format(calendar2.time)
        sellerAddProductBinding.endDateText.text = selectedDate
        sellerAddProductBinding.endDateText.error = null
        endDate = selectedDate
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            R.style.AppTheme_DatePickerDialog,
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun showTimePickerDialog2() {
        val timePickerDialog = TimePickerDialog(
            this,
            R.style.AppTheme_DatePickerDialog,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                val formattedHour = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                sellerAddProductBinding.endTimeText.text = formattedHour
                sellerAddProductBinding.endTimeText.error = null
                endTime = formattedHour
            },
            calendar2.get(Calendar.HOUR_OF_DAY),
            calendar2.get(Calendar.MINUTE),
            false// Set to true for 24-hour format
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val formattedHour = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
        sellerAddProductBinding.startTimeText.text = formattedHour
        sellerAddProductBinding.startTimeText.error = null
        startTime = formattedHour
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListeners() {
        sellerAddProductBinding.uploadPhoto.setOnClickListener {
            sellerAddProductBinding.addProductScroll.visibility = View.VISIBLE
            requestForGallery()
        }
        sellerAddProductBinding.startDateText.setOnClickListener {
            showDatePickerDialog()
        }
        sellerAddProductBinding.endDateText.setOnClickListener {
            showDatePickerDialog2()
        }
        sellerAddProductBinding.startTimeText.setOnClickListener {
            showTimePickerDialog()
        }
        sellerAddProductBinding.endTimeText.setOnClickListener {
            showTimePickerDialog2()
        }
        sellerAddProductBinding.selectLocation.setOnClickListener {
            sellerAddProductBinding.selectLocation.isEnabled = false
            Handler().postDelayed({
                sellerAddProductBinding.selectLocation.isEnabled = true
            }, 2000)
            onSearch()
        }

        sellerAddProductBinding.totalPrice.setOnClickListener {
            showDefaultDialog(this@SellerAddProduct)
        }

        sellerAddProductBinding.spinner1.setOnClickListener {
            initSoldOutViewModel()
        }
        sellerAddProductBinding.btnAdd.setOnClickListener {
            if (validateFields()) {
                sellerAddProductBinding.apply {
                    btnAdd.isEnabled = false
                    if (productDescriptionText.text.toString().isNotEmpty() && textQuantity.text.toString()
                            .isNotEmpty() && enterTotalPrice.text.toString()
                                .isNotEmpty() && selectLocation.text.toString().isNotEmpty()
                    ) {
                        addProductViewModel.request.mobile = mobileNumberText.text.toString()
                        addProductViewModel.request.countryCode = CC
                        addProductViewModel.request.price = enterTotalPrice.text.toString()
                        addProductViewModel.request.quantity = textQuantity.text.toString()
                        addProductViewModel.request.lat = latitude.toString()
                        addProductViewModel.request.long = longitude.toString()
                        addProductViewModel.request.description = productDescriptionText.text.toString()
                        addProductViewModel.request.endDate = endDateText.text.toString()
                        addProductViewModel.request.startDate = startDateText.text.toString()
                        addProductViewModel.request.startTime = startTimeText.text.toString()
                        addProductViewModel.request.endTime = endTimeText.text.toString()
                        addProductViewModel.request.productLocation = selectLocation.text.toString()
                        addProductViewModel.request.token = Preferences.getStringPreference(this@SellerAddProduct, TOKEN)
                        addProductViewModel.request.country = TimeZone.getDefault().id
                        Log.d("TAG", "addProduct: ${addProductViewModel.request.lat}")
                        addProductViewModel.getAddProductRequest(imagePath)


                    } else {
                        showToast("Please fill all the fields")
                    }
                }
            }
        }
        // Variable to track if spinner2 is open
        sellerAddProductBinding.spinner2.setOnClickListener {
            if (selectCategory?.isAdded != true) {
                spinner2Open = true
                val listOfCategories = mutableListOf<SelectCategoryModel>()
                addProductViewModel.dataModel.categoriesWithSubcategories?.forEach { model ->
                    listOfCategories.add(
                        SelectCategoryModel(
                            model.enName,
                            model._id,
                            isSelect = model._id == addProductViewModel.request.categoryId
                        )
                    )
                }

                selectCategory = SelectCategory(listOfCategories) { categoryName, id, position ->
                    sellerAddProductBinding.spinner2.text = categoryName
                    addProductViewModel.request.categoryId = id
                    addProductViewModel.dataModel.categoriesWithSubcategories?.get(position)?.subCategories
                    selectedCategoryPosition = position
                    val subCategory =
                        addProductViewModel.dataModel.categoriesWithSubcategories?.getOrNull(
                            position
                        )?.subCategories?.getOrNull(0)
                    sellerAddProductBinding.spinner3.text =
                        addProductViewModel.dataModel.categoriesWithSubcategories?.getOrNull(
                            position
                        )?.subCategories?.getOrNull(0)?.enName
                    addProductViewModel.request.subCategoryId = subCategory?._id
                }
                selectCategory?.show(supportFragmentManager, selectCategory?.tag)
            }
        }
        sellerAddProductBinding.spinner3.setOnClickListener {
            if (spinner2Open) {
                if (selectedCategoryPosition != -1) {
                    val selectedCategory =
                        addProductViewModel.dataModel.categoriesWithSubcategories?.get(
                            selectedCategoryPosition
                        )
                    val listOfSubCategories = mutableListOf<SelectCategoryModel>()
                    selectedCategory?.subCategories?.forEach { model ->
                        listOfSubCategories.add(
                            SelectCategoryModel(
                                model.enName,
                                model._id,
                                isSelect = model._id == addProductViewModel.request.subCategoryId
                            )
                        )
                    }
                    if (sellerSelectSubCategory?.isAdded != true) {
                        sellerSelectSubCategory =
                            SellerSelectSubCategory(listOfSubCategories) { subCategory, id ->
                                addProductViewModel.request.subCategoryId = id
                                sellerAddProductBinding.spinner3.text = subCategory
                            }
                        sellerSelectSubCategory?.show(
                            supportFragmentManager, sellerSelectSubCategory?.tag
                        )
                    }
                } else {
                    Toast.makeText(this, "Please select the category first.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        sellerAddProductBinding.backwardImage.setOnClickListener {
            finish()
        }
        sellerAddProductBinding.kg.setOnClickListener {
            val selectUnit = SelectUnit({ categoryUnit ->
                sellerAddProductBinding.kg.text = when (categoryUnit) {//Kilogram
                    "Kilogram" -> {
                        addProductViewModel.request.unit = UnitEnum.KILO_GRAM.value
                        getString(R.string.kg_1)
                    }

                    "Gram" -> {
                        addProductViewModel.request.unit = UnitEnum.GRAM.value
                        getString(R.string.gm_1)
                    }

                    else -> {
                        addProductViewModel.request.unit = UnitEnum.TONNE.value
                        getString(R.string.ton_1)
                    }
                }
            }, addProductViewModel.request.unit)
            selectUnit.show(supportFragmentManager, selectUnit.tag)
        }
    }

    private fun initSoldOutViewModel() {
        soldProductViewModel =
            ViewModelProvider(this@SellerAddProduct)[SellerSoldProductViewModel::class.java]
        soldProductViewModel?.request?.token = Preferences.getStringPreference(this, TOKEN)
        soldProductViewModel?.soldProductRequest()
        soldProductViewModel?.soldProductData()?.observe(this) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(
                                com.ripenapps.greenhouse.utills.Companion.SECRET_KEY, it.data
                            ), SellerSoldProductResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {

                                ProcessDialog.dismissDialog()
                                if (data.data?.orders?.isEmpty() == true) {
                                    showToast("No Sold  product Data found")
                                    return@observe
                                } else {
                                    openSoldOutDialog(data)
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@SellerAddProduct, "token")
                                Preferences.removePreference(this@SellerAddProduct, "user_details")
                                Preferences.removePreference(this@SellerAddProduct, "isVerified")
                                Preferences.removePreference(this@SellerAddProduct, "roomId")
                                val signIn = Intent(this@SellerAddProduct, SignIn::class.java)
                                startActivity(signIn)
                                finishAffinity()
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this)
                    }

                    Status.ERROR -> {
                        openSoldOutDialog(null)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun openSoldOutDialog(data: SellerSoldProductResponseModel?) {
        if (selectSoldProduct?.isAdded != true) {
            selectSoldProduct = SelectSoldProduct(data) { dataModel ->
                setUiForSoldOutProduct(dataModel)
            }
            selectSoldProduct?.show(supportFragmentManager, selectSoldProduct?.tag)
        }
    }

    private fun showDefaultDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)

        alertDialog.apply {
            val commissionPercent = getString(R.string.the_price_includes_the_15_of_commission)
            val deductionMessage =
                getString(R.string.that_will_be_deducted_when_product_is_awarded_to_someone)

            val adminCommission = intent.getStringExtra("adminCommission")
            val message = if (adminCommission.isNullOrEmpty()) {
                "$commissionPercent $deductionMessage"
            } else {
                "$commissionPercent $adminCommission $deductionMessage"
            }

            setMessage(message)
            setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                // Positive button click action
            }
        }.create().show()

    }

    private fun setUiForSoldOutProduct(dataModel: SelectSoldProductModel) {
        Log.d("TAG", "setUi: ${Gson().toJson(dataModel)}")
        listOfUri.clear()
        sellerAddProductBinding.apply {
            spinner2Open = true
            selectedCategoryPosition =
                addProductViewModel.dataModel.categoriesWithSubcategories?.indexOfFirst { it._id == dataModel.categoryId }
                    ?: -1
            spinner1.text = dataModel.subCategory
            spinner3.text = dataModel.subCategory
            setProductCategory(dataModel.category)
            productDescriptionText.setText(dataModel.description)
            textQuantity.setText(dataModel.quantity)
            selectLocation.text = dataModel.location
            mobileNumberText.setText(dataModel.mobNumber)
            enterTotalPrice.setText(dataModel.price?.replace(getString(R.string.sar), "")?.trim())
            addProductViewModel.request.categoryId = dataModel.categoryId
            addProductViewModel.request.subCategoryId = dataModel.subCategoryId
            latitude = dataModel.latitude ?: 0.0
            longitude = dataModel.longitude ?: 0.0
            addProductViewModel.request.lat = latitude.toString()
            addProductViewModel.request.long = longitude.toString()
            lifecycleScope.launch {
                dataModel.imageUrl?.forEach { url ->
                    val path =
                        addProductViewModel.getImageFilePathFromUrl(this@SellerAddProduct, url)
                    Log.d("TAG", "setUiForSoldOutProduct: $path")
                    if (path?.isNotEmpty() == true) {
                        imagePath.add(path)
                        listOfUri.add(SellerMultipleImagesModel(path))
                        Log.d("TAG", "setUiForSoldOutProduct: $path ${Gson().toJson(listOfUri)}")
                    } else {
                        showCustomToast("Unable to parse image, Please try later!!")
                    }
                }
                adapter = RecyclerMultipleImageAdapter(this@SellerAddProduct, listOfUri)
                sellerAddProductBinding.addProductScroll.adapter = adapter
            }
        }
    }

    private fun setProductCategory(category: String?) {
        with(sellerAddProductBinding) {
            spinner2.text = category
        }
    }

    private val startForImageGallery =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!CollectionUtils.isEmpty(uris)) {
                Log.d("url", "imageUris : $uris")
                for (imageUri in uris.take(15)) {
                    var filePath = CommonUtils.getRealPathFromURI(this@SellerAddProduct, imageUri)
                    if (filePath.isNotEmpty()) {
                        val compressedImgUrl = CommonUtils.compressImage(filePath, quality = 50)
                        filePath = compressedImgUrl.ifEmpty { filePath }
                        Log.d("TAG", "compressed $compressedImgUrl")
                        if (listOfUri.size < 15) {

                            imagePath.add(filePath)
                            listOfUri.add(SellerMultipleImagesModel(filePath))
                        }
                        sellerAddProductBinding.uploadPhoto.isEnabled = listOfUri.size < 15
                    } else {
                        showToast("Unable to fetch image, Try using any other format.")
                    }
                }
                adapter = RecyclerMultipleImageAdapter(this, listOfUri)
                sellerAddProductBinding.addProductScroll.adapter = adapter
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
            }
        }

    private fun openGallery() {
        try {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForImageGallery.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            showCustomToast(getString(R.string.did_not_find_activity))
        }
    }

    override fun onDeleteClicked(position: Int) {
        imagePath.removeAt(position)
        listOfUri.removeAt(position)
        sellerAddProductBinding.uploadPhoto.isEnabled = true
        // Notify the adapter of the change
        adapter?.notifyItemRemoved(position)
    }

    private fun validateFields(): Boolean {
        val categoryName = sellerAddProductBinding.spinner2.text.toString().trim()
        val subCategoryName = sellerAddProductBinding.spinner3.text.toString().trim()
        val productDescription = sellerAddProductBinding.productDescriptionText.text.toString().trim()
        val quantity = sellerAddProductBinding.textQuantity.text.toString().trim()
        val price = sellerAddProductBinding.enterTotalPrice.text.toString().trim()
        val location = sellerAddProductBinding.selectLocation.text.toString().trim()
        val mobile = sellerAddProductBinding.mobileNumberText.text.toString().trim()

        if ((adapter?.arrMultipleImage?.size ?: 0) == 0) {
            showToast("Please select at least one image")
            return false
        }

        if (categoryName.isEmpty()) {
            sellerAddProductBinding.spinner2.error = getString(R.string.please_select_a_category)
            return false
        }


        if (subCategoryName.isEmpty()) {
            sellerAddProductBinding.spinner3.error = getString(R.string.please_select_a_subcategory)
            return false
        }
        if (productDescription.isEmpty()) {
            sellerAddProductBinding.productDescriptionText.error =
                getString(R.string.please_enter_a_product_description)
            return false
        }

        if (quantity.isEmpty()) {
            sellerAddProductBinding.textQuantity.error = getString(R.string.please_enter_quantity)
            return false
        } else if (sellerAddProductBinding.textQuantity.text.toString().toInt() <= 0) {
            sellerAddProductBinding.textQuantity.error =
                getString(R.string.please_enter_valid_quantity)
            return false
        }
        if (price.isEmpty()) {
            sellerAddProductBinding.enterTotalPrice.error = getString(R.string.please_enter_price)
            return false
        } else if (sellerAddProductBinding.enterTotalPrice.text.toString().toFloat() <= 0) {
            sellerAddProductBinding.enterTotalPrice.error =
                getString(R.string.please_enter_more_than_zero_enter_price)
            return false
        }
        if (location.isEmpty()) {
            sellerAddProductBinding.selectLocation.error = getString(R.string.please_enter_location)
            return false
        }

        if (mobile.isEmpty()) {
            sellerAddProductBinding.mobileNumberText.error =
                getString(R.string.please_enter_a_mobile_number)
        } else {
            if (!isPhoneNumberValid(mobile)) {
                if (mobile.length != 9) {
                    sellerAddProductBinding.mobileNumberText.error = getString(R.string.invalid_number)
                }
                return false
            }
        }

        if (startDate.isEmpty()) {
            sellerAddProductBinding.startDateText.error =
                getString(R.string.please_select_start_date)
            return false
        }

        if (endDate.isEmpty()) {
            sellerAddProductBinding.endDateText.error = getString(R.string.please_select_end_date)
            return false
        }

        if (startTime.isEmpty()) {
            sellerAddProductBinding.startTimeText.error =
                getString(R.string.please_select_start_time)
            return false
        }

        if (endTime.isEmpty()) {
            sellerAddProductBinding.endTimeText.error = getString(R.string.please_select_end_time)
            return false
        }
        return true
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
                latitude = place.latLng?.latitude ?: 0.0
                longitude = place.latLng?.longitude ?: 0.0
                moveCamera()
                getAddress(latitude, longitude)
                sellerAddProductBinding.selectLocation.text = place.address?.toString() ?: ""
                sellerAddProductBinding.selectLocation.error = null
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
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

    companion object {
        const val REQUEST_LOCATION_SETTINGS = 123
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
                    sellerAddProductBinding.selectLocation.text = "$city  $country $postalCode"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
                        sellerAddProductBinding.apply {
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
                Toast.makeText(this, getString(R.string.please_turn_on_location), Toast.LENGTH_LONG)
                    .show()
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
                        LocationServices.getFusedLocationProviderClient(this@SellerAddProduct)
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
}

