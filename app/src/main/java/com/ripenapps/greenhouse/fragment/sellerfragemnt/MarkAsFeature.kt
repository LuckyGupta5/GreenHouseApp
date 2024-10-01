package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.RecyclerDateAdapter
import com.ripenapps.greenhouse.databinding.FragmentMarkAsFeatureBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.DateClass
import com.ripenapps.greenhouse.screen.seller.FeaturedBidDetailsPlacedBid
import com.ripenapps.greenhouse.utills.CommonUtils.convertToLocalDate
import com.ripenapps.greenhouse.utills.CommonUtils.getCurrentDateInUTCString
import com.ripenapps.greenhouse.utills.CommonUtils.getDatesBetween
import com.ripenapps.greenhouse.utills.CommonUtils.isStartDateStringBeforeCurrentDate
import com.ripenapps.greenhouse.utills.Constants.PRODUCT_ID
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import java.time.format.TextStyle
import java.util.Locale

class MarkAsFeature(
    val productId: String?, val subCategoryId: String?, val endDate: String?, val startDate: String?
) : BottomSheetDialogFragment() {
    private lateinit var markAsFeatureBinding: FragmentMarkAsFeatureBinding
    private val arrCalender: ArrayList<DateClass> = arrayListOf()
    private var recyclerCalender: RecyclerDateAdapter? = null
    private var dateSelected: String? = null
    private lateinit var mContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        markAsFeatureBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_mark_as_feature, container, false)
        initListeners()
        initAdapter()
        return markAsFeatureBinding.root
    }

    private fun initAdapter() {
        if (startDate?.isEmpty() == true && endDate?.isEmpty() == true) {
            return
        }

        Log.d("TAG", "dateAdapter: $startDate $endDate")
        val currentDate = if (isStartDateStringBeforeCurrentDate(startDate ?: "")) {
            getCurrentDateInUTCString()
        } else {
            startDate
        }

        val dateArr = getDatesBetween(
            convertToLocalDate(currentDate ?: ""), convertToLocalDate(endDate ?: "")
        )
        dateArr.forEach { localDate ->
            arrCalender.add(
                DateClass(
                    day = localDate.dayOfMonth.toString(),
                    weekDay = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    month = localDate.month.toString(),
                    date = localDate.toString(),
                    isSelect = false
                )
            )
        }
        if (arrCalender.size > 1) {
            recyclerCalender = RecyclerDateAdapter(arrCalender,
                mContext,
                object : RecyclerDateAdapter.OnDateSelected {
                    override fun onDateClick(
                        date: String,
                        isCurrentDate: Boolean,
                        fullDate: String,
                        position: Int,
                        isSelected: Boolean
                    ) {
                        dateSelected = if (isSelected) {
                            Log.d("TAG", "onDateClick: $fullDate")
                            fullDate//24 Mar, 2024 //yyyy-mm-dd}
                        } else {
                            ""
                        }
                    }
                })


            markAsFeatureBinding.recycler.adapter = recyclerCalender
        } else {
            markAsFeatureBinding.recycler.setVisibility(false)
            markAsFeatureBinding.proceedToBid.setVisibility(false)
            markAsFeatureBinding.dateText.setVisibility(false)
            markAsFeatureBinding.text.text = getString(R.string.cannot_feature)
        }

    }

    private fun initListeners() {
        markAsFeatureBinding.proceedToBid.setOnClickListener {
            if (dateSelected.isNullOrEmpty()) {
                showToast("please Select Date ")
                return@setOnClickListener
            }
            val intent = Intent(mContext, FeaturedBidDetailsPlacedBid::class.java)
            intent.putExtra("biddingDate", dateSelected)
            intent.putExtra(PRODUCT_ID, productId)
            startActivity(intent)
            dismiss()
        }
    }
}