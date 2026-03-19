package com.wanfeng.launcher.service

import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.util.Log

/**
 * SimpleVpnService — 辅助工具通过 am startservice 拉起此服务。
 * 实际 VPN 隧道逻辑由 libaxel.so / run.sh 实现；
 * 此处为 Android 系统侧的占位入口，确保 Service 声明合法。
 *
 * 后续填充：
 *  - onStartCommand 中 prepare() → establish() 建立隧道
 *  - 读取 /data/local/tmp 中解压出的配置文件
 *  - 通过 libaxel.so JNI 或 exec 驱动实际代理
 */
class SimpleVpnService : VpnService() {

    companion object {
        private const val TAG = "SimpleVpnService"
        const val ACTION_START = "com.wanfeng.launcher.VPN_START"
        const val ACTION_STOP  = "com.wanfeng.launcher.VPN_STOP"
    }

    override fun onBind(intent: Intent?): IBinder? {
        // VpnService 不通过 onBind 使用；保留父类实现
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // TODO: 在此实现 VPN 隧道建立逻辑
                Log.d(TAG, "VPN service started (stub)")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SimpleVpnService destroyed")
    }
}
