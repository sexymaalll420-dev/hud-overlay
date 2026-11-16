          package com.venice.hud

          import java.nio.ByteBuffer

          object Detector {

              class Blob(var x0: Int, var x1: Int, var y0: Int, var y1: Int, var lastRow: Int)

              fun scan(
                  buf: ByteBuffer, w: Int, h: Int, rowStride: Int, ps: Int,
                  redThr: Int, minBar: Int
              ): List<Blob> {

                  val yStart = (h * 0.06f).toInt()
                  val yEnd = (h * 0.80f).toInt()
                  if (yEnd <= yStart) return emptyList()

                  val active = ArrayList<Blob>()
                  val done = ArrayList<Blob>()

                  for (y in yStart until yEnd) {
                      val base = y * rowStride
                      var x = 0
                      val segs = ArrayList<IntArray>()

                      while (x < w) {
                          var i = base + x * ps
                          if (!isRed(buf.get(i), buf.get(i + 1), buf.get(i + 2), redThr)) { x++; continue }
                          val sx = x
                          while (x < w) {
                              i = base + x * ps
                              if (!isRed(buf.get(i), buf.get(i + 1), buf.get(i + 2), redThr)) break
                              x++
                          }
                          if (x - sx >= minBar) segs.add(intArrayOf(sx, x - 1))
                      }

                      for (s in segs) {
                          var hit: Blob? = null
                          for (b in active) {
                              if (s[0] <= b.x1 + 1 && s[1] >= b.x0 - 1) { hit = b; break }
                          }
                          if (hit != null) {
                              if (s[0] < hit.x0) hit.x0 = s[0]
                              if (s[1] > hit.x1) hit.x1 = s[1]
                              hit.y1 = y
                              hit.lastRow = y
                          } else {
                              active.add(Blob(s[0], s[1], y, y, y))
                          }
                      }

                      val it = active.iterator()
                      while (it.hasNext()) {
                          val b = it.next()
                          if (y - b.lastRow > 2) { done.add(b); it.remove() }
                      }
                  }
                  done.addAll(active)

                  val out = ArrayList<Blob>()
                  for (b in done) {
                      val bw = b.x1 - b.x0 + 1
                      val bh = b.y1 - b.y0 + 1
                      if (bw < minBar) continue
                      if (bh > minBar + 2) continue
                      if (bw.toFloat() / bh.toFloat() < 2.2f) continue
                      out.add(b)
                  }
                  return out
              }

              private fun isRed(rb: Byte, gb: Byte, bb: Byte, thr: Int): Boolean {
                  val r = rb.toInt() and 0xFF
                  val g = gb.toInt() and 0xFF
                  val b = bb.toInt() and 0xFF
                  return r >= 115 && (r - g) >= thr && (r - b) >= thr && g <= 145
              }
          }