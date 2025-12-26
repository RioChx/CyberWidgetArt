package com.cyber.widget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.View
import android.graphics.SweepGradient
import android.graphics.Matrix
import android.media.AudioManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private lateinit var clockView: ClockView
    private var params: WindowManager.LayoutParams? = null
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var lastTapTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val rotationRunnable = object : Runnable {
        override fun run() {
            clockView.rotationAngle += prefs.getFloat("rgb_speed", 1f)
            clockView.invalidate()
            handler.postDelayed(this, 50)
        }
    }

    private val prefs by lazy { getSharedPreferences("widget_prefs", MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = FrameLayout(this)
        val inflater = LayoutInflater.from(this)
        // For simplicity, we use ClockView as root; add gear/icon in ClockView
        clockView = ClockView(this)
        overlayView.addView(clockView)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        windowManager.addView(overlayView, params)

        overlayView.setOnTouchListener(DragListener())
        handler.post(rotationRunnable)
    }

    inner class DragListener : View.OnTouchListener {
        private val gestureDetector = GestureDetector(this@OverlayService, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (kotlin.math.abs(distanceY) > kotlin.math.abs(distanceX)) {
                    // Vertical scroll -> volume
                    val audio = getSystemService(AUDIO_SERVICE) as AudioManager
                    if (distanceY > 0) audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    else audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    return true
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Expand logic (simplified: toggle size)
                params?.width = if (params?.width == WindowManager.LayoutParams.WRAP_CONTENT) 600 else WindowManager.LayoutParams.WRAP_CONTENT
                params?.height = if (params?.height == WindowManager.LayoutParams.WRAP_CONTENT) 200 else WindowManager.LayoutParams.WRAP_CONTENT
                params?.let { windowManager.updateViewLayout(overlayView, it) }
                return true
            }
        })

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params!!.x
                    initialY = params!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    if (System.currentTimeMillis() - lastTapTime < 300) {
                        // Double tap handled by gestureDetector
                    }
                    lastTapTime = System.currentTimeMillis()
                    return true
                }
                MotionEvent.ACTION_UP -> return true
                MotionEvent.ACTION_MOVE -> {
                    params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params!!)
                    return true
                }
            }
            return false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rotationRunnable)
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
    }
}
