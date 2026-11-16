          package com.venice.hud

          import android.content.Context

          data class Config(
              var crosshair: Boolean = true,
              var stats: Boolean = true,
              var timer: Boolean = true,
              var esp: Boolean = true,
              var aimMode: Int = 0,
              var autoFire: Boolean = false,
              var fireBtn: Boolean = false,
              var fx: Float = 0f,
              var fy: Float = 0f,
              var fov: Float = 16f,
              var smooth: Int = 30,
              var aimMs: Int = 50,
              var headK: Int = 30,
              var redThr: Int = 65,
              var minBar: Int = 6,
              var style: Int = 0,
              var size: Float = 34f,
              var thickness: Float = 3f,
              var gap: Float = 14f,
              var color: Int = 0xFF00E08A.toInt(),
              var boxColor: Int = 0xFFFF3B30.toInt()
          ) {
              fun save(c: Context) {
                  c.getSharedPreferences("cfg", 0).edit()
                      .putBoolean("ch", crosshair).putBoolean("st", stats).putBoolean("tm", timer)
                      .putBoolean("es", esp).putInt("am", aimMode).putBoolean("af", autoFire)
                      .putBoolean("fb", fireBtn).putFloat("fx", fx).putFloat("fy", fy)
                      .putFloat("fv", fov).putInt("sm", smooth).putInt("ams", aimMs)
                      .putInt("hk", headK).putInt("rt", redThr).putInt("mb", minBar)
                      .putInt("sty", style).putFloat("sz", size).putFloat("th", thickness)
                      .putFloat("gp", gap).putInt("cl", color).putInt("bc", boxColor)
                      .apply()
              }

              companion object {
                  fun load(c: Context): Config {
                      val p = c.getSharedPreferences("cfg", 0)
                      val d = Config()
                      return d.copy(
                          crosshair = p.getBoolean("ch", d.crosshair),
                          stats = p.getBoolean("st", d.stats),
                          timer = p.getBoolean("tm", d.timer),
                          esp = p.getBoolean("es", d.esp),
                          aimMode = p.getInt("am", d.aimMode),
                          autoFire = p.getBoolean("af", d.autoFire),
                          fireBtn = p.getBoolean("fb", d.fireBtn),
                          fx = p.getFloat("fx", d.fx),
                          fy = p.getFloat("fy", d.fy),
                          fov = p.getFloat("fv", d.fov),
                          smooth = p.getInt("sm", d.smooth),
                          aimMs = p.getInt("ams", d.aimMs),
                          headK = p.getInt("hk", d.headK),
                          redThr = p.getInt("rt", d.redThr),
                          minBar = p.getInt("mb", d.minBar),
                          style = p.getInt("sty", d.style),
                          size = p.getFloat("sz", d.size),
                          thickness = p.getFloat("th", d.thickness),
                          gap = p.getFloat("gp", d.gap),
                          color = p.getInt("cl", d.color),
                          boxColor = p.getInt("bc", d.boxColor)
                      )
                  }
              }
          }