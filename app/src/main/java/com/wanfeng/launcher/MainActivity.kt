package com.wanfeng.launcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanfeng.launcher.service.NotificationUtil
import com.wanfeng.launcher.ui.screen.LauncherScreen
import com.wanfeng.launcher.ui.screen.LauncherViewModel
import com.wanfeng.launcher.ui.theme.WanfengLauncherTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        NotificationUtil.createChannel(this)
        ensureNotificationPermission()

        setContent {
            val vm: LauncherViewModel = viewModel()
            val state by vm.uiState.collectAsState()

            WanfengLauncherTheme(darkTheme = state.isDark) {
                LauncherScreen(vm = vm)
            }
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) return

        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
