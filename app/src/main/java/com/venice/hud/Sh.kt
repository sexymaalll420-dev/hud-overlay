          package com.venice.hud

          import rikka.shizuku.Shizuku

          object Sh {
              fun run(cmd: String): String {
                  return try {
                      val p = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null) ?: return ""
                      val out = p.inputStream.bufferedReader().readText()
                      p.waitFor()
                      out
                  } catch (t: Throwable) { "" }
              }
          }