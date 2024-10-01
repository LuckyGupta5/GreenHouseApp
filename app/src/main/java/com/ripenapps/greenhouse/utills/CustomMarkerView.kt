package com.ripenapps.greenhouse.utills

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.ripenapps.greenhouse.R

class CustomMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    private var xValue: Float = 0f
    private var yValue: Float = 0f
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val paint: Paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, R.color.greenhousetheme)
        paint.strokeWidth = 10f
    }

    // Refresh the content of the MarkerView when a data point is selected
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val monthLabel = getXAxisValue(e.x.toInt())
            val value = e.y
            yValue = e.y
            xValue = e.y
            Log.e("TAG", "refreshContent: " + e.y)

            val sarLabel = context.getString(R.string.sar)
            tvContent.text = "$sarLabel $value"
        }

        // Center the MarkerView above the selected point
        super.refreshContent(e, highlight)
    }

    private fun getXAxisValue(index: Int): String {
        val months = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        )
        return if (index in 0 until months.size) {
            months[index]
        } else {
            ""
        }
    }

    // Offset the MarkerView to position it above the selected point
    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }

    fun setData(entry: String) {
        // Assuming your entry contains relevant data
        tvContent.text = " ${entry}"
    }
}
