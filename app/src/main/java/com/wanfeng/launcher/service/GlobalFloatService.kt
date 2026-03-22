package com.wanfeng.launcher.service

import android.app.AlertDialog
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
        var onStopAll:         (() -> Unit)? = null  // 关闭时执行 stop.sh

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
    private var isCollapsed   = false
    private var currentIsHero = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "onCreate ok")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        isCollapsed = false
        val isHero  = currentIsHero
        val dp      = resources.displayMetrics.density
        val isDark  = isNightTime()
        val accent  = if (isDark) Color.parseColor("#9B6DFF") else Color.parseColor("#3D7EFF")
        val bg      = if (isDark) Color.parseColor("#E00F0C1E") else Color.parseColor("#E0E8F3FF")
        val fg      = if (isDark) Color.WHITE else Color.parseColor("#0D1E35")
        val fgSub   = if (isDark) Color.parseColor("#AAAAAA") else Color.parseColor("#666666")
        val redClr  = Color.parseColor("#EF4444")
        val p       = (14 * dp).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER_HORIZONTAL
            setPadding(p, (14 * dp).toInt(), p, (14 * dp).toInt())
            background  = GradientDrawable().apply {
                setColor(bg)
                cornerRadius = 40f * dp
                setStroke((2 * dp).toInt(), accent)
            }
        }

        // ── 第一行：缩小按钮 + 关闭按钮 ──────────────────────────────────────
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
        }

        // 缩小按钮（左）
        topRow.addView(TextView(this).apply {
            text     = "收起"
            textSize = 10f
            setTextColor(fgSub)
            setPadding(0, 0, (10 * dp).toInt(), 0)
            setOnClickListener { handler.post { showCollapsed() } }
        })

        // 占位撑开
        topRow.addView(android.widget.Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        })

        // 关闭按钮（右）
        topRow.addView(TextView(this).apply {
            text     = "关闭辅助"
            textSize = 10f
            setTextColor(redClr)
            setOnClickListener {
                // 全屏二次确认弹窗
                handler.post { showStopConfirmDialog() }
            }
        })

        root.addView(topRow)

        // ── 分割线 ────────────────────────────────────────────────────────────
        root.addView(android.view.View(this).apply {
            setBackgroundColor(if (isDark) Color.parseColor("#33FFFFFF") else Color.parseColor("#22000000"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (1 * dp).toInt()
            ).apply { setMargins(0, (6 * dp).toInt(), 0, (10 * dp).toInt()) }
        })

        // ── 图标 ──────────────────────────────────────────────────────────────
        root.addView(TextView(this).apply {
            text     = if (isHero) "⚔️" else "🏁"
            textSize = 22f; gravity = Gravity.CENTER
        })

        // ── 主提示文字 ────────────────────────────────────────────────────────
        root.addView(TextView(this).apply {
            text = if (isHero)
                "已进入选人界面了吗？\n\n确认后辅助自动启动"
            else
                "这局打完了吗？\n\n确认后自动重启下一局"
            textSize = 12f; setTextColor(fg); gravity = Gravity.CENTER
            setPadding(0, (8 * dp).toInt(), 0, (10 * dp).toInt())
        })

        // ── 主操作按钮 ────────────────────────────────────────────────────────
        root.addView(android.widget.Button(this).apply {
            text = if (isHero) "✓  进了，启动辅助" else "✓  打完了，下一局"
            textSize = 12f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                colors = if (isDark) intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#9B6DFF"))
                         else        intArrayOf(Color.parseColor("#3D7EFF"), Color.parseColor("#5B8EFF"))
                orientation  = GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = 28f * dp
            }
            val hp = (18 * dp).toInt(); val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            setOnClickListener {
                removeFloat()
                if (isHero) onConfirmHero?.invoke() else onConfirmMatchEnd?.invoke()
            }
        })

        addToWindow(root, (148 * dp).toInt())
        upgradeForeground()
    }

    // ── 关闭辅助二次确认（AlertDialog 全屏弹窗）─────────────────────────────
    private fun showStopConfirmDialog() {
        val isDark = isNightTime()
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("⚠️  确认关闭辅助？")
        builder.setMessage("点击确认后将：\n\n• 停止所有辅助进程\n• 关闭游戏\n• 退出辅助服务\n\n如需重新使用，请回到启动器重新启动。")
        builder.setPositiveButton("确认关闭") { _, _ ->
            removeFloat()
            // 执行 stop.sh 并通知 ViewModel
            Thread {
                try {
                    val stopPath = AssetUtil.extractScript(this, "stop.sh")
                    RootUtil.execScript(stopPath)
                    Log.d(TAG, "stop.sh done")
                } catch (e: Exception) {
                    Log.e(TAG, "stop.sh failed: ${e.message}")
                }
                onStopAll?.invoke()
            }.start()
        }
        builder.setNegativeButton("取消", null)
        val dialog = builder.create()
        dialog.window?.setType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        )
        dialog.show()
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
                setColor(Color.argb(0xBB,
                    Color.red(accent), Color.green(accent), Color.blue(accent)))
                setStroke((2 * dp).toInt(), accent)
            }
            setOnClickListener { handler.post { showExpanded() } }
        }
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
        upgradeForeground()
    }

    // ── WindowManager addView ─────────────────────────────────────────────────
    private fun addToWindow(
        view: android.view.View,
        width: Int,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        offsetX: Int = (12 * resources.displayMetrics.density).toInt()
    ) {
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
            Log.d(TAG, "addToWindow OK")
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
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY).let { it < 6 || it >= 18 }
}
