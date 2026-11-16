          package com.venice.hud

          import android.content.Context
          import android.content.Intent
          import android.graphics.PixelFormat
          import android.hardware.display.DisplayManager
          import android.hardware.display.VirtualDisplay
          import android.media.ImageReader
          import android.media.projection.MediaProjection
          import android.media.projection.MediaProjectionManager
          import android.os.Handler
          import android.os.HandlerThread
          import android.util.DisplayMetrics
          import android.view.WindowManager
          import java.nio.ByteBuffer

          class CaptureManager(private val ctx: Context, private val cb: Callback) {

              interface Callback {
                  fun onProjectionReady()
                  fun onFrame(buf: ByteBuffer, w: Int, h: Int, rowStride: Int, ps: Int)
                  fun onScale(scale: Int)
              }

              val scale = 4

              private val thread = HandlerThread("hudcap").apply { start() }
              private val h = Handler(thread.looper)
              private val dm = ctx.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
              private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

              private var projection: MediaProjection? = null
              private var vd: VirtualDisplay? = null
              private var reader: ImageReader? = null
              private var live = false

              private val displayListener = object : DisplayManager.DisplayListener {
                  override fun onDisplayAdded(id: Int) {}
                  override fun onDisplayRemoved(id: Int) {}
                  override fun onDisplayChanged(id: Int) { h.post { if (live) rebuild() } }
              }

              fun start(resultCode: Int, data: Intent) {
                  val mpm = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                  val p = mpm.getMediaProjection(resultCode, data) ?: return
                  projection = p
                  p.registerCallback(object : MediaProjection.Callback() {
                      override fun onStop() { stop() }
                  }, h)
                  cb.onProjectionReady()
                  live = true
                  dm.registerDisplayListener(displayListener, h)
                  rebuild()
              }

              private fun size(): Pair<Int, Int> {
                  val m = DisplayMetrics()
                  @Suppress("DEPRECATION")
                  wm.defaultDisplay.getRealMetrics(m)
                  return m.widthPixels to m.heightPixels
              }

              private fun rebuild() {
                  release()
                  val (dw, dh) = size()
                  val w = (dw / scale).coerceAtLeast(8)
                  val hh = (dh / scale).coerceAtLeast(8)
                  val r = ImageReader.newInstance(w, hh, PixelFormat.RGBA_8888, 2)
                  r.setOnImageAvailableListener({ ir ->
                      val img = try { ir.acquireLatestImage() } catch (e: Exception) { null } ?: return@setOnImageAvailableListener
                      try {
                          val pl = img.planes[0]
                          cb.onFrame(pl.buffer, w, hh, pl.rowStride, pl.pixelStride)
                      } catch (e: Exception) {
                      } finally {
                          img.close()
                      }
                  }, h)
                  reader = r
                  vd = projection?.createVirtualDisplay(
                      "hud", w, hh, ctx.resources.displayMetrics.densityDpi,
                      DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, r.surface, null, h
                  )
                  cb.onScale(scale)
              }

              private fun release() {
                  try { vd?.release() } catch (e: Exception) {}
                  try { reader?.close() } catch (e: Exception) {}
                  vd = null
                  reader = null
              }

              fun stop() {
                  if (!live && projection == null) return
                  live = false
                  try { dm.unregisterDisplayListener(displayListener) } catch (e: Exception) {}
                  release()
                  try { projection?.stop() } catch (e: Exception) {}
                  projection = null
                  thread.quitSafely()
              }
          }