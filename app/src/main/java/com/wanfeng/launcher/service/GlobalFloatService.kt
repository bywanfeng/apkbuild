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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class GlobalFloatService : Service() {

    companion object {
        private const val TAG          = "GlobalFloat"
        const val ACTION_SHOW_HERO     = "wf.float.SHOW_HERO"
        const val ACTION_SHOW_MATCH    = "wf.float.SHOW_MATCH_END"
        const val ACTION_HIDE          = "wf.float.HIDE"
        const val CHANNEL_ID           = "wf_float_fg"

        var onConfirmHero:     (() -> Unit)? = null
        var onConfirmMatchEnd: (() -> Unit)? = null

        fun showHero(context: Context)     = start(context, ACTION_SHOW_HERO)
        fun showMatchEnd(context: Context) = start(context, ACTION_SHOW_MATCH)
        fun hide(context: Context)         = start(context, ACTION_HIDE)

        private fun start(context: Context, action: String) {
            context.startService(Intent(context, GlobalFloatService::class.java).setAction(action))
        }
    }

    private val handler  = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var floatView: android.view.View? = null
    private var isCollapsed = false   // 当前是否折叠成小点
    private var currentIsHero = true  // 记住当前阶段，展开时用

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "onCreate ok")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_SHOW_HERO  -> handler.post { currentIsHero = true;  showExpanded() }
            ACTION_SHOW_MATCH -> handler.post { currentIsHero = false; showExpanded() }
            ACTION_HIDE       -> handler.post { removeFloat() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.post { removeFloat() }
        super.onDestroy()
    }

    // ── 展开的完整悬浮窗 ──────────────────────────────────────────────────────
    private fun showExpanded() {
        removeFloat()
        isCollapsed  = false
        val isHero   = currentIsHero
        val dp       = resources.displayMetrics.density
        val isDark   = isNightTime()
        val accent   = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val bg       = if (isDark) Color.parseColor("#CC0F0C1E") else Color.parseColor("#CCE8F3FF")
        val fg       = if (isDark) Color.WHITE else Color.parseColor("#0D1E35")
        val p        = (14 * dp).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setPadding(p, (18 * dp).toInt(), p, (18 * dp).toInt())
            background  = GradientDrawable().apply {
                setColor(bg)
                cornerRadius = 40f * dp
                setStroke((2 * dp).toInt(), accent)
            }
        }

        // ── 顶部：折叠按钮 ────────────────────────────────────────────────────
        val collapseBtn = TextView(this).apply {
            text     = "—"
            textSize = 12f
            setTextColor(if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF"))
            gravity  = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, (4 * dp).toInt())
            setOnClickListener { handler.post { showCollapsed() } }
        }
        root.addView(collapseBtn)

        // ── 图标 ──────────────────────────────────────────────────────────────
        root.addView(TextView(this).apply {
            text     = if (isHero) "⚔️" else "🏁"
            textSize = 20f; gravity = Gravity.CENTER
        })

        // ── 提示文字 ──────────────────────────────────────────────────────────
        root.addView(TextView(this).apply {
            text     = if (isHero) "已拉起游戏\n是否进入\n选英雄界面？" else "本局对局\n是否结束？"
            textSize = 11f; setTextColor(fg); gravity = Gravity.CENTER
            setPadding(0, (8 * dp).toInt(), 0, (8 * dp).toInt())
        })

        // ── 确认按钮 ──────────────────────────────────────────────────────────
        root.addView(android.widget.Button(this).apply {
            text = "是"; textSize = 13f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                colors = if (isDark) intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#9B6DFF"))
                         else        intArrayOf(Color.parseColor("#3D7EFF"), Color.parseColor("#5B8EFF"))
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = 28f * dp
            }
            val hp = (20 * dp).toInt(); val vp = (7 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            setOnClickListener {
                removeFloat()
                if (isHero) onConfirmHero?.invoke() else onConfirmMatchEnd?.invoke()
            }
        })

        addToWindow(root, (140 * dp).toInt())
        upgradeForeground()
    }

    // ── 折叠成右侧小圆点 ──────────────────────────────────────────────────────
    private fun showCollapsed() {
        removeFloat()
        isCollapsed = true
        val dp     = resources.displayMetrics.density
        val isDark = isNightTime()
        val accent = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val size   = (28 * dp).toInt()

        val dot = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                shape        = GradientDrawable.OVAL
                setColor(accent.and(0x00FFFFFF.or(0xBB000000.toInt())))  // 半透明
                setStroke((2 * dp).toInt(), accent)
            }
            setOnClickListener { handler.post { showExpanded() } }
        }
        // 中心小三角指示
        dot.addView(TextView(this).apply {
            text     = "◀"
            textSize = 10f
            setTextColor(Color.WHITE)
            gravity  = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        })

        addToWindow(dot, size, height = size, offsetX = (4 * dp).toInt())
        // 折叠状态也保持前台（悬浮窗还在，仍需保活）
        upgradeForeground()
    }

    // ── WindowManager addView ─────────────────────────────────────────────────
    private fun addToWindow(view: android.view.View, width: Int, height: Int = WindowManager.LayoutParams.WRAP_CONTENT, offsetX: Int = (12 * resources.displayMetrics.density).toInt()) {
        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            width, height, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = offsetX; y = 0
        }
        try {
            wm!!.addView(view, params)
            floatView = view
            Log.d(TAG, "addToWindow OK collapsed=$isCollapsed")
        } catch (e: Exception) {
            Log.e(TAG, "addToWindow FAILED: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun removeFloat() {
        floatView?.let {
            try { wm?.removeViewImmediate(it) } catch (_: Exception) {}
            floatView = null
        }
        degradeForeground()
    }

    // ── 前台保活 ──────────────────────────────────────────────────────────────
    private fun upgradeForeground() {
        createFgChannel()
        val notif = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.wanfeng.launcher.R.drawable.ic_launcher)
            .setContentTitle("晚风服务运行中")
            .setOngoing(true).setSilent(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .build()
        startForeground(9001, notif)
    }

    private fun degradeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            stopForeground(STOP_FOREGROUND_REMOVE)
        else @Suppress("DEPRECATION") stopForeground(true)
    }

    private fun createFgChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "晚风悬浮窗", NotificationManager.IMPORTANCE_MIN)
            ch.setShowBadge(false)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun isNightTime() =
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY).let { it >= 19 || it < 7 }
}
