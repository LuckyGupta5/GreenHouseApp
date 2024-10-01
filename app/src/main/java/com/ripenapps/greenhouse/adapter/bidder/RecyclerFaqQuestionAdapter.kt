package com.ripenapps.greenhouse.adapter.bidder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FaqQuestionsLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.FaqQuestionModel
import com.ripenapps.greenhouse.utills.setVisibility

class RecyclerFaqQuestionAdapter(
    private val comingFrom: String,
    private val arrQuestion: ArrayList<FaqQuestionModel>,
    private val callBack: (FaqQuestionModel) -> Unit,
    private val status: Boolean? = false
) : RecyclerView.Adapter<RecyclerFaqQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: FaqQuestionsLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FaqQuestionModel) {
            binding.apply {
                faqQuestionModel = data
                if (data.isItemVisible) {
                    answer.setVisibility(true)
                    plus1.setImageResource(R.drawable.ic_minus)
                    chatButton.setVisibility(comingFrom.isNotEmpty() && status == false)
                } else {
                    answer.setVisibility(false)
                    plus1.setImageResource(R.drawable.ic_plus)
                }

                plus1.setOnClickListener {
                    data.isItemVisible = !(data.isItemVisible)
                    notifyItemChanged(absoluteAdapterPosition)
                }

                chatButton.setOnClickListener {
                    callBack.invoke(data)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FaqQuestionsLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrQuestion.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrQuestion.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}