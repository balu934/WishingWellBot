package com.bot.wishingwell

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class AutoSwipeService : AccessibilityService() {

    companion object {
        var instance: AutoSwipeService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    // ఎడమ వైపు నుండి కుడి వైపుకు లేదా ఇచ్చిన పాయింట్ల మధ్య స్వైప్ చేసే ఫంక్షన్
    fun swipe(fromX: Float, fromY: Float, toX: Float, toY: Float, durationMs: Long = 80) {
        val swipePath = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }

    // బకెట్‌ను ఎడమ వైపుకు జరపడానికి
    fun moveLeft() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()
        val midY = screenHeight * 0.7f
        swipe(screenWidth * 0.5f, midY, screenWidth * 0.2f, midY)
    }

    // బకెట్‌ను కుడి వైపుకు జరపడానికి
    fun moveRight() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()
        val midY = screenHeight * 0.7f
        swipe(screenWidth * 0.5f, midY, screenWidth * 0.8f, midY)
    }
}

