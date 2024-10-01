package com.ripenapps.greenhouse.utills

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ripenapps.greenhouse.R

class VerticalLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = context.getColor(R.color.greenhousetheme)
        strokeWidth = 3f
    }

    var xValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startY = 50f  // Fixed height from the top
        val endY = 150f   // Fixed height to the bottom

        canvas.drawLine(xValue, startY, xValue, endY, paint)
    }
}
