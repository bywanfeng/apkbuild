package com.wanfeng.launcher.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class GlobalFloatService : Service() {

    companion object {
        private const val TAG      = "GlobalFloat"
        const val ACTION_SHOW_HERO = "wf.float.SHOW_HERO"
        const val ACTION_SHOW_MATCH = "wf.float.SHOW_MATCH_END"
        const val ACTION_HIDE      = "wf.float.HIDE"

        var onConfirmHero:     (() -> Unit)? = null
        var onConfirmMatchEnd: (() -> Unit)? = null

        fun showHero(context: Context)     = start(context, ACTION_SHOW_HERO)
        fun showMatchEnd(context: Context) = start(context, ACTION_SHOW_MATCH)
        fun hide(context: Context)         = start(context, ACTION_HIDE)

        private fun start(context: Context, action: String) {
            // 普通 startService，不用 startForegroundService
            context.startService(
                Intent(context, GlobalFloatService::class.java).setAction(action)
            )
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var floatView: android.view.View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "onCreate ok, wm=$wm")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_SHOW_HERO  -> handler.post { showFloat(true) }
            ACTION_SHOW_MATCH -> handler.post { showFloat(false) }
            ACTION_HIDE       -> handler.post { removeFloat() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.post { removeFloat() }
        super.onDestroy()
    }

    private fun showFloat(isHero: Boolean) {
        removeFloat()
        val dp     = resources.displayMetrics.density
        val isDark = isNightTime()
        val accent = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val bg     = if (isDark) Color.parseColor("#CC0F0C1E") else Color.parseColor("#CCE8F3FF")
        val fg     = if (isDark) Color.WHITE else Color.parseColor("#0D1E35")
        val p      = (16 * dp).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setPadding(p, (20 * dp).toInt(), p, (20 * dp).toInt())
            background  = GradientDrawable().apply {
                setColor(bg)
                cornerRadius = 40f * dp
                setStroke((2 * dp).toInt(), accent)
            }
        }
        root.addView(TextView(this).apply {
            text = if (isHero) "⚔️" else "🏁"
            textSize = 22f; gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = if (isHero) "已拉起游戏\n是否进入\n选英雄界面？" else "本局对局\n是否结束？"
            textSize = 12f; setTextColor(fg); gravity = Gravity.CENTER
            setPadding(0, (10 * dp).toInt(), 0, (10 * dp).toInt())
        })
        root.addView(Button(this).apply {
            text = "是"; textSize = 14f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                colors = if (isDark) intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#9B6DFF"))
                         else        intArrayOf(Color.parseColor("#3D7EFF"), Color.parseColor("#5B8EFF"))
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = 28f * dp
            }
            val hp = (24 * dp).toInt(); val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            setOnClickListener {
                removeFloat()
                if (isHero) onConfirmHero?.invoke() else onConfirmMatchEnd?.invoke()
            }
        })

        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            (148 * dp).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = (12 * dp).toInt(); y = 0
        }

        try {
            wm!!.addView(root, params)
            floatView = root
            Log.d(TAG, "addView OK")
        } catch (e: Exception) {
            Log.e(TAG, "addView FAILED: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun removeFloat() {
        floatView?.let {
            try { wm?.removeViewImmediate(it) } catch (_: Exception) {}
            floatView = null
        }
    }

    private fun isNightTime() =
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY).let { it >= 19 || it < 7 }
}
