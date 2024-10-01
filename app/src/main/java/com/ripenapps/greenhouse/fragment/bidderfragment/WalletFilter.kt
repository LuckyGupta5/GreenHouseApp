package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.databinding.FragmentWalletFilterBinding
import com.ripenapps.greenhouse.utills.setVisibility

class WalletFilter(
    private val model: FilterModel? = null,
    private val isFromGraph: Boolean? = false,
    private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var walletFilterBinding: FragmentWalletFilterBinding
    private var filterModel: FilterModel? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        walletFilterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_filter, container, false)
        initListeners()
        filterModel = model ?: FilterModel()
        chooseFilter()
        return walletFilterBinding.root
    }

    private fun chooseFilter() {
        walletFilterBinding.apply {
            btnWallet.setVisibility(isFromGraph ?: false)
            when (filterModel?.transactionType?.lowercase()) {
                "dates" -> {
                    datesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.white_tick, 0)
                    vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    walletText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                "wallet" -> {
                    walletText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.white_tick, 0)
                    vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    datesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                "vegetables" -> {
                    vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.white_tick, 0)
                    datesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    walletText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                "fruits" -> {
                    fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    allText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.white_tick, 0)
                    vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    datesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    walletText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }

                else -> {
                    allText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_green_tick_filter, 0
                    )
                    datesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                    walletText.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.white_tick, 0
                    )
                }
            }
        }
    }

    private fun initListeners() {
        walletFilterBinding.apply {
            btnAll.setOnClickListener {
                allText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.transactionType = null
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            btnDates.setOnClickListener {
                datesText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.transactionType = "dates"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            btnWallet.setOnClickListener {
                walletText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.transactionType = "wallet"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            btnFruits.setOnClickListener {
                fruitesText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.transactionType = "fruits"
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            btnVegetables.setOnClickListener {
                vegetableText.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_green_tick_filter, 0
                )
                filterModel?.transactionType = "vegetables"
                applyCallBack.invoke(filterModel)
                dismiss()
            }
        }
    }
}