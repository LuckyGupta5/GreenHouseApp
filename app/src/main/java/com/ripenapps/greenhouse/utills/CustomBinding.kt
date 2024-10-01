package com.ripenapps.greenhouse.utills

import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.textview.MaterialTextView

object CustomBinding {

    @JvmStatic
    @BindingAdapter("app:getInitials")
    fun getInitials(view: View, input: String) {
        val names = input.split(" ") // Split the input string into individual words
        val initials = StringBuilder() // StringBuilder to store the initials

        // Iterate through the first two words
        for (i in 0 until minOf(names.size, 2)) {
            // Append the first character of each word to the initials StringBuilder
            if (names[i].isNotEmpty()) {
                initials.append(names[i][0].uppercaseChar())
            }
        }
        Log.d("TAG", "getInitials: " + initials)
        (view as MaterialTextView).text = initials
//        return initials.toString() // Return the concatenated initials
    }
}