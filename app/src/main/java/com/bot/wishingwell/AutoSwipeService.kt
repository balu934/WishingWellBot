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
import android.widget.TextView

class AutoSwipeService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAutoRunning = false
    private var currentDirection = 0 // 0 = Left to Right, 1 = Right to Left
    private var swipeDuration: Long = 350L // Drag Speed in ms

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
            setBackgroundColor(Color.parseColor("#E6000000"))
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
                    runFullLoop()
                } else {
                    isAutoRunning = false
                    text = "START AUTO"
                    setBackgroundColor(Color.parseColor("#2E7D32"))
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }

        val speedText = TextView(this).apply {
            text = "Speed: ${swipeDuration}ms"
            setTextColor(Color.YELLOW)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 6)
        }

        val speedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnFaster = Button(this).apply {
            text = "⚡ Fast"
            textSize = 11f
            setOnClickListener {
                if (swipeDuration > 150L) swipeDuration -= 50L
                speedText.text = "Speed: ${swipeDuration}ms"
            }
        }
        val btnSlower = Button(this).apply {
            text = "🐢 Slow"
            textSize = 11f
            setOnClickListener {
                if (swipeDuration < 1000L) swipeDuration += 50L
                speedText.text = "Speed: ${swipeDuration}ms"
            }
        }
        speedLayout.addView(btnFaster)
        speedLayout.addView(btnSlower)

        val lrLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnLeft = Button(this).apply {
            text = "◀ Full L"
            textSize = 11f
            setOnClickListener { fullSwipeLeft() }
        }
        val btnRight = Button(this).apply {
            text = "Full R ▶"
            textSize = 11f
            setOnClickListener { fullSwipeRight() }
        }
        lrLayout.addView(btnLeft)
        lrLayout.addView(btnRight)

        layout.addView(btnToggle)
        layout.addView(speedText)
        layout.addView(speedLayout)
        layout.addView(lrLayout)

        floatingView = layout
        windowManager?.addView(floatingView, layoutParams)
    }

    private fun runFullLoop() {
        if (!isAutoRunning) return

        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        val yPos = height * 0.60f

        val startX: Float
        val endX: Float

        if (currentDirection == 0) {
            // కుడి చివరి నుంచి ఎడమ చివరి వరకు (90% నుండి 10%)
            startX = width * 0.90f
            endX = width * 0.10f
            currentDirection = 1
        } else {
            // ఎడమ చివరి నుంచి కుడి చివరి వరకు (10% నుండి 90%)
            startX = width * 0.10f
            endX = width * 0.90f
            currentDirection = 0
        }

        val path = Path().apply {
            moveTo(startX, yPos)
            lineTo(endX, yPos)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, swipeDuration))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                if (isAutoRunning) {
                    handler.postDelayed({ runFullLoop() }, 40)
                }
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                if (isAutoRunning) {
                    handler.postDelayed({ runFullLoop() }, 40)
                }
            }
        }, null)
    }

    fun fullSwipeLeft() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        val yPos = height * 0.60f
        performSingleSwipe(width * 0.90f, yPos, width * 0.10f, yPos, swipeDuration)
    }

    fun fullSwipeRight() {
        val width = resources.displayMetrics.widthPixels.toFloat()
        val height = resources.displayMetrics.heightPixels.toFloat()
        val yPos = height * 0.60f
        performSingleSwipe(width * 0.10f, yPos, width * 0.90f, yPos, swipeDuration)
    }

    private fun performSingleSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
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
