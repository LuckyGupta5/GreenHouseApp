package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FaqTypesLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FaqQuestionTypeModel
import com.ripenapps.greenhouse.utills.setEndMargin
import com.ripenapps.greenhouse.utills.setStartMargin

class RecyclerFaqQuesAdapter(
    private val arrQuestionType: ArrayList<FaqQuestionTypeModel>
) : RecyclerView.Adapter<RecyclerFaqQuesAdapter.ViewHolder>() {
    inner class ViewHolder(private val faqTypeBinding: FaqTypesLayoutBinding) :
        RecyclerView.ViewHolder(faqTypeBinding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: FaqQuestionTypeModel) {
            faqTypeBinding.apply {
                questionModel = data
                this.backgroundColor.setBackgroundColor(
                    getColorFromResourceId(
                        faqTypeBinding.root.context, data.boxBackground
                    )
                )
                this.typeImage.setImageDrawable(
                    faqTypeBinding.root.context.resources.getDrawable(
                        data.typesImage
                    )
                )


                if (absoluteAdapterPosition == 0) {
                    faqBox.setStartMargin(R.dimen.dimen_15)
                    faqBox.setEndMargin(R.dimen.dimen_15)
                } else {
                    faqBox.setEndMargin(R.dimen.dimen_15)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FaqTypesLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrQuestionType.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrQuestionType.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }

    fun getColorFromResourceId(context: Context, colorResourceId: Int): Int {
        return context.resources.getColor(colorResourceId)
    }
}