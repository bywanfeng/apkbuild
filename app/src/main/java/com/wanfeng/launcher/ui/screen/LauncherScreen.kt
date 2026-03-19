package com.wanfeng.launcher.ui.screen

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanfeng.launcher.ui.theme.AccentBlue
import com.wanfeng.launcher.ui.theme.AccentCyan
import com.wanfeng.launcher.ui.theme.AccentGreen
import com.wanfeng.launcher.ui.theme.AccentIndigo
import com.wanfeng.launcher.ui.theme.CardGreen
import com.wanfeng.launcher.ui.theme.CardPurple
import com.wanfeng.launcher.ui.theme.CardRed
import com.wanfeng.launcher.ui.theme.DarkBg0
import com.wanfeng.launcher.ui.theme.DarkBg100
import com.wanfeng.launcher.ui.theme.DarkBg45
import com.wanfeng.launcher.ui.theme.LaunchEnd
import com.wanfeng.launcher.ui.theme.LaunchMid
import com.wanfeng.launcher.ui.theme.LaunchStart
import com.wanfeng.launcher.ui.theme.LightBg0
import com.wanfeng.launcher.ui.theme.LightBg100
import com.wanfeng.launcher.ui.theme.LightBg50
import com.wanfeng.launcher.ui.theme.StatusAmber
import com.wanfeng.launcher.ui.theme.StatusGreen
import com.wanfeng.launcher.ui.theme.StatusRed
import com.wanfeng.launcher.ui.theme.ToastBg
import com.wanfeng.launcher.ui.theme.ToastText
import kotlinx.coroutines.delay
import java.util.Calendar

private fun Long.toComposeColor() = Color(this)
private val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
private data class Greeting(val main: String, val sub: String)
private fun getGreeting(): Greeting {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 0..4  -> Greeting("夜深了，辛苦了。", "如需继续操作，建议先确认设备与网络状态。")
        in 5..7  -> Greeting("早上好，欢迎使用。", "建议先查看今日公告，再开始本次操作。")
        in 8..11 -> Greeting("上午好，准备就绪。", "可直接查看状态、启动服务和处理常用操作。")
        in 12..13 -> Greeting("中午好，先休息一下。", "操作前确认环境正常，能减少重复处理。")
        in 14..17 -> Greeting("下午好，继续处理吧。", "当前状态会实时同步，便于快速判断下一步。")
        in 18..20 -> Greeting("晚上好，欢迎回来。", "开始前先看一眼状态信息，会更省时间。")
        else     -> Greeting("今天辛苦了。", "若还需继续操作，建议完成后及时检查结果。")
    }
}

@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark
    val isBusy = state.loadingBtn != ButtonKey.NONE
    var showNotice by rememberSaveable { mutableStateOf(false) }
    var showGuide  by rememberSaveable { mutableStateOf(false) }
    var autoShown  by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.announcement) {
        if (state.announcement.isNotBlank() && !autoShown) { showNotice = true; autoShown = true }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            if (isDark) Brush.linearGradient(arrayOf(0f to DarkBg0, 0.45f to DarkBg45, 1f to DarkBg100), Offset(0f,0f), Offset(900f,2200f))
            else        Brush.linearGradient(arrayOf(0f to LightBg0, 0.52f to LightBg50, 1f to LightBg100), Offset(0f,0f), Offset(900f,2200f))
        )
    ) {
        BackgroundBlobs(isDark)

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            AnimSec(0)   { HeaderRow(isDark, vm::toggleTheme) }
            AnimSec(60)  { HeroCard(isDark, state, state.gameQuote, { showNotice = true }, { showGuide = true }) }
            // ← 运行状态面板已删除
            AnimSec(120) { LaunchButton(state.loadingBtn == ButtonKey.LAUNCH, !isBusy, isDark, vm::onLaunch) }
            AnimSec(160) { ActionList(isDark, state, !isBusy, vm) }
            AnimSec(200) { FooterRow(isDark, isBusy) }
            Spacer(Modifier.height(8.dp))
        }

        ToastLayer(state.toasts, vm::removeToast)

        InfoDialog(showNotice, isDark, "系统公告", "请先阅读公告内容后再继续操作。",
            state.announcement, "如需再次查看，可在首页点击"查看公告"。") { showNotice = false }
        InfoDialog(showGuide, isDark, "使用教程", "已将教程与公告分开显示，便于查阅。",
            state.tutorial, "如需再次查看，可在首页点击"使用教程"。") { showGuide = false }

        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                confirmButton = { TextButton(vm::dismissDialog) { Text("确定", color = AccentBlue) } },
                title = { Text("提示", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                text  = { Text(state.dialogMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
            )
        }
    }
}

@Composable
private fun AnimSec(delay: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(true,
        enter = fadeIn(tween(480, delay)) + slideInVertically(tween(480, delay, FastOutSlowInEasing)) { it / 5 }
    ) { content() }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderRow(isDark: Boolean, onToggleTheme: () -> Unit) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); time = Calendar.getInstance() } }
    val timeStr = remember(time) { "%02d:%02d".format(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE)) }
    val dateStr = remember(time) {
        val d = arrayOf("周日","周一","周二","周三","周四","周五","周六")
        "${time.get(Calendar.MONTH)+1}月${time.get(Calendar.DAY_OF_MONTH)}日 ${d[time.get(Calendar.DAY_OF_WEEK)-1]}"
    }
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppLogoCompose(42.dp, isDark)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("WANFENG STUDIO", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("三角洲服务面板", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), shadowElevation = if (isDark) 0.dp else 2.dp) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                    Text(timeStr, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // 主题切换：浅色模式用白底+极淡蓝边，不会发暗
            Surface(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(14.dp))
                    .clickable(remember { MutableInteractionSource() }, null, onClick = onToggleTheme),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.14f) else AccentBlue.copy(alpha = 0.18f)),
            ) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text(if (isDark) "☀" else "◐", fontSize = 16.sp) } }
        }
    }
}

@Composable
private fun AppLogoCompose(size: Dp, isDark: Boolean) {
    Box(modifier = Modifier.size(size).clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(AccentBlue, AccentIndigo, AccentCyan))).padding(1.2.dp),
        contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp))
            .background(if (isDark) Color(0xFF09101D) else Color(0xFFF2F7FF))
            .border(1.dp, if (isDark) Color.White.copy(0.08f) else AccentBlue.copy(0.12f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(size * 0.52f).clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(AccentBlue.copy(0.14f), AccentCyan.copy(0.08f))))
                .border(1.dp, AccentBlue.copy(0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Text("▲", fontSize = (size.value * 0.32f).sp, color = AccentCyan, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── HeroCard ──────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(isDark: Boolean, state: LauncherUiState, quote: String, onShowNotice: () -> Unit, onShowGuide: () -> Unit) {
    val g = remember { getGreeting() }
    Card(RoundedCornerShape(24.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        CardDefaults.cardElevation(if (isDark) 2.dp else 6.dp), Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(if (isDark) listOf(Color(0xFF10192D), MaterialTheme.colorScheme.surface) else listOf(Color(0xFFF2F7FF), MaterialTheme.colorScheme.surface)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 状态 Chip 行 — 服务器离线时隐藏
            Row(Arrangement.spacedBy(6.dp), Alignment.CenterVertically) {
                MiniChip("游戏 ${state.gameStatus.label()}", state.gameStatus.dotColor(), isDark)
                MiniChip("辅助 ${state.auxStatus.label()}", state.auxStatus.dotColor(), isDark)
                if (state.serverStatus != ConnStatus.DISCONNECTED)
                    MiniChip("服务器 ${state.serverStatus.label()}", state.serverStatus.dotColor(), isDark)
            }
            Text(g.main, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(g.sub,  style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)

            // 寄语（guli.txt 随机一行）
            if (quote.isNotBlank()) {
                Surface(color = if (isDark) AccentBlue.copy(0.09f) else AccentBlue.copy(0.06f), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                        Text("💬", fontSize = 13.sp)
                        Text(quote, style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) AccentBlue.copy(0.88f) else AccentBlue, lineHeight = 17.sp)
                    }
                }
            }

            // 按钮行 — 浅色模式 OutlinedButton 边框极淡，不发暗
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Button(onShowNotice, Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(AccentBlue, Color.White),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                    Text("查看公告", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onShowGuide, Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDark) AccentBlue.copy(0.30f) else AccentBlue.copy(0.20f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) Color.Transparent else Color(0xFFF5F9FF),
                        contentColor = AccentBlue),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                    Text("使用教程", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, accent: Color, isDark: Boolean) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp))
            .background(accent.copy(if (isDark) 0.15f else 0.08f))
            .border(1.dp, accent.copy(if (isDark) 0.22f else 0.13f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        Arrangement.spacedBy(5.dp), Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).background(accent, CircleShape))
        Text(text, fontSize = 10.sp, color = if (isDark) accent else accent.copy(0.82f), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PulseDot(color: Color, pulse: Boolean) {
    val a by if (pulse) rememberInfiniteTransition("dot").animateFloat(0.35f, 0.95f, infiniteRepeatable(tween(900), RepeatMode.Reverse), "p")
              else remember { mutableStateOf(1f) }
    Box(Modifier.size(10.dp), Alignment.Center) {
        if (pulse) Box(Modifier.size(10.dp).background(color.copy(a * 0.35f), CircleShape))
        Box(Modifier.size(6.dp).background(color, CircleShape))
    }
}

// ── LaunchButton ──────────────────────────────────────────────────────────────
@Composable
private fun LaunchButton(loading: Boolean, enabled: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val bp by rememberInfiniteTransition("lb").animateFloat(0.35f, 0.82f, infiniteRepeatable(tween(2200, easing = EaseInOut), RepeatMode.Reverse), "lbB")
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp),
        CardDefaults.cardColors(Color.Transparent), CardDefaults.cardElevation(if (isDark) 2.dp else 8.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(LaunchStart,LaunchMid,LaunchEnd), Offset(0f,0f), Offset(1200f,280f)))
                .border(1.dp, Color.White.copy(bp * 0.22f), RoundedCornerShape(24.dp))
                .alpha(if (enabled || loading) 1f else 0.72f)
                .clickable(remember { MutableInteractionSource() }, null) { if (enabled && !loading) onClick() }
                .padding(horizontal = 16.dp, vertical = 13.dp),
        ) {
            Box(Modifier.matchParentSize().background(Brush.linearGradient(listOf(Color.White.copy(0.13f), Color.Transparent), Offset(0f,0f), Offset(780f,320f))))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                    Surface(Color.White.copy(0.16f), RoundedCornerShape(16.dp)) {
                        Box(Modifier.size(44.dp), Alignment.Center) {
                            if (loading) {
                                val r by rememberInfiniteTransition("spin").animateFloat(0f,360f,infiniteRepeatable(tween(900,easing=LinearEasing)),"r")
                                Text("↻", Color.White, 20.sp, modifier = Modifier.graphicsLayer { rotationZ = r })
                            } else Text("▶", Color.White, 17.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(when { loading -> "正在启动中…"; !enabled -> "请等待当前任务完成"; else -> "开始启动" },
                            style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(if (loading) "正在执行启动流程，请勿重复点击" else "一键完成当前启动流程",
                            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.78f))
                    }
                }
                Text("›", fontSize = 22.sp, color = Color.White.copy(0.75f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── ActionList ────────────────────────────────────────────────────────────────
@Composable
private fun ActionList(isDark: Boolean, state: LauncherUiState, enabled: Boolean, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("快捷操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        WideActionCard("🔄","重启辅助程序","重新载入辅助模块",CardPurple,state.loadingBtn==ButtonKey.RESTART_AUX,enabled,isDark,vm::onRestartAux)
        WideActionCard("🚫","完全关闭辅助","停止所有相关进程",CardRed,   state.loadingBtn==ButtonKey.CLOSE_ALL,   enabled,isDark,vm::onCloseAll)
        WideActionCard("🛡","清理防盗号",  "清理设备并加固账号",CardGreen,state.loadingBtn==ButtonKey.CLEAN,       enabled,isDark,vm::onClean)
    }
}

@Composable
private fun WideActionCard(icon: String, title: String, sub: String, accent: Color, loading: Boolean, enabled: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val sc by animateFloatAsState(if (loading) 0.985f else 1f, label = title)
    val rot by if (loading) rememberInfiniteTransition("cs$title").animateFloat(0f,360f,infiniteRepeatable(tween(900,easing=LinearEasing)),"cr$title")
               else remember { mutableStateOf(0f) }
    Card(
        Modifier.fillMaxWidth().graphicsLayer { scaleX = sc; scaleY = sc }
            .alpha(if (enabled || loading) 1f else 0.68f)
            .clickable(remember { MutableInteractionSource() }, null) { if (enabled && !loading) onClick() },
        RoundedCornerShape(20.dp),
        CardDefaults.cardColors(if (isDark) accent.copy(0.12f) else accent.copy(0.06f)),
        CardDefaults.cardElevation(if (isDark) 1.dp else 4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth()
                // 浅色模式 border alpha 0.10f，远低于之前的 0.16f，不再发暗
                .border(1.dp, accent.copy(if (isDark) 0.20f else 0.10f), RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(accent.copy(if (isDark) 0.08f else 0.04f), Color.Transparent), Offset(0f,0f), Offset(1200f,280f)))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically,
        ) {
            Row(Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                Surface(accent.copy(if (isDark) 0.16f else 0.10f), RoundedCornerShape(16.dp)) {
                    Box(Modifier.size(42.dp), Alignment.Center) {
                        Text(icon, color = accent, fontSize = 19.sp,
                            modifier = if (loading) Modifier.graphicsLayer { rotationZ = rot } else Modifier)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (!enabled && !loading) "当前有任务执行中" else sub,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(accent.copy(if (isDark) 0.15f else 0.09f), RoundedCornerShape(999.dp)) {
                Text(if (loading) "执行中" else "进入",
                    Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) accent else accent.copy(0.88f))
            }
        }
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────
@Composable
private fun rememberAppVersionName(): String {
    val ctx = LocalContext.current
    return remember(ctx) {
        runCatching {
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.PackageInfoFlags.of(0))
            else @Suppress("DEPRECATION") ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pi.versionName ?: "unknown"
        }.getOrDefault("unknown")
    }
}

@Composable
private fun FooterRow(isDark: Boolean, busy: Boolean) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp),
        CardDefaults.cardColors(if (isDark) Color.White.copy(0.04f) else Color(0xFFF4F8FF)),
        CardDefaults.cardElevation(if (isDark) 0.dp else 2.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("晚风工作室", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Text("版本 v${rememberAppVersionName()} · 客户服务版", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MiniChip(if (busy) "处理中" else "服务正常", if (busy) AccentBlue else AccentGreen, isDark)
        }
    }
}

// ── InfoDialog ────────────────────────────────────────────────────────────────
@Composable
private fun InfoDialog(visible: Boolean, isDark: Boolean, title: String, subtitle: String, bodyText: String, footerText: String, onDismiss: () -> Unit) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text("知道了", color = AccentBlue) } },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(if (isDark) StatusAmber.copy(0.10f) else StatusAmber.copy(0.07f), RoundedCornerShape(14.dp)) {
                    Text(bodyText.ifBlank { "暂无内容" }.trim(), Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp, style = MaterialTheme.typography.bodyMedium)
                }
                Text(footerText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
    )
}

// ── Toast ─────────────────────────────────────────────────────────────────────
@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(Modifier.fillMaxSize().zIndex(10f), Alignment.TopCenter) {
        Column(Modifier.statusBarsPadding().padding(top = 10.dp), Arrangement.spacedBy(7.dp), Alignment.CenterHorizontally) {
            AnimatedContent(toasts, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tl") { list ->
                Column(Arrangement.spacedBy(7.dp), Alignment.CenterHorizontally) {
                    list.forEach { t -> ToastItem(t) { onRemove(t.id) } }
                }
            }
        }
    }
}

@Composable
private fun ToastItem(toast: ToastData, onDone: () -> Unit) {
    LaunchedEffect(toast.id) { delay(3200); onDone() }
    val dot = toast.color.toComposeColor()
    Row(
        Modifier.widthIn(max = 340.dp).clip(RoundedCornerShape(14.dp))
            .background(ToastBg).border(1.dp, dot.copy(0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        Arrangement.spacedBy(9.dp), Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).background(dot, CircleShape))
        Text(toast.msg, fontSize = 13.sp, color = ToastText, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ── BackgroundBlobs ───────────────────────────────────────────────────────────
@Composable
private fun BackgroundBlobs(isDark: Boolean) {
    val drift by rememberInfiniteTransition("blobs").animateFloat(0f,1f,infiniteRepeatable(tween(9000,easing=EaseInOut),RepeatMode.Reverse),"drift")
    Box(Modifier.fillMaxSize().zIndex(0f)) {
        Box(Modifier.size(260.dp).graphicsLayer { translationX=-60f+18f*drift; translationY=-40f }.blur(90.dp)
            .background(Brush.radialGradient(listOf(if(isDark) AccentBlue.copy(0.11f) else AccentBlue.copy(0.05f), Color.Transparent)), CircleShape))
        Box(Modifier.size(200.dp).align(Alignment.TopEnd).graphicsLayer { translationX=16f*drift; translationY=-18f+20f*drift }.blur(84.dp)
            .background(Brush.radialGradient(listOf(if(isDark) AccentCyan.copy(0.09f) else AccentCyan.copy(0.04f), Color.Transparent)), CircleShape))
        Box(Modifier.size(220.dp).align(Alignment.BottomEnd).graphicsLayer { translationX=38f-22f*drift; translationY=56f }.blur(88.dp)
            .background(Brush.radialGradient(listOf(if(isDark) AccentIndigo.copy(0.10f) else AccentIndigo.copy(0.04f), Color.Transparent)), CircleShape))
    }
}

// ── 扩展函数 ──────────────────────────────────────────────────────────────────
private fun RunStatus.label() = when(this) { RunStatus.RUNNING->"运行中"; RunStatus.LOADING->"启动中"; RunStatus.STOPPED->"已停止" }
private fun RunStatus.dotColor() = when(this) { RunStatus.RUNNING->StatusGreen; RunStatus.LOADING->StatusAmber; RunStatus.STOPPED->StatusRed }
// ↓ 服务器断开显示"离线"，并在 HeroCard 中离线时隐藏 Chip
private fun ConnStatus.label() = when(this) { ConnStatus.CONNECTED->"已连接"; ConnStatus.CONNECTING->"连接中"; ConnStatus.DISCONNECTED->"离线" }
private fun ConnStatus.dotColor() = when(this) { ConnStatus.CONNECTED->StatusGreen; ConnStatus.CONNECTING->StatusAmber; ConnStatus.DISCONNECTED->StatusRed }
