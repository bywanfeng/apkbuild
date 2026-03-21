package com.wanfeng.launcher.service

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

object OverlayPermissionManager {

    private const val TAG = "OverlayPerm"

    /**
     * 检查悬浮窗权限是否已授予。
     * 部分系统（如精简 AOSP 云手机）可能不支持该 API，返回 true 跳过。
     */
    fun hasPermission(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val ok = Settings.canDrawOverlays(context)
                Log.d(TAG, "canDrawOverlays=$ok")
                ok
            } else {
                // Android 6 以下无需动态授权
                true
            }
        } catch (e: Exception) {
            // 系统不支持该 API（极简 AOSP / 定制云手机），直接跳过
            Log.w(TAG, "canDrawOverlays not supported, skip: ${e.message}")
            true
        }
    }

    /**
     * 尝试用 root appops 命令自授权悬浮窗，无需用户跳转设置页。
     * 兼容主流 Android 版本的 appops 命令格式。
     * 如果授权失败（系统不支持），静默忽略。
     */
    fun requestByRoot(context: Context): Boolean {
        if (hasPermission(context)) {
            Log.d(TAG, "already granted, skip")
            return true
        }
        val pkg = context.packageName
        Log.d(TAG, "granting SYSTEM_ALERT_WINDOW via appops for $pkg")

        // 尝试多种命令格式（不同 Android 版本 appops 命令参数不同）
        val cmds = listOf(
            "appops set $pkg SYSTEM_ALERT_WINDOW allow",
            "appops set --uid ${getUid(context)} SYSTEM_ALERT_WINDOW allow",
            "cmd appops set $pkg android:system_alert_window allow",
        )
        for (cmd in cmds) {
            try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val err = p.errorStream.bufferedReader().readText().trim()
                val code = p.waitFor()
                Log.d(TAG, "cmd='$cmd' exitCode=$code err='$err'")
                if (code == 0) {
                    // 再验证一次
                    val granted = hasPermission(context)
                    Log.d(TAG, "after appops grant: $granted")
                    if (granted) return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "cmd failed: $cmd err=${e.message}")
            }
        }
        Log.w(TAG, "all appops attempts failed, overlay may not work")
        return false
    }

    /**
     * 获取应用 UID（用于部分需要 --uid 的 appops 命令）
     */
    private fun getUid(context: Context): Int {
        return try {
            context.packageManager.getApplicationInfo(context.packageName, 0).uid
        } catch (e: Exception) { -1 }
    }

    /**
     * 在 MainActivity.onCreate 调用：
     * 1. 已有权限 → 直接返回
     * 2. 无权限 → root 自动授权
     * 3. 系统不支持 → 跳过
     */
    fun ensurePermission(context: Context) {
        if (hasPermission(context)) return
        val granted = requestByRoot(context)
        if (!granted) {
            Log.w(TAG, "overlay permission not granted, floating window may not display")
        }
    }
}
