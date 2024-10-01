package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentAuctionTimeBinding
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.product_details.Product
import com.ripenapps.greenhouse.model.seller.response.ProductEditResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.isStartDateStringBeforeCurrentDate
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.DATE_FORMAT
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.showToast
import com.ripenapps.greenhouse.view_models.ProductEditViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class AuctionTime(
    private val productDetails: Product? = null,
    private val isFromEdit: Boolean? = false,
    private val productId: String? = null,
    private val isFromOrder: Boolean? = false,
    private val successCallBack: () -> Unit,
    private val isFinish: Boolean? = false
) : BottomSheetDialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var auctionTimeBinding: FragmentAuctionTimeBinding
    private val calendar = Calendar.getInstance()
    private val endDateCalendar = Calendar.getInstance()
    private lateinit var mcontext: Context
    private var productEditViewModel: ProductEditViewModel? = null
    var accountBlocked: AccountBlocked? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        auctionTimeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_auction_time, container, false)
        if (isFromEdit == true) {
            val isButtonEnabled = isStartDateStringBeforeCurrentDate(productDetails?.endDate ?: "")

            auctionTimeBinding.startDateText.isEnabled = isButtonEnabled
            if (!isButtonEnabled) {
                auctionTimeBinding.startDateText.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        mcontext, R.color.light_gray
                    )
                )
            }
        } else if (isFromOrder == true) {
            auctionTimeBinding.startDateText.isEnabled = true

        } else {
            auctionTimeBinding.startDateText.isEnabled = true
            auctionTimeBinding.startDateText.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    mcontext, R.color.white
                )
            )
        }

        isCancelable = true

        Log.d("TAG", "productDetailsCheck: $productDetails")

        Log.d("TAG", "onCreateView: $productDetails")
        auctionTimeBinding.apply {
            val startDate = convertDateString(productDetails?.startDate ?: "") ?: ""
            startDateText.text = startDate.ifEmpty {
                "Start Date"
            }
            val endDate = convertDateString(productDetails?.endDate ?: "")?:""
            endDateText.text = endDate.ifEmpty {
                "End Date"
            }

            startTimeText.text = (productDetails?.startTime ?: "".ifEmpty {
                    "Start Time"
            })


            endTimeText.text = productDetails?.endTime ?: "".ifEmpty {
                "End Time"
            }

        }
        initListeners()
        initViewModel()
        return auctionTimeBinding.root
    }

    private fun initViewModel() {
        productEditViewModel = ViewModelProvider(this)[ProductEditViewModel::class.java]
        auctionTimeBinding.lifecycleOwner = this
        productEditViewModel?.getProductEditData()?.observe(viewLifecycleOwner) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ProductEditResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                successCallBack.invoke()
                                ProcessDialog.dismissDialog()
                                if (isFinish == true) {
                                    activity?.finish()
                                }
                                showToast(data.message ?: "")
                                dismiss()
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
                                val signin = Intent(mcontext, SignIn::class.java)
                                startActivity(signin)
                                activity?.finishAffinity()
                            }

                            422 -> {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT)
                                    .show()
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

    @SuppressLint("ResourceAsColor")
    private fun showDatePickerDialog() {
        val startDateMillis = calendar.timeInMillis
        val endDateMillis =
            startDateMillis + TimeUnit.DAYS.toMillis(30) // 30 days after the start date

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateStartDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set minimum date as the current date
        datePickerDialog.datePicker.minDate = startDateMillis
        datePickerDialog.datePicker.maxDate =
            endDateMillis // Set maximum date as 30 days after the start date
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

    }

    @SuppressLint("SimpleDateFormat")
    private fun showDatePickerDialog2() {
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        Log.d("TAG", "showDatePickerDialog2: ${CommonUtils.getCurrentDateInUTCString()}")
        val startDate =
            dateFormat.parse(productDetails?.endDate ?: CommonUtils.getCurrentDateInUTCString())
        println("startDate $startDate")
        if (startDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            Log.d(
                "TAG",
                "showDatePickerDialog2: ${calendar.get(Calendar.YEAR)} ${calendar.get(Calendar.MONTH)}"
            )
        }
        val startDateMillis = calendar.timeInMillis
        val endDateMillis =
            startDateMillis + TimeUnit.DAYS.toMillis(30) // 30 days after the start date

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                endDateCalendar.set(Calendar.YEAR, year)
                endDateCalendar.set(Calendar.MONTH, monthOfYear)
                endDateCalendar.set(
                    Calendar.DAY_OF_MONTH, dayOfMonth
                )
                updateExpiryDate()
            },
            endDateCalendar.get(Calendar.YEAR),
            endDateCalendar.get(Calendar.MONTH),
            endDateCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date as the current date
        datePickerDialog.datePicker.minDate = startDateMillis
        datePickerDialog.datePicker.maxDate =
            endDateMillis // Set maximum date as 30 days after the start date
        datePickerDialog.show()
    }

    private fun updateExpiryDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selectedDate = dateFormat.format(endDateCalendar.time)
        auctionTimeBinding.endDateText.text = selectedDate
    }

    private fun updateStartDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selectedDate = dateFormat.format(calendar.time)
        auctionTimeBinding.startDateText.text = selectedDate
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                val formattedHour = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                auctionTimeBinding.startTimeText.text = formattedHour
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }


    private fun showTimePickerDialog2() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                val formattedHour = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                auctionTimeBinding.endTimeText.text = formattedHour
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val formattedHour = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
        auctionTimeBinding.startTimeText.text = formattedHour
    }

    fun initListeners() {
        auctionTimeBinding.startDateText.setOnClickListener {
            showDatePickerDialog()
        }
        auctionTimeBinding.endDateText.setOnClickListener {
            showDatePickerDialog2()
        }
        auctionTimeBinding.startTimeText.setOnClickListener {
            showTimePickerDialog()
        }
        auctionTimeBinding.endTimeText.setOnClickListener {
            showTimePickerDialog2()
        }
        auctionTimeBinding.btnUpdate.setOnClickListener {
            if (isFromOrder == true) {
                productEditViewModel?.request?.productId = productId
            } else {
                productEditViewModel?.request?.productId = productDetails?.id ?: ""
            }
//            Log.d("TAG", "initListeners: ${productDetail.id}")
            productEditViewModel?.request?.token = Preferences.getStringPreference(mcontext, TOKEN)
            productEditViewModel?.request?.startTime =
                auctionTimeBinding.startTimeText.text.toString()
            productEditViewModel?.request?.endTime = auctionTimeBinding.endTimeText.text.toString()
            productEditViewModel?.request?.startDate =
                auctionTimeBinding.startDateText.text.toString()
            productEditViewModel?.request?.endDate = auctionTimeBinding.endDateText.text.toString()
            productEditViewModel?.request?.country = TimeZone.getDefault().id
            productEditViewModel?.getProductEditRequest()

        }
    }

    private fun convertDateString(inputDateString: String): String? {
        if (inputDateString.isEmpty()) return ""
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(inputDateString)
        return date?.let { outputFormat.format(it) }
    }

}
