package com.wanfeng.launcher.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wanfeng.launcher.R

object NotificationUtil {

    const val CHANNEL_ID = "wf_security_status"
    const val NOTIF_ID_SECURITY = 1001

    /**
     * 创建通知 Channel（Android 8+ 必须，幂等调用）。
     * 在 Application / MainActivity.onCreate 中调用一次即可。
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(false)
                setSound(null, null)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * 发送"安全扫描"通知。
     * 标题：🛡️ 安全扫描
     * 内容：状态：正常 | 防护：已启用
     */
    fun sendSecurityNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(null)
            .setSound(null)
            .build()

        nm.notify(NOTIF_ID_SECURITY, notification)
    }
}
