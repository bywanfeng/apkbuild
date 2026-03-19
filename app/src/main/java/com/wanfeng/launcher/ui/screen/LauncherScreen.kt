package com.wanfeng.launcher.ui.screen

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
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

private fun getStreak(): Int = 1

private fun getGreeting(streak: Int): Greeting {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val badge = when {
        streak >= 30 -> "精英干员"
        streak >= 7 -> "老干员"
        else -> "干员"
    }
    return when (h) {
        in 0..4 -> Greeting("$badge，夜已深了。", "战场可以等到天明，但你的状态不能。注意休息。", "已连续出勤 $streak 天 · 感谢你的坚守")
        in 5..7 -> Greeting("$badge，黎明将至。", "黎明前的部署最为关键，愿今日首战告捷。", "已连续出勤 $streak 天 · 早起的干员运气不差")
        in 8..11 -> Greeting("$badge，上午好。", "清晨的判断力最为清醒，今日的战绩就靠你了。", "已连续出勤 $streak 天 · 感谢一直以来的信任")
        in 12..13 -> Greeting("$badge，该补给了。", "精锐也需要足够的后勤保障。先去吃点东西吧。", "已连续出勤 $streak 天 · 满血状态才能满血发挥")
        in 14..17 -> Greeting("$badge，下午好。", "午后最易松懈，但最强的干员往往这时反而更专注。", "已连续出勤 $streak 天 · 今天的你无往不胜")
        in 18..20 -> Greeting("$badge，黄金作战时段。", "今晚的战场等待着你，出发吧，祝旗开得胜。", "已连续出勤 $streak 天 · 晚风永远在你身后")
        else -> Greeting("$badge，今天辛苦了。", "每一次出击都是淬炼，你比昨天更强了。注意休息。", "已连续出勤 $streak 天 · 晚安，好好休息")
    }
}

@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark
    var showNotice by rememberSaveable { mutableStateOf(false) }
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
                    quote = state.gameQuote,
                    streak = getStreak(),
                    onShowNotice = { showNotice = true },
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
                    isDark = isDark,
                    onClick = vm::onLaunch,
                )
            }

            AnimatedSection(delayMillis = 240) {
                ActionList(
                    isDark = isDark,
                    state = state,
                    vm = vm,
                )
            }

            AnimatedSection(delayMillis = 300) {
                FooterRow(isDark = isDark)
            }

            Spacer(Modifier.height(12.dp))
        }

        ToastLayer(toasts = state.toasts, onRemove = vm::removeToast)

        NoticeDialog(
            visible = showNotice,
            isDark = isDark,
            noticeText = state.announcement,
            onDismiss = { showNotice = false },
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
            AppLogoCompose(size = 42.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "晚风工作室",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "三角洲启动程序",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTheme,
                    ),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isDark) "☀" else "🌙",
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLogoCompose(size: Dp) {
    val gradient = Brush.linearGradient(listOf(AccentBlue, AccentIndigo, AccentGreen))
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(gradient)
            .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF0B1020)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "▲",
                fontSize = (size.value * 0.42f).sp,
                color = AccentBlue,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun HeroCard(
    isDark: Boolean,
    quote: String,
    streak: Int,
    onShowNotice: () -> Unit,
) {
    val greeting = remember(streak) { getGreeting(streak) }
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFF131C31),
                                MaterialTheme.colorScheme.surface,
                            )
                        } else {
                            listOf(
                                Color(0xFFF6F8FD),
                                MaterialTheme.colorScheme.surface,
                            )
                        }
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        MiniChip(
                            text = "稳定模式",
                            accent = AccentGreen,
                            isDark = isDark,
                        )
                        MiniChip(
                            text = "出勤 $streak 天",
                            accent = AccentBlue,
                            isDark = isDark,
                        )
                    }
                    Text(
                        text = greeting.main,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = greeting.sub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                    )
                }
            }

            if (quote.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        text = "「$quote」",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
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
                ) {
                    Text("查看公告")
                }
                OutlinedButton(
                    onClick = onShowNotice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("使用说明")
                }
            }

            Text(
                text = greeting.tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = "系统状态",
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
                        text = "CPU 占用",
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
                    text = "状态监控",
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
private fun LaunchButton(loading: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val borderPulse by rememberInfiniteTransition(label = "launchBtn").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOut), RepeatMode.Reverse),
        label = "launchBorder",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(LaunchStart, LaunchMid, LaunchEnd),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 320f),
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = borderPulse * 0.22f),
                    RoundedCornerShape(26.dp),
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { if (!loading) onClick() },
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(700f, 360f),
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
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
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
                            text = if (loading) "正在启动中…" else "启动辅助与游戏",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "一键完成启动流程",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                }
                Text(
                    text = "›",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.74f),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ActionList(isDark: Boolean, state: LauncherUiState, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "快捷操作",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        WideActionCard(
            icon = "↺",
            title = "重启辅助程序",
            subtitle = "重新载入辅助模块",
            accent = CardPurple,
            loading = state.loadingBtn == ButtonKey.RESTART_AUX,
            isDark = isDark,
            onClick = vm::onRestartAux,
        )
        WideActionCard(
            icon = "⏻",
            title = "完全关闭辅助",
            subtitle = "停止所有相关进程",
            accent = CardRed,
            loading = state.loadingBtn == ButtonKey.CLOSE_ALL,
            isDark = isDark,
            onClick = vm::onCloseAll,
        )
        WideActionCard(
            icon = "🛡",
            title = "清理防盗号",
            subtitle = "清理设备并加固账号",
            accent = CardGreen,
            loading = state.loadingBtn == ButtonKey.CLEAN,
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                accent.copy(alpha = 0.14f)
            } else {
                accent.copy(alpha = 0.08f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = if (isDark) 0.22f else 0.16f), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    color = accent.copy(alpha = if (isDark) 0.18f else 0.14f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = if (loading) "执行中" else "进入",
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FooterRow(isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "晚风工作室 · v2.4.1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "界面已优化为护眼浅色与弹窗公告样式",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MiniChip(
                text = "云手机服务正常",
                accent = AccentGreen,
                isDark = isDark,
            )
        }
    }
}

@Composable
private fun NoticeDialog(
    visible: Boolean,
    isDark: Boolean,
    noticeText: String,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val mergedText = buildString {
        append(noticeText.ifBlank { "暂无公告" }.trim())
        append("\n\n")
        append("本产品仅供学习技术交流使用，严禁用于其他破坏游戏公平性行为。")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了", color = AccentBlue)
            }
        },
        title = {
            Text(
                text = "公告",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = if (isDark) StatusAmber.copy(alpha = 0.12f) else StatusAmber.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = mergedText,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = "如需再次查看，可在首页点击“查看公告”。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(26.dp),
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
            .clip(RoundedCornerShape(16.dp))
            .background(ToastBg)
            .border(1.dp, dotColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        Text(toast.msg, fontSize = 13.sp, color = ToastText)
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
