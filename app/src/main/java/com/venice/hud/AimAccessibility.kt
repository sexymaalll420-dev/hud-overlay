          package com.venice.hud

          import android.accessibilityservice.AccessibilityService
          import android.accessibilityservice.GestureDescription
          import android.graphics.Path
          import android.view.accessibility.AccessibilityEvent

          class AimAccessibility : AccessibilityService() {

              override fun onServiceConnected() {
                  super.onServiceConnected()
                  instance = this
              }

              override fun onAccessibilityEvent(e: AccessibilityEvent?) {}

              override fun onInterrupt() {}

              override fun onDestroy() {
                  instance = null
                  super.onDestroy()
              }

              fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, ms: Long): Boolean {
                  return try {
                      val p = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
                      val g = GestureDescription.Builder()
                          .addStroke(GestureDescription.StrokeDescription(p, 0, ms.coerceAtLeast(20)))
                          .build()
                      dispatchGesture(g, null, null)
                  } catch (e: Exception) { false }
              }

              companion object {
                  @Volatile var instance: AimAccessibility? = null
              }
          }