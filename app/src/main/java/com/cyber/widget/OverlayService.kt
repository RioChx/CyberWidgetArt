// File: app/src/main/java/com/cyber/widget/OverlayService.kt
// FULL ENTIRE CODE - copy and paste over your existing OverlayService.kt

package com.cyber.widget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import java.util.Timer
import java.util.TimerTask

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var minimizedView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var minimizedParams: WindowManager.LayoutParams? = null
    private var isMaximized = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflated maximized layout (you need layout files or create views programmatically)
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_maximized, null) // Replace with your layout or programmatic view

        // Minimized view (just the clock)
        minimizedView = ClockView(this) // Assuming ClockView is your custom clock drawing view

        // Parameters for maximized view
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        // Parameters for minimized view
        minimizedParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        // Add maximized view initially
        windowManager.addView(overlayView, params)

        // Setup buttons (example IDs - adjust to your layout)
        overlayView?.findViewById<ImageButton>(R.id.btn_minimize)?.setOnClickListener {
            toggleMinimize()
        }
        overlayView?.findViewById<ImageButton>(R.id.btn_home)?.setOnClickListener {
            // Go home
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }
        overlayView?.findViewById<ImageButton>(R.id.btn_apps)?.setOnClickListener {
            // Open recent apps or apps drawer
            val recentIntent = Intent(Intent.ACTION_RECENTS)
            startActivity(recentIntent)
        }

        // Make the overlay draggable
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayView, params)
                        return true
                    }
                }
                return false
            }
        })

        // Timer to update clock every second (if needed)
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Update clock hands or time text
                (minimizedView as? ClockView)?.invalidate()
                // Update maximized clock if needed
            }
        }, 0, 1000)
    }

    private fun toggleMinimize() {
        if (isMaximized) {
            windowManager.removeView(overlayView)
            windowManager.addView(minimizedView, minimizedParams)
        } else {
            windowManager.removeView(minimizedView)
            windowManager.addView(overlayView, params)
        }
        isMaximized = !isMaximized
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
        minimizedView?.let { windowManager.removeView(it) }
    }
}
