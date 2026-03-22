package com.wanfeng.launcher.service

import android.app.NotificationChannel
import android.app.NotificationManager
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
        private const val TAG         = "GlobalFloat"
        const val ACTION_SHOW_HERO    = "wf.float.SHOW_HERO"
        const val ACTION_SHOW_MATCH   = "wf.float.SHOW_MATCH_END"
        const val ACTION_HIDE         = "wf.float.HIDE"
        const val CHANNEL_ID          = "wf_float_fg"

        var onConfirmHero:     (() -> Unit)? = null
        var onConfirmMatchEnd: (() -> Unit)? = null

        fun startAction(context: Context, action: String) {
            val i = Intent(context, GlobalFloatService::class.java).setAction(action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i)
            else
                context.startService(i)
        }
        fun showHero(context: Context)     = startAction(context, ACTION_SHOW_HERO)
        fun showMatchEnd(context: Context) = startAction(context, ACTION_SHOW_MATCH)
        fun hide(context: Context)         = startAction(context, ACTION_HIDE)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var floatView: android.view.View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createFgNotification()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "onCreate wm=$wm")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_SHOW_HERO  -> mainHandler.post { showFloat(isHero = true) }
            ACTION_SHOW_MATCH -> mainHandler.post { showFloat(isHero = false) }
            ACTION_HIDE       -> mainHandler.post { removeFloat() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mainHandler.post { removeFloat() }
        super.onDestroy()
    }

    private fun showFloat(isHero: Boolean) {
        removeFloat()
        val dp      = resources.displayMetrics.density
        val isDark  = isNightTime()
        val accent  = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val bgAlpha = if (isDark) Color.parseColor("#CC0F0C1E") else Color.parseColor("#CCE8F3FF")
        val textClr = if (isDark) Color.WHITE else Color.parseColor("#0D1E35")
        val pad     = (16 * dp).toInt()

        // 根容器
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setPadding(pad, (20 * dp).toInt(), pad, (20 * dp).toInt())
            background  = GradientDrawable().apply {
                setColor(bgAlpha)
                cornerRadius = 40f * dp
                setStroke((2 * dp).toInt(), accent)
            }
        }

        root.addView(TextView(this).apply {
            text     = if (isHero) "⚔️" else "🏁"
            textSize = 22f
            gravity  = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text     = if (isHero) "已拉起游戏\n是否进入\n选英雄界面？"
                       else        "本局对局\n是否结束？"
            textSize = 12f
            setTextColor(textClr)
            gravity  = Gravity.CENTER
            setPadding(0, (10 * dp).toInt(), 0, (10 * dp).toInt())
        })

        root.addView(Button(this).apply {
            text     = "是"
            textSize = 14f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                colors = if (isDark)
                    intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#9B6DFF"))
                else
                    intArrayOf(Color.parseColor("#3D7EFF"), Color.parseColor("#5B8EFF"))
                orientation  = GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = 28f * dp
            }
            val hp = (24 * dp).toInt(); val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            setOnClickListener {
                removeFloat()
                if (isHero) onConfirmHero?.invoke() else onConfirmMatchEnd?.invoke()
            }
        })

        // WindowManager 参数
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        val params = WindowManager.LayoutParams(
            (148 * dp).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = (12 * dp).toInt()
            y = 0
        }

        try {
            wm!!.addView(root, params)
            floatView = root
            Log.d(TAG, "addView OK isHero=$isHero type=$type")
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

    private fun isNightTime(): Boolean {
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return h >= 19 || h < 7
    }

    private fun createFgNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "晚风悬浮窗", NotificationManager.IMPORTANCE_MIN)
            ch.setShowBadge(false)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
        val notif = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.wanfeng.launcher.R.drawable.ic_launcher)
            .setContentTitle("晚风服务运行中")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .build()
        startForeground(9001, notif)
    }
}
