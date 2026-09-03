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
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AutoSwipeService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAutoRunning = false
    private var swipeDelay: Long = 300L
    private var dragDuration: Long = 250L

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
            x = 20
            y = 120
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6111111"))
            setPadding(16, 16, 16, 16)
        }

        val btnToggle = Button(this).apply {
            text = "START AUTO"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#2E7D32"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (!isAutoRunning) {
                    isAutoRunning = true
                    text = "STOP AUTO"
                    setBackgroundColor(Color.parseColor("#C62828"))
                    startAutoLoop()
                } else {
                    isAutoRunning = false
                    text = "START AUTO"
                    setBackgroundColor(Color.parseColor("#2E7D32"))
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }

        val lrLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnLeft = Button(this).apply {
            text = "◀ Left"
            textSize = 12f
            setOnClickListener { moveLeft() }
        }
        val btnRight = Button(this).apply {
            text = "Right ▶"
            textSize = 12f
            setOnClickListener { moveRight() }
        }
        lrLayout.addView(btnLeft)
        lrLayout.addView(btnRight)

        val speedText = TextView(this).apply {
            text = "Delay: ${swipeDelay}ms"
            setTextColor(Color.YELLOW)
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 4)
        }

        val speedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnSpeedDown = Button(this).apply {
            text = "Fast (-)"
            textSize = 10f
            setOnClickListener {
                if (swipeDelay > 100L) swipeDelay -= 50L
                speedText.text = "Delay: ${swipeDelay}ms"
            }
        }
        val btnSpeedUp = Button(this).apply {
            text = "Slow (+)"
            textSize = 10f
            setOnClickListener {
                if (swipeDelay < 800L) swipeDelay += 50L
                speedText.text = "Delay: ${swipeDelay}ms"
            }
        }
        speedLayout.addView(btnSpeedDown)
        speedLayout.addView(btnSpeedUp)

        layout.addView(btnToggle)
        layout.addView(lrLayout)
        layout.addView(speedText)
        layout.addView(speedLayout)

        floatingView = layout
        windowManager?.addView(floatingView, layoutParams)
    }

    private fun startAutoLoop() {
        if (!isAutoRunning) return

        moveLeft()
        handler.postDelayed({
            if (isAutoRunning) {
                moveRight()
                handler.postDelayed({ startAutoLoop() }, swipeDelay)
            }
        }, dragDuration + 50)
    }

    fun moveLeft() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        // బకెట్ ఉండే ఎత్తు సుమారు 60% ఎత్తులో ఉంటుంది
        val yPos = height * 0.60f
        // స్క్రీన్ మధ్య నుండి ఎడమ వైపుకు లాగడం
        performDrag(width * 0.55f, yPos, width * 0.20f, yPos, dragDuration)
    }

    fun moveRight() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        val yPos = height * 0.60f
        // స్క్రీన్ మధ్య నుండి కుడి వైపుకు లాగడం
        performDrag(width * 0.45f, yPos, width * 0.80f, yPos, dragDuration)
    }

    private fun performDrag(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
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
