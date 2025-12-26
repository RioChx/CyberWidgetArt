package com.cyber.widget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
// Add UI for config (simplified SeekBars)

class ConfigActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("widget_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle ?) {
        super.onCreate(savedInstanceState)
        // Build UI with SeekBars for scale (50-150), alpha (0-255), speed (0.5-5), etc.
        // On change: prefs.edit().putFloat("scale", value).apply()
        // ClockView will pick up on next invalidate
    }
}
