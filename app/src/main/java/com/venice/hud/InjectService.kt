          package com.venice.hud

          import android.app.Service
          import android.content.Intent
          import android.os.IBinder
          import android.os.SystemClock
          import android.view.InputDevice
          import android.view.InputEvent
          import android.view.MotionEvent
          import java.lang.reflect.Method
          import kotlin.math.max

          class InjectService : Service() {

              private val im: Any? by lazy { resolveInputManager() }
              private val injectM: Method? by lazy { resolveInject(im) }
              @Volatile private var holding = false

              private val binder = object : IInject.Stub() {

                  override fun ping(): Boolean = injectM != null

                  override fun tap(x: Float, y: Float): Boolean = swipe(x, y, x, y, 40)

                  override fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, ms: Int): Boolean {
                      if (injectM == null) return false
                      val dur = ms.coerceIn(20, 400)
                      Thread {
                          val down = SystemClock.uptimeMillis()
                          send(down, down, MotionEvent.ACTION_DOWN, floatArrayOf(x1), floatArrayOf(y1), 1)
                          val steps = max(3, dur / 8)
                          for (i in 1..steps) {
                              val t = i / steps.toFloat()
                              send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE,
                                  floatArrayOf(x1 + (x2 - x1) * t), floatArrayOf(y1 + (y2 - y1) * t), 1)
                              sleep((dur / steps).toLong())
                          }
                          send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, floatArrayOf(x2), floatArrayOf(y2), 1)
                      }.start()
                      return true
                  }

                  override fun fireDown(x: Float, y: Float): Boolean {
                      if (injectM == null) return false
                      if (holding) fireUp(x, y)
                      val now = SystemClock.uptimeMillis()
                      send(now, now, MotionEvent.ACTION_DOWN, floatArrayOf(x), floatArrayOf(y), 1)
                      holding = true
                      return true
                  }

                  override fun fireUp(x: Float, y: Float): Boolean {
                      if (injectM == null) return false
                      if (!holding) return true
                      val now = SystemClock.uptimeMillis()
                      send(now, now, MotionEvent.ACTION_UP, floatArrayOf(x), floatArrayOf(y), 1)
                      holding = false
                      return true
                  }

                  /** सिर पर स्नैप पूरा करो, फिर फायर दबाकर रखो */
                  override fun snapHold(
                      cx: Float, cy: Float, tx: Float, ty: Float, fx: Float, fy: Float, ms: Int
                  ): Boolean {
                      if (injectM == null) return false
                      val dur = ms.coerceIn(20, 200)
                      Thread {
                          val down = SystemClock.uptimeMillis()
                          send(down, down, MotionEvent.ACTION_DOWN, floatArrayOf(cx), floatArrayOf(cy), 1)
                          val steps = max(3, dur / 6)
                          for (i in 1..steps) {
                              val t = i / steps.toFloat()
                              send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE,
                                  floatArrayOf(cx + (tx - cx) * t), floatArrayOf(cy + (ty - cy) * t), 1)
                              sleep((dur / steps).toLong())
                          }
                          send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, floatArrayOf(tx), floatArrayOf(ty), 1)
                          sleep(12)
                          fireDown(fx, fy)
                      }.start()
                      return true
                  }
              }

              override fun onBind(i: Intent?): IBinder = binder

              private fun sleep(ms: Long) {
                  try { Thread.sleep(ms.coerceAtLeast(3)) } catch (e: Exception) {}
              }

              private fun send(down: Long, whenT: Long, action: Int, xs: FloatArray, ys: FloatArray, count: Int) {
                  val m = injectM ?: return
                  val i = im ?: return
                  val props = Array(count) { MotionEvent.PointerProperties().apply { id = it; toolType = MotionEvent.TOOL_TYPE_FINGER } }
                  val coords = Array(count) { MotionEvent.PointerCoords().apply { x = xs[it]; y = ys[it]; pressure = 1f; size = 1f } }
                  val e = MotionEvent.obtain(down, whenT, action, count, props, coords, 0, 0, 1f, 1f, 0, 0,
                      InputDevice.SOURCE_TOUCHSCREEN, 0)
                  try { m.invoke(i, e, 0) } catch (t: Throwable) {}
                  e.recycle()
              }

              private fun resolveInputManager(): Any? {
                  return try {
                      if (android.os.Build.VERSION.SDK_INT >= 34) {
                          val c = Class.forName("android.hardware.input.InputManagerGlobal")
                          c.getMethod("getInstance").invoke(null)
                      } else {
                          val c = Class.forName("android.hardware.input.InputManager")
                          c.getMethod("getInstance").invoke(null)
                      }
                  } catch (t: Throwable) { null }
              }

              private fun resolveInject(target: Any?): Method? {
                  if (target == null) return null
                  return try {
                      val m = target.javaClass.getMethod("injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType)
                      m.isAccessible = true
                      m
                  } catch (t: Throwable) { null }
              }
          }