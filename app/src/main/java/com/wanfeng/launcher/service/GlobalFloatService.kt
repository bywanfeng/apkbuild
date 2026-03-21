package com.wanfeng.launcher.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.util.Log
import android.util.TypedValue

/**
 * 系统级全局悬浮窗 Service。
 * TYPE_APPLICATION_OVERLAY — 浮在所有应用上方，游戏时可见。
 * 通过 Intent action 控制显示/隐藏。
 */
class GlobalFloatService : Service() {

    companion object {
        private const val TAG          = "GlobalFloat"
        const val ACTION_SHOW_HERO     = "wf.float.SHOW_HERO"
        const val ACTION_SHOW_MATCH    = "wf.float.SHOW_MATCH_END"
        const val ACTION_HIDE          = "wf.float.HIDE"
        const val CHANNEL_ID           = "wf_float_fg"

        var onConfirmHero:     (() -> Unit)? = null
        var onConfirmMatchEnd: (() -> Unit)? = null

        fun showHero(context: Context) {
            context.startService(Intent(context, GlobalFloatService::class.java)
                .setAction(ACTION_SHOW_HERO))
        }
        fun showMatchEnd(context: Context) {
            context.startService(Intent(context, GlobalFloatService::class.java)
                .setAction(ACTION_SHOW_MATCH))
        }
        fun hide(context: Context) {
            context.startService(Intent(context, GlobalFloatService::class.java)
                .setAction(ACTION_HIDE))
        }
    }

    private var wm: WindowManager? = null
    private var floatView: android.view.View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notif = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.wanfeng.launcher.R.drawable.ic_launcher)
            .setContentTitle("晚风服务")
            .setOngoing(true)
            .setSilent(true)
            .build()
        startForeground(9001, notif)
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_HERO  -> showFloat(isHero = true)
            ACTION_SHOW_MATCH -> showFloat(isHero = false)
            ACTION_HIDE       -> removeFloat()
        }
        return START_STICKY
    }

    override fun onDestroy() { removeFloat(); super.onDestroy() }

    private fun showFloat(isHero: Boolean) {
        removeFloat()
        val isDark    = isNightTime()
        val accentInt = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val bgInt     = if (isDark) Color.parseColor("#CC0F0C1E") else Color.parseColor("#CCE8F3FF")
        val textInt   = if (isDark) Color.WHITE else Color.parseColor("#0D1E35")
        val dp        = resources.displayMetrics.density

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            val pad     = (18 * dp).toInt()
            setPadding(pad, (20 * dp).toInt(), pad, (20 * dp).toInt())
            background  = android.graphics.drawable.GradientDrawable().apply {
                setColor(bgInt)
                cornerRadius = 40f * dp
                setStroke((1.5f * dp).toInt(), accentInt)
            }
        }

        // 图标
        root.addView(android.widget.TextView(this).apply {
            text     = if (isHero) "⚔️" else "🏁"
            textSize = 22f
            gravity  = Gravity.CENTER
        })

        // 说明文字
        root.addView(android.widget.TextView(this).apply {
            text     = if (isHero) "已拉起游戏\n是否进入\n选英雄界面？"
                       else        "本局对局\n是否结束？"
            textSize = 12f
            setTextColor(textInt)
            gravity  = Gravity.CENTER
            val vpad = (12 * dp).toInt()
            setPadding(0, vpad, 0, vpad)
        })

        // 按钮
        root.addView(android.widget.Button(this).apply {
            text     = "是"
            textSize = 14f
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                colors = if (isDark)
                    intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#9B6DFF"))
                else
                    intArrayOf(Color.parseColor("#3D7EFF"), Color.parseColor("#5B8EFF"))
                orientation  = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = 28f * dp
            }
            val hpad = (28 * dp).toInt(); val vpad = (10 * dp).toInt()
            setPadding(hpad, vpad, hpad, vpad)
            setOnClickListener {
                removeFloat()
                if (isHero) onConfirmHero?.invoke() else onConfirmMatchEnd?.invoke()
            }
        })

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            (152 * dp).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = (12 * dp).toInt()
            y = 0
        }

        try {
            wm?.addView(root, params)
            floatView = root
            Log.d(TAG, "showFloat isHero=$isHero")
        } catch (e: Exception) {
            Log.e(TAG, "addView failed: ${e.message}")
        }
    }

    private fun removeFloat() {
        floatView?.let {
            try { wm?.removeView(it) } catch (_: Exception) {}
            floatView = null
        }
    }

    private fun isNightTime(): Boolean {
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return h >= 19 || h < 7
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "晚风悬浮窗", NotificationManager.IMPORTANCE_MIN)
            ch.setShowBadge(false)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }
}
