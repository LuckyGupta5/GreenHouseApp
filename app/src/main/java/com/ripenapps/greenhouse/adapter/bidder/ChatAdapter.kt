package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.ChatLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatModel
import com.ripenapps.greenhouse.utills.CommonUtils.sendMessageTime
import com.ripenapps.greenhouse.utills.setImage
import com.ripenapps.greenhouse.utills.setVisibility

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    val messages: ArrayList<ChatModel.Result> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun updateChatList(messageList: MutableList<ChatModel.Result>) {
        messages.clear()
        messages.addAll(messageList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.chat_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        messages.getOrNull(position)?.let { dataModel -> holder.bind(dataModel, position) }
    }

    inner class ViewHolder(private val binding: ChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatModel.Result, position: Int) {
            binding.apply {
                chatModel = data
                if (position > 0) {
                    chatDate.isVisible = messages.getOrNull(position)
                        ?.formattedDate() != messages.getOrNull(position - 1)
                        ?.formattedDate()
                } else {
                    chatDate.isVisible = true
                }
                if (messages.getOrNull(position)?.senderType == "user") {
                    userSideMessages.visibility = View.GONE
                    mySideMessages.setVisibility(messages.getOrNull(position)?.message?.isNotEmpty() == true)
//                    imageBackground.setVisibility(true)
//                    images.setVisibility(true)
                    imagesSender.setVisibility(false)
                    imageBackground2.setVisibility(false)
                    images.setVisibility(messages.getOrNull(position)?.images?.isNotEmpty() == true)
                    imageBackground.setVisibility(messages.getOrNull(position)?.images?.isNotEmpty() == true)
                    images.setImage(messages.getOrNull(position)?.images?.getOrNull(0).toString())
                    userSideTime.setVisibility(false)
                    mySideTime.text = sendMessageTime((data.createdAt ?: "").toString())
                } else {
                    mySideMessages.visibility = View.GONE
                    userSideMessages.setVisibility(messages.getOrNull(position)?.message?.isNotEmpty() == true)
                    mySideTime.setVisibility(false)
                    images.setVisibility(false)
                    imageBackground.setVisibility(false)
                    imagesSender.setVisibility(messages.getOrNull(position)?.images?.isNotEmpty() == true)
                    imageBackground2.setVisibility(messages.getOrNull(position)?.images?.isNotEmpty() == true)
                    //for multiple images
                    Gson().toJson(messages.getOrNull(position)?.images)
                    messages.getOrNull(position)?.images?.forEach { imageUrl ->
                        imagesSender.setImage(imageUrl)
                    }
                    userSideTime.text = sendMessageTime(data.createdAt.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}