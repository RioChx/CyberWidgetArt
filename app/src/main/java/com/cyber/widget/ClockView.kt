package com.cyber.widget

import android.content.Context
import android.graphics.*
import android.view.View
import java.util.Calendar

class ClockView(context: Context) : View(context) {

    private val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var rotationAngle = 0f
    private val calendar = Calendar.getInstance()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = kotlin.math.min(width, height) / 2f * prefs.getFloat("scale", 1f)

        // Background glassmorphism
        paint.color = Color.parseColor("#88020617") // Semi-transparent slate
        canvas.drawCircle(centerX, centerY, radius, paint)

        // RGB rotating aura
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.RED)
        val gradient = SweepGradient(centerX, centerY, colors, null)
        val matrix = Matrix()
        matrix.postRotate(rotationAngle, centerX, centerY)
        gradient.setLocalMatrix(matrix)
        paint.shader = gradient
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(centerX, centerY, radius - 10, paint)
        paint.shader = null

        // Clock (digital example)
        calendar.timeInMillis = System.currentTimeMillis()
        paint.color = Color.CYAN
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        val time = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
        canvas.drawText(time, centerX, centerY, paint)

        // Add gear icon or buttons here if expanded

        invalidate() // For real-time clock
    }
}
