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
        // 悬浮窗权限：已有跳过，没有用 root 授权，系统不支持静默忽略
        com.wanfeng.launcher.service.OverlayPermissionManager.ensurePermission(this)

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
