package com.ripenapps.greenhouse.fragment.bidderfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentHelpTopicSheetBinding
import com.ripenapps.greenhouse.datamodels.FilterModel

class HelpTopicSheet(
    private val model: FilterModel? = null, private val applyCallBack: (FilterModel?) -> (Unit)
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentHelpTopicSheetBinding
    private var filterModel: FilterModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_help_topic_sheet, container, false)

        filterModel = model ?: FilterModel()

        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.apply {
            option1.setOnClickListener {
                filterModel?.helpTopic = option1.text.toString()
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            option2.setOnClickListener {
                filterModel?.helpTopic = option2.text.toString()
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            option3.setOnClickListener {
                filterModel?.helpTopic = option3.text.toString()
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            option4.setOnClickListener {
                filterModel?.helpTopic = option4.text.toString()
                applyCallBack.invoke(filterModel)
                dismiss()
            }

            option5.setOnClickListener {
                filterModel?.helpTopic = option5.text.toString()
                applyCallBack.invoke(filterModel)
                dismiss()
            }
        }
    }
}