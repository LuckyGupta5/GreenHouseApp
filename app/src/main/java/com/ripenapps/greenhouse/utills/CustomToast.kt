package com.ripenapps.greenhouse.utills

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.ripenapps.greenhouse.R

class CustomToast(context: Context?) : Toast(context) {
    @Suppress("DEPRECATION")
    companion object {
        @SuppressLint("MissingInflatedId", "InflateParams")
        fun makeText(context: Context, text: CharSequence?, duration: Int): Toast {
            val customToast = Toast.makeText(context, text, duration)
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.custom_toast, null)
            val textView: TextView = layout.findViewById(R.id.idToastText)
            textView.text = text
            customToast.view = layout
            return customToast
        }
    }
}