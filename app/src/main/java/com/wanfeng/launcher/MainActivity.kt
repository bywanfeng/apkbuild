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
        // 悬浮窗权限
        com.wanfeng.launcher.service.OverlayPermissionManager.ensurePermission(this)

        // 预启动全局悬浮窗 Service（先建好 WindowManager，用户点击时可立即显示）
        val floatIntent = android.content.Intent(this, com.wanfeng.launcher.service.GlobalFloatService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            startForegroundService(floatIntent)
        else
            startService(floatIntent)

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
