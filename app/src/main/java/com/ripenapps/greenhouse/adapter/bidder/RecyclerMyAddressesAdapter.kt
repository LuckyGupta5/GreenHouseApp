package com.ripenapps.greenhouse.adapter.bidder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.greenhouse.databinding.MyAddressesLayoutBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.MyAddressesModel

class RecyclerMyAddressesAdapter(private val contextAddresses: Context) :
    RecyclerView.Adapter<RecyclerMyAddressesAdapter.ViewHolder>() {

    private lateinit var mListner: onClickListner

    interface onClickListner {
        fun onEdit(position: Int)
        fun onDelete(position: Int)
    }


    fun onClickListner(listner: onClickListner) {
        mListner = listner
    }

    private var arrAddress = mutableListOf<MyAddressesModel>()
    @SuppressLint("NotifyDataSetChanged")
    fun addAddress(listOfAddress: MutableList<MyAddressesModel>) {
        arrAddress = listOfAddress
        notifyDataSetChanged()
    }
    class ViewHolder(
        private val addressBinding: MyAddressesLayoutBinding, private val listner: onClickListner
    ) : RecyclerView.ViewHolder(addressBinding.root) {
        fun bind(data: MyAddressesModel) {
            addressBinding.apply {
                addressModel = data
                btnEditAddress.setOnClickListener {
                    listner.onEdit(adapterPosition)
                }
                idDeleteBtn.setOnClickListener {
                    listner.onDelete(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MyAddressesLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), mListner
        )
    }

    override fun getItemCount(): Int {
        return arrAddress.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrAddress.getOrNull(position)?.let { dataModel -> holder.bind(dataModel) }
    }
}