package com.ripenapps.greenhouse.utills

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import androidx.core.content.ContextCompat

fun String.getSpannableString(
    startPosition: Int,
    endPosition: Int,
    textColor: Int? = null,
    textRatio: Float? = null,
    typeFace: Typeface? = null,
    context: Context
): SpannableString {
    val spannableString = SpannableString(this).apply {
        typeFace?.let {
            setSpan(
                CustomTypefaceSpan(it),
                startPosition,
                endPosition,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        textRatio?.let {
            setSpan(
                RelativeSizeSpan(it), startPosition, endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        textColor?.let { color ->
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, color)),
                startPosition,
                endPosition,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }
    return spannableString
}