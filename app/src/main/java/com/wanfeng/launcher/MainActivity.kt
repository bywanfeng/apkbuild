package com.wanfeng.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.wanfeng.launcher.service.NotificationUtil
import com.wanfeng.launcher.ui.screen.LauncherScreen
import com.wanfeng.launcher.ui.screen.LauncherViewModel
import com.wanfeng.launcher.ui.theme.WanfengLauncherTheme

class MainActivity : ComponentActivity() {

    private val vm: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 预启动全局悬浮窗 Service（普通 startService，不需要前台）
        startService(android.content.Intent(this, com.wanfeng.launcher.service.GlobalFloatService::class.java))

        // 在后台线程做权限授权（appops 需要等待系统缓存更新，不能阻塞主线程）
        Thread {
            com.wanfeng.launcher.service.OverlayPermissionManager.ensurePermission(this)
        }.start()

        NotificationUtil.createChannel(this)

        setContent {
            WanfengLauncherTheme(darkTheme = true) {
                LauncherScreen(vm = vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到前台自动按时间刷新主题
        vm.refreshThemeByTime()
    }
}
