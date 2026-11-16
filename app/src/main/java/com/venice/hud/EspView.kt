          package com.venice.hud

          import android.content.Context
          import android.graphics.Canvas
          import android.graphics.Paint
          import android.view.View

          class EspView(c: Context) : View(c) {

              @Volatile var targets: List<FloatArray> = emptyList()
              @Volatile var boxColor: Int = 0xFFFF3B30.toInt()
              @Volatile var fovRadius: Float = 0f
              @Volatile var showFov: Boolean = false

              private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

              override fun onDraw(canvas: Canvas) {
                  val list = targets
                  val d = resources.displayMetrics.density

                  if (showFov && fovRadius > 0f) {
                      p.color = 0x66FFFFFF
                      p.strokeWidth = 1.5f * d
                      p.style = Paint.Style.STROKE
                      canvas.drawCircle(width / 2f, height / 2f, fovRadius, p)
                  }

                  if (list.isEmpty()) return

                  p.color = boxColor
                  p.strokeWidth = 1.6f * d
                  p.style = Paint.Style.STROKE

                  for (t in list) {
                      val x = t[0]
                      val y = t[1]
                      val bw = t[2]
                      val half = bw * 0.45f
                      canvas.drawRect(x - half, y - bw * 0.30f, x + half, y + bw * 0.95f, p)

                      p.style = Paint.Style.FILL
                      canvas.drawCircle(x, y, 2.4f * d, p)
                      p.style = Paint.Style.STROKE
                  }
              }
          }