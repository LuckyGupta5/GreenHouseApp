package com.ripenapps.greenhouse.utills

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.greenhouse.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun View.setEndMargin(endMargin: Int = R.dimen.dimen_15) {
    val marginValue = context.resources.getDimensionPixelSize(endMargin)

    when (val params = this.layoutParams) {
        is ConstraintLayout.LayoutParams -> params.marginEnd = marginValue
        is FrameLayout.LayoutParams -> params.marginEnd = marginValue
        is RecyclerView.LayoutParams -> params.marginEnd = marginValue
    }
}

fun View.setWidthAccordingToScreen(layoutSizePercent: Int) {
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val itemWidth =
        screenWidth * layoutSizePercent / 100 // Calculate item width as 80% of screen width
    val params = this.layoutParams
    params.width = itemWidth
    this.layoutParams = params
}

fun String.convertToDateFormat(fromFormat: String, toFormat: String): String? {
    val inputFormatter = SimpleDateFormat(fromFormat, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val outputFormatter = SimpleDateFormat(toFormat, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    return try {
        val date = inputFormatter.parse(this)
        date?.let { outputFormatter.format(it) }
    } catch (e: Exception) {
        null
    }
}
fun View.isButtonEnable(enabled: Boolean) {
    if (enabled) {
        this.isEnabled = true
        this.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this.context,
                R.color.greenhousetheme
            )
        )
    } else {
        this.isEnabled = false
        this.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this.context,
                R.color.light_gray
            )
        )
    }
}
fun Double.formatToReadableNumber(): String {
    return when {
        this >= 1_000_000_000_000_000 -> String.format("%.1fQ", this / 1_000_000_000_000_000.0)
        this >= 1_000_000_000_000 -> String.format("%.1fT", this / 1_000_000_000_000.0)
        this >= 1_000_000_000 -> String.format("%.1fB", this / 1_000_000_000.0)
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        this >= 100 -> String.format("%.2f", this)  // Format hundreds with two decimal places
        else -> String.format("%.2f", this)  // Format all other numbers with two decimal places
    }.also { str ->
        Log.d("TAG", "formatToReadableNumber: $str")
    }
}


fun ImageView.setImage(url: String?, placeHolder: Int = R.drawable.image_placeholder_new) {
    if (url == null) {
        this.setImageResource(placeHolder)
    } else {
        Glide.with(this).load(url).placeholder(placeHolder).into(this)
    }
}

fun View.setPaddingEnd(endPadding: Int = R.dimen.dimen_15) {
    val paddingValue = context.resources.getDimensionPixelSize(endPadding)
    val paddingStartvalue = context.resources.getDimensionPixelSize(R.dimen.dimen_20)
    this.setPadding(paddingStartvalue, 0, paddingValue, 0)
}


fun View.setTopMargin(topMargin: Int = R.dimen.dimen10dp, isResourceValue: Boolean = true) {
    val marginValue = if (isResourceValue) {
        context.resources.getDimensionPixelSize(topMargin)
    } else {
        topMargin
    }
    when (val params = this.layoutParams) {
        is ConstraintLayout.LayoutParams -> params.topMargin = marginValue
        is FrameLayout.LayoutParams -> params.topMargin = marginValue
        is RecyclerView.LayoutParams -> params.topMargin = marginValue
    }
}

fun View.setBottomMargin(topMargin: Int = R.dimen.dimen10dp) {
    val marginValue = context.resources.getDimensionPixelSize(topMargin)

    when (val params = this.layoutParams) {
        is ConstraintLayout.LayoutParams -> params.bottomMargin = marginValue
        is FrameLayout.LayoutParams -> params.bottomMargin = marginValue
        is RecyclerView.LayoutParams -> params.bottomMargin = marginValue
    }
}

fun View.setStartMargin(topMargin: Int = R.dimen.dimen_0) {
    val marginValue = context.resources.getDimensionPixelSize(topMargin)

    when (val params = this.layoutParams) {
        is ViewGroup.MarginLayoutParams -> params.marginStart = marginValue
        is ConstraintLayout.LayoutParams -> params.marginStart = marginValue
        is FrameLayout.LayoutParams -> params.marginStart = marginValue
        is RecyclerView.LayoutParams -> params.marginStart = marginValue
    }
}

fun Fragment.move(@IdRes direction: Int, bundle: Bundle = Bundle()) {
    try {
        findNavController().navigate(direction, bundle)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.setVisibility(isVisible: Boolean): Unit =
    if (isVisible) visibility = View.VISIBLE else visibility = View.GONE

fun Fragment.showToast(message: String) {
    activity?.let {
        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
    }
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun throwException(message: String): Nothing = throw Exception(message)

fun View.setAspectRatio(
    heightFactor: Float, margin: Int = 0
) {
    val marginInPx = this.context.dpToPixel(margin)
    val calculatedWidth = (Resources.getSystem().displayMetrics.widthPixels - 2.times(marginInPx))
    val params = this.layoutParams
    params.width = calculatedWidth
    params.height = (calculatedWidth / heightFactor).toInt()
    this.layoutParams = params
}

fun View.below(view: View) {
    (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.BELOW, view.id)
}

fun Context.dpToPixel(dp: Int): Int =
    (dp * applicationContext.resources.displayMetrics.density).toInt()