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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanfeng.launcher.ui.theme.AccentBlue
import com.wanfeng.launcher.ui.theme.AccentGreen
import com.wanfeng.launcher.ui.theme.AccentCyan
import com.wanfeng.launcher.ui.theme.AccentIndigo
import com.wanfeng.launcher.ui.theme.CardGreen
import com.wanfeng.launcher.ui.theme.CardRed
import com.wanfeng.launcher.ui.theme.CardPurple
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
private val Color.withAlpha: (Float) -> Color get() = { copy(alpha = it) }
private val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)

private data class Greeting(val main: String, val sub: String, val tip: String)

private fun getGreeting(): Greeting {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 0..4 -> Greeting("夜深了，辛苦了。", "如需继续操作，建议先确认设备与网络状态。", "公告与教程可在下方随时查看")
        in 5..7 -> Greeting("早上好，欢迎使用。", "建议先查看今日公告，再开始本次操作。", "常用说明已整理到教程页面")
        in 8..11 -> Greeting("上午好，准备就绪。", "当前页面可直接查看状态、启动服务和处理常用操作。", "如遇异常，可优先查看公告说明")
        in 12..13 -> Greeting("中午好，先休息一下。", "操作前确认环境正常，能减少重复处理。", "需要帮助时可查看教程页面")
        in 14..17 -> Greeting("下午好，继续处理吧。", "当前状态会实时同步，便于你快速判断下一步。", "公告和教程已分开显示，查找更方便")
        in 18..20 -> Greeting("晚上好，欢迎回来。", "开始前先看一眼状态信息，会更省时间。", "如需重新处理，可使用下方快捷操作")
        else -> Greeting("今天辛苦了。", "若还需要继续操作，建议完成后及时检查结果。", "常见说明已整理完毕，按需查看即可")
    }
}

@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark
    val isBusy = state.loadingBtn != ButtonKey.NONE
    var showNotice by rememberSaveable { mutableStateOf(false) }
    var showGuide by rememberSaveable { mutableStateOf(false) }
    var autoNoticeShown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.announcement) {
        if (state.announcement.isNotBlank() && !autoNoticeShown) {
            showNotice = true
            autoNoticeShown = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) {
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to DarkBg0,
                            0.45f to DarkBg45,
                            1f to DarkBg100,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 2200f),
                    )
                } else {
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to LightBg0,
                            0.52f to LightBg50,
                            1f to LightBg100,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 2200f),
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
                .padding(horizontal = 18.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedSection(delayMillis = 0) {
                HeaderRow(
                    isDark = isDark,
                    onToggleTheme = vm::toggleTheme,
                )
            }

            AnimatedSection(delayMillis = 60) {
                HeroCard(
                    isDark = isDark,
                    state = state,
                    quote = state.gameQuote,
                    onShowNotice = { showNotice = true },
                    onShowGuide = { showGuide = true },
                )
            }

            AnimatedSection(delayMillis = 120) {
                OverviewCard(
                    isDark = isDark,
                    state = state,
                )
            }

            AnimatedSection(delayMillis = 180) {
                LaunchButton(
                    loading = state.loadingBtn == ButtonKey.LAUNCH,
                    enabled = !isBusy,
                    isDark = isDark,
                    onClick = vm::onLaunch,
                )
            }

            AnimatedSection(delayMillis = 240) {
                ActionList(
                    isDark = isDark,
                    state = state,
                    enabled = !isBusy,
                    vm = vm,
                )
            }

            AnimatedSection(delayMillis = 300) {
                FooterRow(isDark = isDark, busy = isBusy)
            }

            Spacer(Modifier.height(12.dp))
        }

        ToastLayer(toasts = state.toasts, onRemove = vm::removeToast)

        InfoDialog(
            visible = showNotice,
            isDark = isDark,
            title = "系统公告",
            subtitle = "请先阅读公告内容后再继续操作。",
            bodyText = state.announcement,
            footerText = "如需再次查看，可在首页点击“查看公告”。",
            onDismiss = { showNotice = false },
        )

        InfoDialog(
            visible = showGuide,
            isDark = isDark,
            title = "使用教程",
            subtitle = "已将教程与公告分开显示，便于查阅。",
            bodyText = state.tutorial,
            footerText = "如需再次查看，可在首页点击“使用教程”。",
            onDismiss = { showGuide = false },
        )

        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                confirmButton = {
                    TextButton(onClick = vm::dismissDialog) {
                        Text("确定", color = AccentBlue)
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
                shape = RoundedCornerShape(24.dp),
            )
        }
    }
}

@Composable
private fun AnimatedSection(
    delayMillis: Int,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 520, delayMillis = delayMillis)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 520, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 4 },
            ),
    ) {
        content()
    }
}

@Composable
private fun HeaderRow(isDark: Boolean, onToggleTheme: () -> Unit) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            time = Calendar.getInstance()
        }
    }

    val timeStr = remember(time) {
        "%02d:%02d".format(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
    }
    val dateStr = remember(time) {
        val days = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val dow = days[time.get(Calendar.DAY_OF_WEEK) - 1]
        "${time.get(Calendar.MONTH) + 1}月${time.get(Calendar.DAY_OF_MONTH)}日 $dow"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppLogoCompose(size = 44.dp, isDark = isDark)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = if (isDark) 0.dp else 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTheme,
                    ),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFEAF3FF),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else AccentBlue.copy(alpha = 0.14f)
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isDark) "☀" else "◐",
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLogoCompose(size: Dp, isDark: Boolean) {
    val gradient = Brush.linearGradient(listOf(AccentBlue, AccentIndigo, AccentCyan))
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(15.dp))
            .background(gradient)
            .padding(1.2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDark) Color(0xFF09101D) else Color(0xFFF2F7FF))
                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.56f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AccentBlue.copy(alpha = 0.14f),
                                AccentCyan.copy(alpha = 0.08f),
                            )
                        )
                    )
                    .border(1.dp, AccentBlue.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "▲",
                    fontSize = (size.value * 0.34f).sp,
                    color = AccentCyan,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

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
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 7.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF10192D), MaterialTheme.colorScheme.surface)
                        } else {
                            listOf(Color(0xFFF2F7FF), MaterialTheme.colorScheme.surface)
                        }
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MiniChip(
                            text = "游戏 ${state.gameStatus.label()}",
                            accent = state.gameStatus.dotColor(),
                            isDark = isDark,
                        )
                        MiniChip(
                            text = "辅助 ${state.auxStatus.label()}",
                            accent = state.auxStatus.dotColor(),
                            isDark = isDark,
                        )
                        MiniChip(
                            text = "服务器 ${state.serverStatus.label()}",
                            accent = state.serverStatus.dotColor(),
                            isDark = isDark,
                        )
                    }
                    Text(
                        text = greeting.main,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = greeting.sub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }

            if (quote.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = "寄语",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentBlue,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onShowNotice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("查看公告")
                }
                OutlinedButton(
                    onClick = onShowGuide,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = if (isDark) 0.28f else 0.22f)),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F7FF),
                        contentColor = AccentBlue,
                    ),
                ) {
                    Text("使用教程")
                }
            }

            Text(
                text = greeting.tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MiniChip(text: String, accent: Color, isDark: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = if (isDark) 0.16f else 0.12f))
            .border(1.dp, accent.copy(alpha = if (isDark) 0.22f else 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(accent, CircleShape)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = accent,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun OverviewCard(isDark: Boolean, state: LauncherUiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "运行状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            StatusRow(
                icon = "🎮",
                title = "游戏",
                status = state.gameStatus.label(),
                dotColor = state.gameStatus.dotColor(),
                pulse = state.gameStatus != RunStatus.STOPPED,
            )
            StatusRow(
                icon = "⚡",
                title = "辅助",
                status = state.auxStatus.label(),
                dotColor = state.auxStatus.dotColor(),
                pulse = state.auxStatus != RunStatus.STOPPED,
            )
            StatusRow(
                icon = "🖥",
                title = "服务器",
                status = state.serverStatus.label(),
                dotColor = state.serverStatus.dotColor(),
                pulse = state.serverStatus != ConnStatus.DISCONNECTED,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "CPU 负载",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Crossfade(targetState = state.cpuUsage, label = "cpuText") { usage ->
                        Text(
                            text = "$usage%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cpuColor(state.cpuUsage),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = (state.cpuUsage / 100f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = cpuColor(state.cpuUsage),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    icon: String,
    title: String,
    status: String,
    dotColor: Color,
    pulse: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = icon, fontSize = 16.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PulseDot(color = dotColor, pulse = pulse)
            Crossfade(targetState = status, label = title) { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PulseDot(color: Color, pulse: Boolean) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by if (pulse) {
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "pulseAlpha",
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(modifier = Modifier.size(10.dp), contentAlignment = Alignment.Center) {
        if (pulse) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color.copy(alpha = alpha * 0.35f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun LaunchButton(loading: Boolean, enabled: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val borderPulse by rememberInfiniteTransition(label = "launchBtn").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOut), RepeatMode.Reverse),
        label = "launchBorder",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(LaunchStart, LaunchMid, LaunchEnd),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 320f),
                    )
                )
                .border(1.dp, Color.White.copy(alpha = borderPulse * 0.24f), RoundedCornerShape(28.dp))
                .alpha(if (enabled || loading) 1f else 0.72f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { if (enabled && !loading) onClick() },
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(780f, 380f),
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
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (loading) {
                                val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                                    label = "rotate",
                                )
                                Text(
                                    text = "↻",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                                )
                            } else {
                                Text(text = "▶", color = Color.White, fontSize = 18.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = when {
                                loading -> "正在启动中…"
                                !enabled -> "请等待当前任务完成"
                                else -> "开始启动"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Text(
                            text = if (loading) "正在执行启动流程，请勿重复点击" else "一键完成当前启动流程",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
                Text(
                    text = "›",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.76f),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ActionList(isDark: Boolean, state: LauncherUiState, enabled: Boolean, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "快捷操作",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        WideActionCard(
            icon = "🔄",
            title = "重启辅助程序",
            subtitle = "重新载入辅助模块",
            accent = CardPurple,
            loading = state.loadingBtn == ButtonKey.RESTART_AUX,
            enabled = enabled,
            isDark = isDark,
            onClick = vm::onRestartAux,
        )
        WideActionCard(
            icon = "🚫",
            title = "完全关闭辅助",
            subtitle = "停止所有相关进程",
            accent = CardRed,
            loading = state.loadingBtn == ButtonKey.CLOSE_ALL,
            enabled = enabled,
            isDark = isDark,
            onClick = vm::onCloseAll,
        )
        WideActionCard(
            icon = "🛡",
            title = "清理防盗号",
            subtitle = "清理设备并加固账号",
            accent = CardGreen,
            loading = state.loadingBtn == ButtonKey.CLEAN,
            enabled = enabled,
            isDark = isDark,
            onClick = vm::onClean,
        )
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
    val rotation by if (loading) {
        rememberInfiniteTransition(label = "cardSpin$title").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "rotate$title",
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(if (enabled || loading) 1f else 0.68f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled && !loading) onClick() },
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) accent.copy(alpha = 0.14f) else accent.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = if (isDark) 0.22f else 0.16f), RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(accent.copy(alpha = if (isDark) 0.09f else 0.06f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 320f),
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    color = accent.copy(alpha = if (isDark) 0.18f else 0.14f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = icon,
                            color = accent,
                            fontSize = 20.sp,
                            modifier = if (loading) Modifier.graphicsLayer { rotationZ = rotation } else Modifier,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
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
                color = accent.copy(alpha = if (isDark) 0.16f else 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = if (loading) "执行中" else "进入",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                )
            }
        }
    }
}


@Composable
private fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        }.getOrDefault("unknown")
    }
}

@Composable
private fun FooterRow(isDark: Boolean, busy: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF1F7FF),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "晚风工作室",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "版本 v${rememberAppVersionName()} · 客户服务版",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            MiniChip(
                text = if (busy) "处理中" else "服务正常",
                accent = if (busy) AccentBlue else AccentGreen,
                isDark = isDark,
            )
        }
    }
}

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
            TextButton(onClick = onDismiss) {
                Text("知道了", color = AccentBlue)
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = if (isDark) StatusAmber.copy(alpha = 0.12f) else StatusAmber.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = bodyText.ifBlank { "暂无内容" }.trim(),
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = footerText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = toasts,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "toastList",
            ) { list ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    list.forEach { toast ->
                        ToastItem(toast = toast, onDone = { onRemove(toast.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastItem(toast: ToastData, onDone: () -> Unit) {
    LaunchedEffect(toast.id) {
        delay(3200)
        onDone()
    }
    val dotColor = toast.color.toComposeColor()
    Row(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ToastBg)
            .border(1.dp, dotColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        Text(
            text = toast.msg,
            fontSize = 13.sp,
            color = ToastText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BackgroundBlobs(isDark: Boolean) {
    val transition = rememberInfiniteTransition(label = "blobs")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = EaseInOut), RepeatMode.Reverse),
        label = "blobDrift",
    )

    Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    translationX = -60f + 20f * drift
                    translationY = -40f
                }
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentBlue.copy(alpha = 0.12f) else AccentBlue.copy(alpha = 0.06f),
                            Color.Transparent,
                        )
                    ),
                    CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationX = 18f * drift
                    translationY = -20f + 22f * drift
                }
                .blur(84.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentCyan.copy(alpha = 0.10f) else AccentCyan.copy(alpha = 0.045f),
                            Color.Transparent,
                        )
                    ),
                    CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 40f - 24f * drift
                    translationY = 60f
                }
                .blur(88.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentIndigo.copy(alpha = 0.11f) else AccentIndigo.copy(alpha = 0.05f),
                            Color.Transparent,
                        )
                    ),
                    CircleShape,
                )
        )
    }
}

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
    ConnStatus.CONNECTED -> "已连接"
    ConnStatus.CONNECTING -> "连接中"
    ConnStatus.DISCONNECTED -> "未连接"
}

private fun ConnStatus.dotColor() = when (this) {
    ConnStatus.CONNECTED -> StatusGreen
    ConnStatus.CONNECTING -> StatusAmber
    ConnStatus.DISCONNECTED -> StatusRed
}

private fun cpuColor(value: Int) = when {
    value > 80 -> StatusRed
    value > 60 -> Color(0xFFFB923C)
    value > 35 -> StatusAmber
    else -> StatusGreen
}
