package com.bot.wishingwell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }

        val title = TextView(this).apply {
            text = "Wishing Well Bot"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        val permBtn = Button(this).apply {
            text = "1. Enable Accessibility Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        layout.addView(permBtn)

        val testLeftBtn = Button(this).apply {
            text = "Test Swipe Left"
            setOnClickListener {
                if (AutoSwipeService.instance != null) {
                    AutoSwipeService.instance?.moveLeft()
                } else {
                    Toast.makeText(this@MainActivity, "Enable Accessibility first!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(testLeftBtn)

        val testRightBtn = Button(this).apply {
            text = "Test Swipe Right"
            setOnClickListener {
                if (AutoSwipeService.instance != null) {
                    AutoSwipeService.instance?.moveRight()
                } else {
                    Toast.makeText(this@MainActivity, "Enable Accessibility first!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(testRightBtn)

        setContentView(layout)
    }
}

