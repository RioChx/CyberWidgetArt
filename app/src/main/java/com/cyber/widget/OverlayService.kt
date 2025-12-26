package com.cyber.widget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var maximizedView: FrameLayout? = null
    private var minimizedView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var minimizedParams: WindowManager.LayoutParams? = null
    private var isMaximized = true
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create minimized view (just the clock)
        minimizedView = ClockView(this).apply {
            layoutParams = FrameLayout.LayoutParams(200, 200) // Adjust size as needed
        }

        // Create maximized view programmatically
        maximizedView = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent dark

            // Rainbow glow border (using GradientDrawable)
            val border = GradientDrawable()
            border.shape = GradientDrawable.OVAL
            border.setStroke(20, rainbowGradient())
            background = border

            // Main container
            val container = LinearLayout(this@OverlayService).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 20, 20, 20)
            }

            // Left buttons panel
            val buttonsPanel = LinearLayout(this@OverlayService).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 0, 40, 0)
            }

            // Minimize button
            val btnMinimize = ImageButton(this@OverlayService).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // Built-in X icon
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { toggleView() }
            }

            // Home button
            val btnHome = ImageButton(this@OverlayService).apply {
                setImageResource(android.R.drawable.ic_menu_myplaces) // Home icon
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                }
            }

            // Apps / Recent button
            val btnApps = ImageButton(this@OverlayService).apply {
                setImageResource(android.R.drawable.ic_menu_agenda) // Grid icon placeholder
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    val recentIntent = Intent("android.intent.action.RECENT") // May not work on all devices
                    recentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(recentIntent)
                }
            }

            buttonsPanel.addView(btnMinimize)
            buttonsPanel.addView(btnHome)
            buttonsPanel.addView(btnApps)

            // Clock view
            val clock = ClockView(this@OverlayService).apply {
                layoutParams = FrameLayout.LayoutParams(300, 300)
            }

            container.addView(buttonsPanel)
            container.addView(clock)

            addView(container)
        }

        // Layout params for maximized
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 100
        }

        // Layout params for minimized
        minimizedParams = WindowManager.LayoutParams(
            200, 200,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 100
        }

        // Add maximized view initially
        windowManager.addView(maximizedView, params)

        // Make whole view draggable
        maximizedView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params!!.x
                    initialY = params!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(maximizedView, params)
                    true
                }
                else -> false
            }
        }

        minimizedView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialX = minimizedParams!!.x
                    initialY = minimizedParams!!.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    minimizedParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    minimizedParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(minimizedView, minimizedParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    toggleView()
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleView() {
        if (isMaximized) {
            windowManager.removeView(maximizedView)
            windowManager.addView(minimizedView, minimizedParams)
        } else {
            windowManager.removeView(minimizedView)
            windowManager.addView(maximizedView, params)
        }
        isMaximized = !isMaximized
    }

    private fun rainbowGradient(): Int {
        // Simple rainbow – you can improve this
        return Color.parseColor("#FF0000") // Red – replace with real gradient if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        maximizedView?.let { windowManager.removeView(it) }
        minimizedView?.let { windowManager.removeView(it) }
    }
}
