
package com.bot.wishingwell

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout

class AutoSwipeService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAutoRunning = false

    companion object {
        var instance: AutoSwipeService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        showFloatingMenu()
    }

    private fun showFloatingMenu() {
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(16, 16, 16, 16)
        }

        val btnToggle = Button(this).apply {
            text = "START AUTO"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (!isAutoRunning) {
                    isAutoRunning = true
                    text = "STOP AUTO"
                    setBackgroundColor(Color.parseColor("#F44336"))
                    startAutoLoop()
                } else {
                    isAutoRunning = false
                    text = "START AUTO"
                    setBackgroundColor(Color.parseColor("#4CAF50"))
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }

        val btnLeft = Button(this).apply {
            text = "◀ Left"
            setOnClickListener { moveLeft() }
        }

        val btnRight = Button(this).apply {
            text = "Right ▶"
            setOnClickListener { moveRight() }
        }

        layout.addView(btnToggle)
        layout.addView(btnLeft)
        layout.addView(btnRight)

        floatingView = layout
        windowManager?.addView(floatingView, layoutParams)
    }

    private fun startAutoLoop() {
        if (!isAutoRunning) return

        moveLeft()
        handler.postDelayed({
            if (isAutoRunning) {
                moveRight()
                handler.postDelayed({ startAutoLoop() }, 600)
            }
        }, 600)
    }

    fun moveLeft() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        performSwipe(width * 0.75f, height * 0.75f, width * 0.25f, height * 0.75f)
    }

    fun moveRight() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        performSwipe(width * 0.25f, height * 0.75f, width * 0.75f, height * 0.75f)
    }

    private fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            .build()

        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        isAutoRunning = false
        handler.removeCallbacksAndMessages(null)
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
