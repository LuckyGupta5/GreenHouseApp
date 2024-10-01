package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentSellerHomeBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.WalletFilter
import com.ripenapps.greenhouse.model.profile.GetUserDetailResponseModel
import com.ripenapps.greenhouse.model.seller.response.SellerHomeGraphResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.screen.bidder.BidderNotification
import com.ripenapps.greenhouse.screen.seller.SellerWallet
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.convertDateToAnotherFormat
import com.ripenapps.greenhouse.utills.CommonUtils.getCategoryId
import com.ripenapps.greenhouse.utills.CommonUtils.getShortMonthName
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.DATES
import com.ripenapps.greenhouse.utills.Constants.FRUITS
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Constants.VEGETABLES
import com.ripenapps.greenhouse.utills.CustomMarkerView
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.formatToReadableNumber
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.SellerHomeGraphViewModel
import com.ripenapps.greenhouse.view_models.UserDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SellerHomeFragment : Fragment() {
    private lateinit var sellerHomeFragmentBinding: FragmentSellerHomeBinding
    private val calendar = Calendar.getInstance()
    private var filterModel: FilterModel? = FilterModel()
    var accountBlocked: AccountBlocked? = null
    private var toSellerWallet: WalletFilter? = null
    private var homeGraphViewModel: SellerHomeGraphViewModel? = null
    private lateinit var getUserDetailsViewModel: UserDetailsViewModel
    var categoryId: String? = null
    private lateinit var mContext: Context
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        sellerHomeFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_seller_home, container, false
        )


        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            sellerHomeFragmentBinding.greenHouseImg.scaleType = ImageView.ScaleType.FIT_END
        }

        checkNotificationPermission()
        pullToRefresh()
        initStartActivityInstance()
        userDetailsViewModel()
        initListeners()
        userDetailApi()
        homeGraphViewModel()
        hitApi()
        return sellerHomeFragmentBinding.root

    }

    private fun pullToRefresh() {
        sellerHomeFragmentBinding.idRefreshLayout.setOnRefreshListener {
            filterModel = null
            homeGraphViewModel?.request?.startDate = null
            homeGraphViewModel?.request?.endDate = null
            homeGraphViewModel?.request?.token =
                Preferences.getStringPreference(mContext, "token")
            homeGraphViewModel?.getHomeGraphRequest()
            sellerHomeFragmentBinding.startDateText.text = getString(R.string.start_date)
            sellerHomeFragmentBinding.endDateText.text = getString(R.string.end_date)
        }
    }

    private fun userDetailApi() {
        getUserDetailsViewModel.request.token = Preferences.getStringPreference(mContext, TOKEN)
        getUserDetailsViewModel.getUserDetailsRequest()
    }

    private fun hitApi() {
        if (!hitApiIfDatesAreFilled()) return
        homeGraphViewModel?.request?.token = Preferences.getStringPreference(mContext, TOKEN)
        homeGraphViewModel?.request?.apply {
            categoryId = filterModel?.categoryId
            startDate = filterModel?.startDate
            endDate = filterModel?.endDate
        }
        homeGraphViewModel?.getHomeGraphRequest()
        homeGraphViewModel?.request?.categoryId = categoryId
    }

    private fun userDetailsViewModel() {
        getUserDetailsViewModel =
            ViewModelProvider(this@SellerHomeFragment)[UserDetailsViewModel::class.java]
        sellerHomeFragmentBinding.lifecycleOwner = this@SellerHomeFragment
        getUserDetailsViewModel.getUserDetailsData().observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetUserDetailResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                sellerHomeFragmentBinding.apply {
                                    homeWallet.text =
                                        data?.data?.userDetails?.amount?.formatToReadableNumber()
                                            ?: ""
                                }
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        parentFragmentManager, accountBlocked?.tag
                                    )
                                }
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

                            else -> {
                                ProcessDialog.dismissDialog()
                                Toast.makeText(mContext, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun homeGraphViewModel() {
        homeGraphViewModel =
            ViewModelProvider(this@SellerHomeFragment)[SellerHomeGraphViewModel::class.java]
        sellerHomeFragmentBinding.lifecycleOwner = this@SellerHomeFragment
        homeGraphViewModel?.getHomeGraphData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        sellerHomeFragmentBinding.sellerHomeShimmer.sellerHomeMainShimmer.setVisibility(false)

                        sellerHomeFragmentBinding.idRefreshLayout.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            SellerHomeGraphResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                sellerHomeFragmentBinding.idRefreshLayout.isRefreshing = false

                                if (!checkForEmptyData(data.data?.newData?.revenue)) {
                                    sellerHomeFragmentBinding.apply {
                                        placeHolder.setVisibility(true)
                                        headingPlaceHolder.setVisibility(true)
                                        placeHolderDescription.setVisibility(true)
                                        revenueChartData.setVisibility(false)
                                        revenueChart.setVisibility(false)
                                        bookingChartText.setVisibility(false)
                                        comparisionChart.setVisibility(false)
                                        bookingChart.setVisibility(false)
                                        pieChart.setVisibility(false)
                                        selectCategory.setVisibility(false)

                                    }
                                    return@observe
                                } else {
                                    sellerHomeFragmentBinding.apply {
                                        placeHolder.visibility = View.GONE
                                        headingPlaceHolder.visibility = View.GONE
                                        placeHolderDescription.visibility = View.GONE
                                        revenueChartData.setVisibility(true)
                                        bookingChart.setVisibility(true)
                                        pieChart.setVisibility(true)
                                        revenueChart.setVisibility(true)
                                        bookingChartText.setVisibility(true)
                                        comparisionChart.setVisibility(true)
                                        selectCategory.setVisibility(true)
                                        revenueChart(data?.data?.newData)
                                        pieChart(data?.data?.newData)
                                        bookingChart(data?.data?.newData)
                                    }
                                }
                            }

                            401, 403 -> {
                                ProcessDialog.dismissDialog()
                                val toSignIn = Intent(mContext, SignIn::class.java)
                                startActivity(toSignIn)
                                activity?.finishAffinity()
                            }

                            else -> {
                                sellerHomeFragmentBinding.sellerHomeShimmer.sellerHomeMainShimmer.setVisibility(false)
                                sellerHomeFragmentBinding.idRefreshLayout.setVisibility(true)
                                Toast.makeText(
                                    mContext, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        sellerHomeFragmentBinding.sellerHomeShimmer.sellerHomeMainShimmer.setVisibility(true)
                        sellerHomeFragmentBinding.idRefreshLayout.setVisibility(false)
                        sellerHomeFragmentBinding.placeHolder.setVisibility(false)
                        sellerHomeFragmentBinding.placeHolderDescription.setVisibility(false)
                        sellerHomeFragmentBinding.headingPlaceHolder.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                        sellerHomeFragmentBinding.sellerHomeShimmer.sellerHomeMainShimmer.setVisibility(true)
                        sellerHomeFragmentBinding.idRefreshLayout.setVisibility(false)
                        sellerHomeFragmentBinding.placeHolder.setVisibility(false)
                        sellerHomeFragmentBinding.placeHolderDescription.setVisibility(false)
                        sellerHomeFragmentBinding.headingPlaceHolder.setVisibility(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun checkForEmptyData(revenue: List<SellerHomeGraphResponseModel.Data.NewData.Revenue>?): Boolean {
        var isDataFound = false
        revenue?.forEach { model ->
            if (model.categories.isNotEmpty()) {
                isDataFound = true
                return@forEach
            }
        }
        return isDataFound
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            mContext,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Set the selected date in the TextInputEditText
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                startDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showDatePickerDialog2() {
        val datePickerDialog = DatePickerDialog(
            mContext,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Set the selected date in the TextInputEditText
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                endDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun startDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val selectedDate = dateFormat.format(calendar.time)
        val convertedDate = convertDateToAnotherFormat(selectedDate, "dd/MM/yyyy", "yyyy-MM-dd")
        sellerHomeFragmentBinding.startDateText.text = convertedDate
        homeGraphViewModel?.request?.startDate = convertedDate
        if (filterModel == null) filterModel = FilterModel()
        filterModel?.startDate = convertedDate
        Log.d("TAG", "startDate: ${homeGraphViewModel?.request?.startDate}")
        hitApi()
    }

    private fun endDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val selectedDate = dateFormat.format(calendar.time)
        val convertedDate = convertDateToAnotherFormat(selectedDate, "dd/MM/yyyy", "yyyy-MM-dd")
        sellerHomeFragmentBinding.endDateText.text = convertedDate
        homeGraphViewModel?.request?.endDate = convertedDate
        if (filterModel == null) filterModel = FilterModel()
        filterModel?.endDate = convertedDate
        Log.d("TAG", "startDate: ${homeGraphViewModel?.request?.endDate}")
        hitApi()
    }

    private fun hitApiIfDatesAreFilled(): Boolean {
        val startDate = sellerHomeFragmentBinding.startDateText.text.toString()
        val endDate = sellerHomeFragmentBinding.endDateText.text.toString()

        if (startDate.isEmpty()) {
            Toast.makeText(context, "Please select a start date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (endDate.isEmpty()) {
            Toast.makeText(context, "Please select an end date", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun initListeners() {
        sellerHomeFragmentBinding.startDateText.setOnClickListener {
            showDatePickerDialog()
        }
        sellerHomeFragmentBinding.endDateText.setOnClickListener {
            showDatePickerDialog2()
        }
        sellerHomeFragmentBinding.sellerHomeFilter.setOnClickListener {
            if (toSellerWallet?.isAdded != true) {
                toSellerWallet = WalletFilter(filterModel, isFromGraph = false, ::handelFilter)
                toSellerWallet?.show(parentFragmentManager, toSellerWallet?.tag)
            }
        }
        sellerHomeFragmentBinding.walletBox.setOnClickListener {
            val toSellerWallet = Intent(mContext, SellerWallet::class.java)
            getContent.launch(toSellerWallet)
        }
        sellerHomeFragmentBinding.icNotification.setOnClickListener {
            val toNotification = Intent(mContext, BidderNotification::class.java)
            startActivity(toNotification)
        }
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        userDetailApi()
                    }
                }
            }
    }

    private fun handelFilter(model: FilterModel? = null) {
        if (filterModel == null) filterModel = FilterModel()
        filterModel?.categoryId = getCategoryId(model?.transactionType ?: "")
        filter()
    }

    private fun filter() {
        homeGraphViewModel?.request?.categoryId = filterModel?.transactionType
        hitApi()
    }

    @SuppressLint("DefaultLocale", "NewApi")
    private fun pieChart(newDataPie: SellerHomeGraphResponseModel.Data.NewData?) {
        sellerHomeFragmentBinding.pieChart.setUsePercentValues(true)
        sellerHomeFragmentBinding.pieChart.description.isEnabled = false
        sellerHomeFragmentBinding.pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        sellerHomeFragmentBinding.pieChart.setDragDecelerationFrictionCoef(0.95f)
        sellerHomeFragmentBinding.pieChart.isDrawHoleEnabled = true
        sellerHomeFragmentBinding.pieChart.setHoleColor(Color.WHITE)
        sellerHomeFragmentBinding.pieChart.setTransparentCircleColor(Color.WHITE)
        sellerHomeFragmentBinding.pieChart.setTransparentCircleAlpha(110) // Adjusted alpha to be within valid range
        sellerHomeFragmentBinding.pieChart.holeRadius = 70f
        sellerHomeFragmentBinding.pieChart.transparentCircleRadius = 61f
        sellerHomeFragmentBinding.pieChart.legend.isEnabled = false
        sellerHomeFragmentBinding.pieChart.setEntryLabelColor(Color.BLACK)
        sellerHomeFragmentBinding.pieChart.setEntryLabelTextSize(12f)
        sellerHomeFragmentBinding.pieChart.setUsePercentValues(true)


        val entries: ArrayList<PieEntry> = ArrayList()
        val vegetablePosition = newDataPie?.totalPercentageOfCategories?.indexOfFirst { it.categoryId == VEGETABLES }
                ?: -1
        val fruitsPosition = newDataPie?.totalPercentageOfCategories?.indexOfFirst { it.categoryId == FRUITS } ?: -1
        val datesPosition = newDataPie?.totalPercentageOfCategories?.indexOfFirst { it.categoryId == DATES } ?: -1

        val colors: ArrayList<Int> = ArrayList()
        if (vegetablePosition != -1) {
            val percentage =
                newDataPie?.totalPercentageOfCategories?.getOrNull(vegetablePosition)?.totalPercentage?.toFloat() ?: 0f
            val pieEntry = PieEntry(percentage, "Vegetables")
            entries.add(pieEntry)
            colors.add(ContextCompat.getColor(mContext, R.color.greenhousetheme))
        }
        if (fruitsPosition != -1) {
            val percentage = newDataPie?.totalPercentageOfCategories?.getOrNull(fruitsPosition)?.totalPercentage?.toFloat() ?: 0f
            val pieEntry = PieEntry(percentage, "Fruits")
            entries.add(pieEntry)
            colors.add(ContextCompat.getColor(mContext, R.color.light_purple))

        }
        if (datesPosition != -1) {
            val percentage = newDataPie?.totalPercentageOfCategories?.getOrNull(datesPosition)?.totalPercentage?.toFloat()
                    ?: 0f
            val pieEntry = PieEntry(percentage, "Dates")
            entries.add(pieEntry)
            colors.add(ContextCompat.getColor(mContext, R.color.light_red))
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.setDrawIcons(false)
        dataSet.iconsOffset = MPPointF(0f, 30f)
        dataSet.colors = colors
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1Length = 0.3f
        dataSet.valueLinePart2Length = 0.3f
        dataSet.valueLineColor = ContextCompat.getColor(mContext,R.color.black)
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(sellerHomeFragmentBinding.pieChart))
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT)
        data.setValueTextColor(ContextCompat.getColor(mContext, R.color.black))
        sellerHomeFragmentBinding.pieChart.data = data
        sellerHomeFragmentBinding.pieChart.highlightValues(null)
        sellerHomeFragmentBinding.pieChart.invalidate()
    }

    private fun revenueChart(newData: SellerHomeGraphResponseModel.Data.NewData?) {
        sellerHomeFragmentBinding.revenueChartData.fitScreen()
        sellerHomeFragmentBinding.revenueChartData.setBackgroundColor(Color.WHITE)
        sellerHomeFragmentBinding.revenueChartData.isHighlightFullBarEnabled = false
        sellerHomeFragmentBinding.revenueChartData.setDrawGridBackground(false)
        sellerHomeFragmentBinding.revenueChartData.setDrawBarShadow(false)
        sellerHomeFragmentBinding.revenueChartData.setDrawValueAboveBar(false)
        val l: Legend = sellerHomeFragmentBinding.revenueChartData.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.isEnabled = false
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis: YAxis = sellerHomeFragmentBinding.revenueChartData.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.setAxisMinimum(0f) // this replaces setStartAtZero(true)
        rightAxis.axisLineColor = Color.BLACK
        rightAxis.axisLineWidth = 1.0f
        rightAxis.setCenterAxisLabels(false)
        rightAxis.isEnabled = false

        val leftAxis: YAxis = sellerHomeFragmentBinding.revenueChartData.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.setAxisMinimum(0f) // this replaces setStartAtZero(true)
        leftAxis.axisLineColor = Color.BLACK
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(5.0f, 5.0f, 5.0f)
        sellerHomeFragmentBinding.revenueChartData.marker =
            CustomMarkerView(mContext, R.layout.custom_marker_layout)
        val xAxis: XAxis = sellerHomeFragmentBinding.revenueChartData.xAxis
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 11f
        xAxis.labelCount = newData?.revenue?.size ?: 0
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        sellerHomeFragmentBinding.revenueChartData.invalidate()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        sellerHomeFragmentBinding.revenueChartData.setDrawGridBackground(false)
        sellerHomeFragmentBinding.revenueChartData.description.isEnabled = false
        sellerHomeFragmentBinding.revenueChartData.legend.isEnabled = false
        val monthList: MutableList<String> = mutableListOf()
        newData?.revenue?.forEachIndexed { _, revenue ->
            monthList.add(getShortMonthName(revenue.month) ?: "")
        }

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                Log.d(
                    "value", "" + value.toInt()
                )
                var axisValue = ""
                try {
                    axisValue = monthList.getOrNull(value.toInt()).toString()
                } catch (e: java.lang.Exception) {
                    Log.d("TAG", "getAxisLabel: ${e.printStackTrace()}")
                }
                Log.d("TAG", "axisValue: ${Gson().toJson(axisValue)}")
                return axisValue
            }
        }
        val data = CombinedData()
        data.setData(generateLineData(newData, "2", null, null, null))
        xAxis.setAxisMaximum(data.xMax + 0.5f)
        sellerHomeFragmentBinding.revenueChartData.setVisibility(true)
        sellerHomeFragmentBinding.revenueChartData.setDrawBarShadow(true)
        sellerHomeFragmentBinding.revenueChartData
        sellerHomeFragmentBinding.revenueChartData.setData(data)
        sellerHomeFragmentBinding.revenueChartData.invalidate()
    }

    private fun getVegPosition(itemList: List<SellerHomeGraphResponseModel.Data.NewData.Revenue.Category>): Int {
        return itemList.indexOfFirst { it.categoryId == VEGETABLES }
    }

    private fun getFruitsPosition(itemList: List<SellerHomeGraphResponseModel.Data.NewData.Revenue.Category>): Int {
        return itemList.indexOfFirst { it.categoryId == FRUITS }
    }

    private fun getDatesPosition(itemList: List<SellerHomeGraphResponseModel.Data.NewData.Revenue.Category>): Int {
        return itemList.indexOfFirst { it.categoryId == DATES }
    }

    private fun bookingChart(newData: SellerHomeGraphResponseModel.Data.NewData?) {
        sellerHomeFragmentBinding.apply {
            val lineChart = sellerHomeFragmentBinding.bookingChart
            val monthList: MutableList<String> = mutableListOf()
            newData?.revenue?.forEachIndexed { _, revenue ->
                monthList.add(getShortMonthName(revenue.month) ?: "")
            }
            lineChart.description.isEnabled = false
            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.axisMaximum = (newData?.revenue?.size ?: 0).toFloat()
            xAxis.labelCount = newData?.revenue?.size ?: 0
            xAxis.axisMinimum = 0f
            xAxis.granularity = 1f
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase): String {
                    return monthList.getOrNull(value.toInt()) ?: ""
                }
            }

            val leftAxis: YAxis = lineChart.axisLeft
            leftAxis.setDrawGridLines(false)
            leftAxis.setAxisMinimum(0f) // this replaces setStartAtZero(true)
            leftAxis.axisLineColor = Color.BLACK
            leftAxis.axisLineWidth = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.enableGridDashedLine(5.0f, 5.0f, 5.0f)

            val rightAxis: YAxis = lineChart.axisRight
            rightAxis.isEnabled = false


            val values1 = mutableListOf<Entry>()
            val maxSize1 = newData?.revenue?.size ?: 0

            for (i in 0 until maxSize1) {
                val position =
                    getVegPosition(newData?.revenue?.getOrNull(i)?.categories ?: listOf())
                Log.d("TAG", "vegPosition: $position")
                if (position != -1) {
                    values1.add(
                        Entry(i.toFloat(),
                            (newData?.revenue?.getOrNull(i)?.categories?.getOrNull(position)?.totalOrders
                                ?: 0f).toFloat()
                        )
                    )
                } else {
                    values1.add(Entry(i.toFloat(), 0f))
                }
            }
            val lineDataSet1 = LineDataSet(values1, "Vegetable")
            lineDataSet1.color = ContextCompat.getColor(mContext, R.color.greenhousetheme)
            lineDataSet1.valueTextColor = ContextCompat.getColor(mContext, R.color.greenhousetheme)
            lineDataSet1.setDrawValues(false)
            lineDataSet1.setLineWidth(3f)
            lineDataSet1.circleRadius = 0f
            // Second dataset
            val values2 = mutableListOf<Entry>()
            val maxSize2 = newData?.revenue?.size ?: 0
            for (i in 0 until maxSize2) {
                val position =
                    getFruitsPosition(newData?.revenue?.getOrNull(i)?.categories ?: listOf())
                Log.d("TAG", "fruitPosition: $position")
                if (position != -1) {
                    values2.add(
                        Entry(i.toFloat(),
                            (newData?.revenue?.getOrNull(i)?.categories?.getOrNull(position)?.totalOrders
                                ?: 0f).toFloat()
                        )
                    )
                } else {
                    values2.add(Entry(i.toFloat(), 0f))
                }
            }

            val lineDataSet2 = LineDataSet(values2, "Fruits")
            lineDataSet2.color = ContextCompat.getColor(mContext, R.color.light_purple)
            lineDataSet2.valueTextColor = ContextCompat.getColor(mContext, R.color.light_purple)
            lineDataSet2.mode = LineDataSet.Mode.CUBIC_BEZIER
            lineDataSet2.setDrawValues(false)
            lineDataSet2.setLineWidth(3f)
            lineDataSet2.circleRadius = 0f

            val values3 = mutableListOf<Entry>()
            val maxSize = newData?.revenue?.size ?: 0
            for (i in 0 until maxSize) {
                val position =
                    getDatesPosition(newData?.revenue?.getOrNull(i)?.categories ?: listOf())
                Log.d("TAG", "datesPosition: $position")
                if (position != -1) {
                    values3.add(
                        Entry(
                            i.toFloat(),
                            (newData?.revenue?.getOrNull(i)?.categories?.getOrNull(position)?.totalOrders
                                ?: 0f).toFloat()
                        )
                    )
                } else {
                    values3.add(Entry(i.toFloat(), 0f))
                }
            }

            val lineDataSet3 = LineDataSet(values3, "Dates")
            lineDataSet3.color = ContextCompat.getColor(mContext, R.color.light_red)
            lineDataSet3.valueTextColor = ContextCompat.getColor(mContext, R.color.light_red)
            lineDataSet3.mode = LineDataSet.Mode.CUBIC_BEZIER
            lineDataSet3.setDrawValues(false)
            lineDataSet3.setLineWidth(3f)
            lineDataSet3.circleRadius = 0f

            // Add datasets to the chart
            val data = LineData(lineDataSet1, lineDataSet2, lineDataSet3)
            bookingChart.data = data

            // Customize chart
            bookingChart.setBackgroundColor(Color.WHITE)
            bookingChart.animateX(2000)

            // Customize legend
            val legend = bookingChart.legend
            legend.form = Legend.LegendForm.LINE
            legend.textColor = Color.BLACK

            // Refresh the chart
            bookingChart.invalidate()

            val markerView = CustomMarkerView(mContext, R.layout.custom_marker_layout)
            lineChart.marker = markerView
        }
    }

    private fun generateLineData(
        dataModel: SellerHomeGraphResponseModel.Data.NewData?,
        type: String,
        vegetableDataSet: LineDataSet?,
        fruitsDataSet: LineDataSet?,
        datesDataSet: LineDataSet?
    ): LineData {
        val d = LineData()
        if (type == "1") {
            d.addDataSet(fruitsDataSet)
        } else {
            val values1 = mutableListOf<Entry>()
            val maxSize = dataModel?.revenue?.size ?: 0
            for (i in 0 until maxSize) {
                values1.add(
                    Entry(
                        i.toFloat(),
                        (dataModel?.revenue?.getOrNull(i)?.categories?.getOrNull(0)?.totalAmount
                            ?: 0f).toFloat()
                    )
                )
            }
            Log.e("TAG", "generateLineData: " + Gson().toJson(values1))

            val set = LineDataSet(values1, "")
            set.setColor(ContextCompat.getColor(mContext, R.color.greenhousetheme))
            set.setLineWidth(3f)
            set.circleRadius = 0f
            set.mode = LineDataSet.Mode.CUBIC_BEZIER
            set.valueTextSize = 20f
            set.disableDashedLine()
            set.setDrawValues(false)
            set.axisDependency = YAxis.AxisDependency.LEFT
            d.addDataSet(set)

            sellerHomeFragmentBinding.revenueChartData.setOnChartValueSelectedListener(object :
                OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // Clear previous marker if any
                    sellerHomeFragmentBinding.revenueChartData.marker = null
                    // If a month is selected
                    e?.let {
                        // Add a straight line to the chart
//                        addVerticalLine(sellerHomeFragmentBinding.revenueChartData, it.x, it.y)
                        // Show data above the selected month
                        showDataAboveMonth(dataModel, it.x)
                    }

                }

                override fun onNothingSelected() {
                    // sellerHomeFragmentBinding.revenueChartData.marker = null
                }
            })
        }
        return d
    }

    private fun showDataAboveMonth(
        newData: SellerHomeGraphResponseModel.Data.NewData?, xValue: Float
    ) {
        val monthIndex = xValue.toInt()
        val monthData = newData?.revenue?.getOrNull(monthIndex)
        monthData?.let {
            val markerView = CustomMarkerView(mContext, R.layout.custom_marker_layout)
            markerView.setData(it.categories.getOrNull(0)?.totalAmount?.toDouble().toString())
            sellerHomeFragmentBinding.revenueChartData.marker = markerView
        }
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    mContext, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    CommonUtils.showSettingDialog(mContext, askedPermission = "Notification")
                }

                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("TAG", "checkNotificationPermission: ")
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast("Notification permission granted")
            } else {
                CommonUtils.showSettingDialog(mContext, askedPermission = "Notification")
            }
        }
}
