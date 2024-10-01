package com.ripenapps.greenhouse.abstracts

import android.content.Context

fun Context.dpToPixel(dp: Int): Int = (dp * applicationContext.resources.displayMetrics.density).toInt()
