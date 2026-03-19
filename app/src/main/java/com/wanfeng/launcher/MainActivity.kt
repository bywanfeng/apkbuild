package com.wanfeng.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import com.wanfeng.launcher.service.NotificationUtil
import com.wanfeng.launcher.ui.screen.LauncherScreen
import com.wanfeng.launcher.ui.screen.LauncherViewModel
import com.wanfeng.launcher.ui.theme.WanfengLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏边到边显示
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 创建通知 Channel（Android 8+，幂等）
        NotificationUtil.createChannel(this)

        setContent {
            val vm: LauncherViewModel = viewModel()
            val state by vm.uiState.collectAsState()

            WanfengLauncherTheme(darkTheme = state.isDark) {
                LauncherScreen(vm = vm)
            }
        }
    }
}
