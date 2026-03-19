package com.wanfeng.launcher.ui.screen

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
        in 0..4  -> Greeting(
            main = "夜深了，注意休息。",
            sub  = "该休息了，明天继续。"
        )
        in 5..7  -> Greeting(
            main = "早上好，今天也加油！",
            sub  = "新的一天，状态不错。"
        )
        in 8..11 -> Greeting(
            main = "上午好，状态不错。",
            sub  = "一切就绪，随时可以开始。"
        )
        in 12..13 -> Greeting(
            main = "中午了，先去吃饭吧。",
            sub  = "吃好喝好，下午更有力气。"
        )
        in 14..17 -> Greeting(
            main = "下午好，继续加油。",
            sub  = "稳住，收尾阶段往往是关键。"
        )
        in 18..20 -> Greeting(
            main = "晚上好，欢迎回来。",
            sub  = "今晚也拜托你了。"
        )
        else     -> Greeting(
            main = "辛苦了，今天也谢谢你。",
            sub  = "收工了，好好休息。"
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark
    val isBusy = state.loadingBtn != ButtonKey.NONE
    var showNotice by rememberSaveable { mutableStateOf(false) }
    var showGuide  by rememberSaveable { mutableStateOf(false) }
    var autoShown  by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.announcement) {
        if (state.announcement.isNotBlank() && !autoShown) {
            showNotice = true
            autoShown  = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) {
                    Brush.linearGradient(
                        colorStops = arrayOf(0f to DarkBg0, 0.45f to DarkBg45, 1f to DarkBg100),
                        start = Offset(0f, 0f), end = Offset(900f, 2200f),
                    )
                } else {
                    Brush.linearGradient(
                        colorStops = arrayOf(0f to LightBg0, 0.52f to LightBg50, 1f to LightBg100),
                        start = Offset(0f, 0f), end = Offset(900f, 2200f),
                    )
                }
            )
    ) {
        BackgroundBlobs(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            AnimSec(delayMs = 0)   { HeaderRow(isDark = isDark, onToggleTheme = vm::toggleTheme) }
            AnimSec(delayMs = 60)  { HeroCard(isDark = isDark, state = state, quote = state.gameQuote, onShowNotice = { showNotice = true }, onShowGuide = { showGuide = true }) }
            AnimSec(delayMs = 120) { LaunchButton(loading = state.loadingBtn == ButtonKey.LAUNCH, enabled = !isBusy, isDark = isDark, onClick = vm::onLaunch) }
            AnimSec(delayMs = 160) { ActionList(isDark = isDark, state = state, enabled = !isBusy, vm = vm) }
            AnimSec(delayMs = 200) { FooterRow(isDark = isDark, busy = isBusy) }
            Spacer(modifier = Modifier.height(8.dp))
        }

        ToastLayer(toasts = state.toasts, onRemove = vm::removeToast)

        InfoDialog(
            visible = showNotice, isDark = isDark,
            title = "系统公告", subtitle = "请阅读后再继续操作。",
            bodyText = state.announcement, footerText = "可随时点击首页按钮再次查看。",
            onDismiss = { showNotice = false },
        )
        InfoDialog(
            visible = showGuide, isDark = isDark,
            title = "使用教程", subtitle = "操作说明，按需查阅。",
            bodyText = state.tutorial, footerText = "可随时点击首页按钮再次查看。",
            onDismiss = { showGuide = false },
        )

        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                confirmButton = {
                    TextButton(onClick = vm::dismissDialog) {
                        Text(text = "确定", color = AccentBlue)
                    }
                },
                title = {
                    Text(
                        text = "提示",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                text = {
                    Text(
                        text = state.dialogMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp,
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(size = 24.dp),
            )
        }
    }
}

@Composable
private fun AnimSec(delayMs: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 480, delayMillis = delayMs)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 480, delayMillis = delayMs, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 5 },
            ),
    ) {
        content()
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderRow(isDark: Boolean, onToggleTheme: () -> Unit) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000L); time = Calendar.getInstance() }
    }
    val timeStr = remember(time) {
        "%02d:%02d".format(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
    }
    val dateStr = remember(time) {
        val days = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        "${time.get(Calendar.MONTH) + 1}月${time.get(Calendar.DAY_OF_MONTH)}日 ${days[time.get(Calendar.DAY_OF_WEEK) - 1]}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
        ) {
            AppLogoCompose(size = 42.dp, isDark = isDark)
            Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                Text(
                    text = "WANFENG STUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "三角洲服务面板",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(size = 14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = if (isDark) 0.dp else 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(text = timeStr, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                modifier = Modifier
                    .size(size = 38.dp)
                    .clip(RoundedCornerShape(size = 14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTheme,
                    ),
                shape = RoundedCornerShape(size = 14.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
                            else AccentBlue.copy(alpha = 0.18f),
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isDark) "☀" else "◐", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun AppLogoCompose(size: Dp, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(size = size)
            .clip(RoundedCornerShape(size = 14.dp))
            .background(brush = Brush.linearGradient(colors = listOf(AccentBlue, AccentIndigo, AccentCyan)))
            .padding(all = 1.2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(size = 13.dp))
                .background(color = if (isDark) Color(0xFF09101D) else Color(0xFFF2F7FF))
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else AccentBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(size = 13.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size = size * 0.52f)
                    .clip(RoundedCornerShape(size = 10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentBlue.copy(alpha = 0.14f), AccentCyan.copy(alpha = 0.08f)),
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = AccentBlue.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(size = 10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "▲",
                    fontSize = (size.value * 0.32f).sp,
                    color = AccentCyan,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ── HeroCard ──────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    isDark: Boolean,
    state: LauncherUiState,
    quote: String,
    onShowNotice: () -> Unit,
    onShowGuide: () -> Unit,
) {
    val greeting = remember { getGreeting() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(Color(0xFF10192D), MaterialTheme.colorScheme.surface)
                                 else        listOf(Color(0xFFF2F7FF), MaterialTheme.colorScheme.surface),
                    )
                )
                .padding(all = 14.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            // 状态 Chip 行 — 服务器离线时隐藏 Chip
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiniChip(text = "游戏 ${state.gameStatus.label()}", accent = state.gameStatus.dotColor(), isDark = isDark)
                MiniChip(text = "辅助 ${state.auxStatus.label()}", accent = state.auxStatus.dotColor(), isDark = isDark)
                if (state.serverStatus != ConnStatus.DISCONNECTED) {
                    MiniChip(text = "服务器 ${state.serverStatus.label()}", accent = state.serverStatus.dotColor(), isDark = isDark)
                }
            }

            Text(
                text = greeting.main,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = greeting.sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )

            // 寄语（guli.txt 随机读取一行）
            if (quote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(size = 14.dp),
                    color = if (isDark) AccentBlue.copy(alpha = 0.09f) else AccentBlue.copy(alpha = 0.06f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "💬", fontSize = 13.sp)
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) AccentBlue.copy(alpha = 0.88f) else AccentBlue,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }

            // 公告 / 教程 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            ) {
                Button(
                    onClick = onShowNotice,
                    modifier = Modifier.weight(weight = 1f),
                    shape = RoundedCornerShape(size = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Text(text = "查看公告", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = onShowGuide,
                    modifier = Modifier.weight(weight = 1f),
                    shape = RoundedCornerShape(size = 16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isDark) AccentBlue.copy(alpha = 0.30f) else AccentBlue.copy(alpha = 0.20f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) Color.Transparent else Color(0xFFF5F9FF),
                        contentColor = AccentBlue,
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Text(text = "使用教程", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, accent: Color, isDark: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 999.dp))
            .background(color = accent.copy(alpha = if (isDark) 0.15f else 0.08f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (isDark) 0.22f else 0.13f),
                shape = RoundedCornerShape(size = 999.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(size = 5.dp).background(color = accent, shape = CircleShape))
        Text(
            text = text,
            fontSize = 10.sp,
            color = if (isDark) accent else accent.copy(alpha = 0.82f),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PulseDot(color: Color, pulse: Boolean) {
    val alpha by if (pulse) {
        rememberInfiniteTransition(label = "dot").animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "pulse",
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    Box(modifier = Modifier.size(size = 10.dp), contentAlignment = Alignment.Center) {
        if (pulse) {
            Box(modifier = Modifier.size(size = 10.dp).background(color = color.copy(alpha = alpha * 0.35f), shape = CircleShape))
        }
        Box(modifier = Modifier.size(size = 6.dp).background(color = color, shape = CircleShape))
    }
}

// ── LaunchButton ──────────────────────────────────────────────────────────────
@Composable
private fun LaunchButton(loading: Boolean, enabled: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val borderPulse by rememberInfiniteTransition(label = "lb").animateFloat(
        initialValue = 0.35f,
        targetValue  = 0.82f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOut), RepeatMode.Reverse),
        label = "lbB",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(size = 24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LaunchStart, LaunchMid, LaunchEnd),
                        start = Offset(0f, 0f), end = Offset(1200f, 280f),
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = borderPulse * 0.22f),
                    shape = RoundedCornerShape(size = 24.dp),
                )
                .alpha(alpha = if (enabled || loading) 1f else 0.72f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { if (enabled && !loading) onClick() },
                )
                .padding(horizontal = 16.dp, vertical = 13.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.13f), Color.Transparent),
                            start = Offset(0f, 0f), end = Offset(780f, 320f),
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(size = 16.dp),
                        color = Color.White.copy(alpha = 0.16f),
                    ) {
                        Box(modifier = Modifier.size(size = 44.dp), contentAlignment = Alignment.Center) {
                            if (loading) {
                                val rot by rememberInfiniteTransition(label = "spin").animateFloat(
                                    initialValue = 0f, targetValue = 360f,
                                    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                                    label = "r",
                                )
                                Text(
                                    text = "↻",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier.graphicsLayer { rotationZ = rot },
                                )
                            } else {
                                Text(text = "▶", color = Color.White, fontSize = 17.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                        Text(
                            text = when {
                                loading  -> "正在启动中…"
                                !enabled -> "请等待当前任务完成"
                                else     -> "开始启动"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (loading) "正在执行启动流程，请勿重复点击" else "一键启动辅助程序与游戏",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                }
                Text(text = "›", fontSize = 22.sp, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── ActionList ────────────────────────────────────────────────────────────────
@Composable
private fun ActionList(isDark: Boolean, state: LauncherUiState, enabled: Boolean, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = "快捷操作",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        WideActionCard(icon = "🔄", title = "重启辅助程序", subtitle = "重新载入辅助模块",    accent = CardPurple, loading = state.loadingBtn == ButtonKey.RESTART_AUX, enabled = enabled, isDark = isDark, onClick = vm::onRestartAux)
        WideActionCard(icon = "🚫", title = "完全关闭辅助", subtitle = "停止所有相关进程",    accent = CardRed,    loading = state.loadingBtn == ButtonKey.CLOSE_ALL,   enabled = enabled, isDark = isDark, onClick = vm::onCloseAll)
        WideActionCard(icon = "🛡", title = "清理防盗号",   subtitle = "清理设备并加固账号",  accent = CardGreen,  loading = state.loadingBtn == ButtonKey.CLEAN,       enabled = enabled, isDark = isDark, onClick = vm::onClean)
    }
}

@Composable
private fun WideActionCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    loading: Boolean,
    enabled: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(targetValue = if (loading) 0.985f else 1f, label = title)
    val rot   by if (loading) {
        rememberInfiniteTransition(label = "cs$title").animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "cr$title",
        )
    } else { remember { mutableStateOf(0f) } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(alpha = if (enabled || loading) 1f else 0.68f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled && !loading) onClick() },
            ),
        shape = RoundedCornerShape(size = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) accent.copy(alpha = 0.12f) else accent.copy(alpha = 0.06f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accent.copy(alpha = if (isDark) 0.20f else 0.10f),
                    shape = RoundedCornerShape(size = 20.dp),
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = if (isDark) 0.08f else 0.04f),
                            Color.Transparent,
                        ),
                        start = Offset(0f, 0f), end = Offset(1200f, 280f),
                    )
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(size = 16.dp),
                    color = accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                ) {
                    Box(modifier = Modifier.size(size = 42.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            color = accent,
                            fontSize = 19.sp,
                            modifier = if (loading) Modifier.graphicsLayer { rotationZ = rot } else Modifier,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (!enabled && !loading) "当前有任务执行中" else subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(size = 999.dp),
                color = accent.copy(alpha = if (isDark) 0.15f else 0.09f),
            ) {
                Text(
                    text = if (loading) "执行中" else "进入",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) accent else accent.copy(alpha = 0.88f),
                )
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
            else
                @Suppress("DEPRECATION") ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pi.versionName ?: "unknown"
        }.getOrDefault("unknown")
    }
}

@Composable
private fun FooterRow(isDark: Boolean, busy: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF4F8FF),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                Text(text = "晚风工作室", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Text(text = "版本 v${rememberAppVersionName()} · 客户服务版", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MiniChip(
                text = if (busy) "处理中" else "服务正常",
                accent = if (busy) AccentBlue else AccentGreen,
                isDark = isDark,
            )
        }
    }
}

// ── InfoDialog ────────────────────────────────────────────────────────────────
@Composable
private fun InfoDialog(
    visible: Boolean,
    isDark: Boolean,
    title: String,
    subtitle: String,
    bodyText: String,
    footerText: String,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = "知道了", color = AccentBlue) }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                Surface(
                    shape = RoundedCornerShape(size = 14.dp),
                    color = if (isDark) StatusAmber.copy(alpha = 0.10f) else StatusAmber.copy(alpha = 0.07f),
                ) {
                    Text(
                        text = bodyText.ifBlank { "暂无内容" }.trim(),
                        modifier = Modifier.padding(all = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(text = footerText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(size = 28.dp),
    )
}

// ── Toast ─────────────────────────────────────────────────────────────────────
@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().zIndex(10f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.statusBarsPadding().padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(space = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = toasts,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tl",
            ) { list ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    list.forEach { t -> ToastItem(toast = t, onDone = { onRemove(t.id) }) }
                }
            }
        }
    }
}

@Composable
private fun ToastItem(toast: ToastData, onDone: () -> Unit) {
    LaunchedEffect(toast.id) { delay(3200L); onDone() }
    val dot = toast.color.toComposeColor()
    Row(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .clip(RoundedCornerShape(size = 14.dp))
            .background(color = ToastBg)
            .border(width = 1.dp, color = dot.copy(alpha = 0.25f), shape = RoundedCornerShape(size = 14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 9.dp),
    ) {
        Box(modifier = Modifier.size(size = 6.dp).background(color = dot, shape = CircleShape))
        Text(text = toast.msg, fontSize = 13.sp, color = ToastText, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ── BackgroundBlobs ───────────────────────────────────────────────────────────
@Composable
private fun BackgroundBlobs(isDark: Boolean) {
    val drift by rememberInfiniteTransition(label = "blobs").animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = EaseInOut), RepeatMode.Reverse),
        label = "drift",
    )
    Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
        Box(
            modifier = Modifier
                .size(size = 260.dp)
                .graphicsLayer { translationX = -60f + 18f * drift; translationY = -40f }
                .blur(radiusX = 90.dp, radiusY = 90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(if (isDark) AccentBlue.copy(alpha = 0.11f) else AccentBlue.copy(alpha = 0.05f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(size = 200.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 16f * drift; translationY = -18f + 20f * drift }
                .blur(radiusX = 84.dp, radiusY = 84.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(if (isDark) AccentCyan.copy(alpha = 0.09f) else AccentCyan.copy(alpha = 0.04f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(size = 220.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer { translationX = 38f - 22f * drift; translationY = 56f }
                .blur(radiusX = 88.dp, radiusY = 88.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(if (isDark) AccentIndigo.copy(alpha = 0.10f) else AccentIndigo.copy(alpha = 0.04f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
    }
}

// ── 扩展函数 ──────────────────────────────────────────────────────────────────
private fun RunStatus.label() = when (this) {
    RunStatus.RUNNING -> "运行中"
    RunStatus.LOADING -> "启动中"
    RunStatus.STOPPED -> "已停止"
}
private fun RunStatus.dotColor() = when (this) {
    RunStatus.RUNNING -> StatusGreen
    RunStatus.LOADING -> StatusAmber
    RunStatus.STOPPED -> StatusRed
}
private fun ConnStatus.label() = when (this) {
    ConnStatus.CONNECTED    -> "已连接"
    ConnStatus.CONNECTING   -> "连接中"
    ConnStatus.DISCONNECTED -> "离线"
}
private fun ConnStatus.dotColor() = when (this) {
    ConnStatus.CONNECTED    -> StatusGreen
    ConnStatus.CONNECTING   -> StatusAmber
    ConnStatus.DISCONNECTED -> StatusRed
}
