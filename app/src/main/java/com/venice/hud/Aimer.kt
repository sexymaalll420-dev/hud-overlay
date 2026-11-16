          package com.venice.hud

          import android.os.SystemClock
          import kotlin.math.abs
          import kotlin.math.hypot

          object Aimer {

              private var lastAt = 0L
              private const val MIN_GAP = 110L
              private const val CAP = 300f
              private const val DEAD = 3f
              private const val BASE = 70f

              val backend: String
                  get() = if (ShizukuClient.ready) "Shizuku" else if (AimAccessibility.instance != null) "Accessibility" else "कोई नहीं"

              /** गेन: smooth 0 = 1.43x, 30 = 1.0x, 70 = 0.43x */
              private fun gain(smooth: Int) = (BASE + 30 - smooth.coerceIn(0, 95)) / BASE

              private fun delta(from: Float, to: Float, g: Float) = ((to - from) * g).coerceIn(-CAP, CAP)

              fun snap(cx: Float, cy: Float, tx: Float, ty: Float, c: Config, force: Boolean = false): Boolean {
                  val now = SystemClock.uptimeMillis()
                  if (!force && now - lastAt < MIN_GAP) return false

                  val g = gain(c.smooth)
                  val ddx = delta(cx, tx, g)
                  val ddy = delta(cy, ty, g)
                  if (abs(ddx) < DEAD && abs(ddy) < DEAD) return false

                  val ms = c.aimMs.coerceIn(20, 400)
                  val inj = ShizukuClient.inject
                  val ok = try {
                      if (inj != null) inj.swipe(cx, cy, cx + ddx, cy + ddy, ms)
                      else AimAccessibility.instance?.swipe(cx, cy, cx + ddx, cy + ddy, ms.toLong()) ?: false
                  } catch (t: Throwable) { false }

                  if (ok) lastAt = now
                  return ok
              }

              /** FIRE दबाने पर: सिर पर स्नैप पूरा करो, फिर फायर दबाकर रखो */
              fun firePress(cx: Float, cy: Float, t: FloatArray?, c: Config) {
                  val inj = ShizukuClient.inject
                  if (inj == null) {
                      if (t != null) AimAccessibility.instance?.swipe(cx, cy, t[0], t[1], c.aimMs.toLong())
                      return
                  }
                  try {
                      if (t == null) {
                          inj.fireDown(c.fx, c.fy)
                          return
                      }
                      val g = gain(c.smooth)
                      inj.snapHold(cx, cy, cx + delta(cx, t[0], g), cy + delta(cy, t[1], g),
                          c.fx, c.fy, c.aimMs.coerceIn(20, 200))
                  } catch (th: Throwable) {}
              }

              fun fireRelease(c: Config) {
                  try { ShizukuClient.inject?.fireUp(c.fx, c.fy) } catch (t: Throwable) {}
              }

              fun dist(ax: Float, ay: Float, bx: Float, by: Float): Float =
                  hypot((ax - bx).toDouble(), (ay - by).toDouble()).toFloat()
          }