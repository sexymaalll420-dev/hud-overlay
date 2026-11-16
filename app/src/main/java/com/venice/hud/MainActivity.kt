package com.venice.hud

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private lateinit var info: TextView
    private val REQ_CAP = 1001
    private val REQ_SHIZUKU = 2002

    private val permListener = Shizuku.OnRequestPermissionResultListener { code, res ->
        if (code == REQ_SHIZUKU) {
            if (res == PackageManager.PERMISSION_GRANTED) {
                ShizukuClient.connect(this)
                Toast.makeText(this, "Shizuku मिल गया", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Shizuku परमिशन नहीं मिली", Toast.LENGTH_SHORT).show()
            }
            refresh()
        }
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        Shizuku.addRequestPermissionResultListener(permListener)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(52, 84, 52, 60)
            setBackgroundColor(0xFF0E1216.toInt())
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = "HUD + ESP + AIM  v4"
            setTextColor(0xFF00E08A.toInt())
            textSize = 26f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = "FIRE बटन दबाने पर क्रॉसहेयर दुश्मन के सिर पर जाता है और गोली चलती है।"
            setTextColor(0xFF8BA0AD.toInt())
            textSize = 13f
            setPadding(0, 16, 0, 24)
        })

        info = TextView(this).apply {
            setTextColor(0xFFCFD8DC.toInt())
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }
        root.addView(info)

        root.addView(btn("0. Shizuku परमिशन + ऑटो सेटअप") { shizukuSetup() })
        root.addView(btn("1. Overlay परमिशन") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        if (Build.VERSION.SDK_INT >= 33) {
            root.addView(btn("2. Notification परमिशन") {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            })
        }
        root.addView(btn("3. Accessibility (बैकअप, वैकल्पिक)") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        root.addView(btn("4. Battery optimization बंद") {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        })
        root.addView(btn("5. Screen capture + START HUD") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "पहले overlay परमिशन दो", Toast.LENGTH_SHORT).show()
                return@btn
            }
            val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CAP)
        })
        root.addView(btn("STOP") {
            stopService(Intent(this, HudService::class.java))
            Toast.makeText(this, "बंद", Toast.LENGTH_SHORT).show()
        })

        setContentView(scroll)
    }

    private fun btn(label: String, onClick: (View) -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener(onClick)
            setPadding(16, 24, 16, 24)
        }
    }

    private fun shizukuSetup() {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku चालू नहीं है — Shizuku ऐप खोलकर स्टार्ट करो", Toast.LENGTH_LONG).show()
            return
        }
        if (Shizuku.isPreV11()) {
            Toast.makeText(this, "Shizuku पुराना है, v13 लगाओ", Toast.LENGTH_LONG).show()
            return
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            grantAll()
        } else {
            Shizuku.requestPermission(REQ_SHIZUKU)
        }
    }

    private fun grantAll() {
        Thread {
            Sh.run("appops set $packageName SYSTEM_ALERT_WINDOW allow")
            Sh.run("pm grant $packageName android.permission.POST_NOTIFICATIONS")
            Sh.run("dumpsys deviceidle whitelist +$packageName")
            runOnUiThread {
                Toast.makeText(this, "परमिशन सेट हो गईं", Toast.LENGTH_SHORT).show()
                refresh()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val shiz = when {
            !Shizuku.pingBinder() -> "Shizuku चालू नहीं"
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> "मिल गई"
            else -> "नहीं मिली"
        }
        info.text = "Overlay: ${if (Settings.canDrawOverlays(this)) "मिल गई" else "नहीं मिली"}\n" +
                "Shizuku: $shiz\n" +
                "इंजेक्टर: ${if (ShizukuClient.ready) "तैयार" else "नहीं"}\n" +
                "HUD: ${if (HudService.running) "चालू है" else "बंद है"}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_CAP) return
        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(this, "capture परमिशन नहीं मिली", Toast.LENGTH_SHORT).show()
            return
        }
        val i = Intent(this, HudService::class.java)
            .putExtra("code", resultCode)
            .putExtra("data", data)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        Toast.makeText(this, "HUD चालू — Free Fire खोलो", Toast.LENGTH_SHORT).show()
    }
}
