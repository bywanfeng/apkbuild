package com.wanfeng.launcher.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanfeng.launcher.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

// ── 颜色辅助 ──────────────────────────────────────────────────────────────────
private fun Long.toComposeColor() = Color(this)
private val Color.withAlpha: (Float) -> Color get() = { copy(alpha = it) }

// ── 问候语（对应 App.tsx getGreeting） ────────────────────────────────────────
private data class Greeting(val main: String, val sub: String, val tip: String)

private fun getStreak(): Int {
    // SharedPreferences 实现连续天数（云手机环境中 localStorage 等价物）
    return 1 // 简化：实际项目可接入 SP
}

private fun getGreeting(streak: Int): Greeting {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val badge = when {
        streak >= 30 -> "精英干员"
        streak >= 7  -> "老干员"
        else         -> "干员"
    }
    return when (h) {
        in 0..4  -> Greeting("$badge，夜已深了。", "战场可以等到天明，但你的状态不能。注意休息。", "已连续出勤 $streak 天 · 感谢你的坚守")
        in 5..7  -> Greeting("$badge，黎明将至。", "黎明前的部署最为关键，愿今日首战告捷。", "已连续出勤 $streak 天 · 早起的干员运气不差")
        in 8..11 -> Greeting("$badge，上午好。", "清晨的判断力最为清醒，今日的战绩就靠你了。", "已连续出勤 $streak 天 · 感谢一直以来的信任")
        in 12..13 -> Greeting("$badge，该补给了。", "精锐也需要足够的后勤保障。先去吃点东西吧。", "已连续出勤 $streak 天 · 满血状态才能满血发挥")
        in 14..17 -> Greeting("$badge，下午好。", "午后最易松懈，但最强的干员往往这时反而更专注。", "已连续出勤 $streak 天 · 今天的你无往不胜")
        in 18..20 -> Greeting("$badge，黄金作战时段。", "今晚的战场等待着你，出发吧，祝旗开得胜。", "已连续出勤 $streak 天 · 晚风永远在你身后")
        else     -> Greeting("$badge，今天辛苦了。", "每一次出击都是淬炼，你比昨天更强了。注意休息。", "已连续出勤 $streak 天 · 晚安，好好休息")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 主屏幕入口
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark)
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to DarkBg0,
                            0.45f to DarkBg45,
                            1.00f to DarkBg100,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 2400f),
                    )
                else
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to LightBg0,
                            0.50f to LightBg50,
                            1.00f to LightBg100,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 2400f),
                    )
            )
    ) {
        // 背景光晕
        BackgroundBlobs(isDark = isDark)

        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            HeaderRow(
                isDark = isDark,
                onToggleTheme = { vm.toggleTheme() },
            )

            GreetingCard(
                isDark = isDark,
                quote = state.gameQuote,
                streak = getStreak(),
            )

            // 公告（有内容才显示）
            if (state.announcement.isNotBlank()) {
                AnnouncementCard(isDark = isDark, text = state.announcement)
            }

            StatusStrip(isDark = isDark, state = state)

            LaunchButton(
                loading = state.loadingBtn == ButtonKey.LAUNCH,
                isDark = isDark,
                onClick = { vm.onLaunch() },
            )

            ActionGrid(isDark = isDark, state = state, vm = vm)

            FooterRow(isDark = isDark)

            Spacer(Modifier.height(16.dp))
        }

        // Toast 层
        ToastLayer(toasts = state.toasts, onRemove = { vm.removeToast(it) })

        // 错误/提示 Dialog
        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = { vm.dismissDialog() },
                confirmButton = {
                    TextButton(onClick = { vm.dismissDialog() }) {
                        Text("确定", color = AccentBlue)
                    }
                },
                title = { Text("提示", fontWeight = FontWeight.Medium) },
                text = { Text(state.dialogMessage) },
                containerColor = if (isDark) Color(0xFF0E1525) else Color.White,
                titleContentColor = if (isDark) Color(0xF2E2E8F0) else Color(0xFF1E2A3A),
                textContentColor = if (isDark) Color(0xA094A3B8) else Color(0xFF475569),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
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
        "%02d:%02d".format(
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE),
        )
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
        // Logo + 标题
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppLogoCompose(size = 38.dp)
            Column {
                Text(
                    "晚风工作室",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = if (isDark) Color(0x8C94A3B8) else Color(0xA66478AB),
                )
                Text(
                    "三角洲启动程序",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    style = LocalTextStyle.current.copy(
                        brush = Brush.horizontalGradient(
                            listOf(AccentBlue, AccentIndigo, AccentGreen)
                        )
                    ),
                )
            }
        }

        // 时间 + 主题切换按钮
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    timeStr,
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xCCE2E8F0) else Color(0xFF374151),
                )
                Text(
                    dateStr,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0x7394A3B8) else Color(0x9964788A),
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDark) Color(0x0FFFFFFF) else Color(0x0D000000)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0x17FFFFFF) else Color(0x14000000),
                        RoundedCornerShape(12.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTheme,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (isDark) "☀" else "🌙", fontSize = 14.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// App Logo（还原 App.tsx SVG 三角形）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AppLogoCompose(size: androidx.compose.ui.unit.Dp) {
    val gradient = Brush.linearGradient(listOf(AccentBlue, AccentIndigo, AccentGreen))
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, gradient, RoundedCornerShape(10.dp))
            .background(AccentIndigo.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        // 三角形符号（近似 SVG polygon）
        Text("▲", fontSize = (size.value * 0.42f).sp, style = LocalTextStyle.current.copy(brush = gradient))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Greeting Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GreetingCard(isDark: Boolean, quote: String, streak: Int) {
    val greeting = remember(streak) { getGreeting(streak) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    if (isDark)
                        listOf(Color(0x1A0EA5E9), Color(0x1A6366F1))
                    else
                        listOf(Color(0x120EA5E9), Color(0x126366F1))
                )
            )
            .border(
                1.dp,
                if (isDark) Color(0x336366F1) else Color(0x1F6366F1),
                RoundedCornerShape(16.dp),
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                greeting.main,
                fontSize = 15.sp,
                color = if (isDark) Color(0xF2E2E8F0) else Color(0xFF1E2A3A),
            )
            Text(
                greeting.sub,
                fontSize = 12.sp,
                color = if (isDark) Color(0xA694A3B8) else Color(0xB2475569),
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🔥", fontSize = 11.sp)
                Text(
                    greeting.tip,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0x8094A3B8) else Color(0x9964788A),
                )
            }
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = if (isDark) Color(0x12FFFFFF) else Color(0x0F000000),
            )
            if (quote.isNotBlank()) {
                Text(
                    "「$quote」",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = if (isDark) Color(0x6194A3B8) else Color(0x7264788A),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 公告卡片
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnnouncementCard(isDark: Boolean, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) Color(0x14FBBF24) else Color(0x0DFBBF24)
            )
            .border(
                1.dp,
                if (isDark) Color(0x33FBBF24) else Color(0x1FFBBF24),
                RoundedCornerShape(14.dp),
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📢", fontSize = 13.sp)
                Text(
                    "公告",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatusAmber,
                )
            }
            Text(
                text,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isDark) Color(0xCCE2E8F0) else Color(0xFF374151),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 状态条
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatusStrip(isDark: Boolean, state: LauncherUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("系统状态", isDark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(
                modifier = Modifier.weight(1f),
                icon = "🎮",
                label = "游戏",
                statusText = state.gameStatus.label(),
                dotColor = state.gameStatus.dotColor(),
                pulse = state.gameStatus == RunStatus.LOADING || state.gameStatus == RunStatus.RUNNING,
                isDark = isDark,
            )
            StatusPill(
                modifier = Modifier.weight(1f),
                icon = "⚡",
                label = "辅助",
                statusText = state.auxStatus.label(),
                dotColor = state.auxStatus.dotColor(),
                pulse = state.auxStatus == RunStatus.LOADING || state.auxStatus == RunStatus.RUNNING,
                isDark = isDark,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(
                modifier = Modifier.weight(1f),
                icon = "🖥",
                label = "服务器",
                statusText = state.serverStatus.label(),
                dotColor = state.serverStatus.dotColor(),
                pulse = state.serverStatus != ConnStatus.DISCONNECTED,
                isDark = isDark,
            )
            CpuPill(modifier = Modifier.weight(1f), value = state.cpuUsage, isDark = isDark)
        }
    }
}

@Composable
private fun StatusPill(
    modifier: Modifier,
    icon: String,
    label: String,
    statusText: String,
    dotColor: Color,
    pulse: Boolean,
    isDark: Boolean,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0x0AFFFFFF) else Color(0xA6FFFFFF))
            .border(1.dp, if (isDark) Color(0x12FFFFFF) else Color(0x0F000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 13.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = if (isDark) Color(0x7394A3B8) else Color(0x8C64788A))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                PulseDot(color = dotColor, pulse = pulse)
                Text(statusText, fontSize = 12.sp, color = if (isDark) Color(0xCCE2E8F0) else Color(0xFF1E2A3A))
            }
        }
    }
}

@Composable
private fun CpuPill(modifier: Modifier, value: Int, isDark: Boolean) {
    val barColor = when {
        value > 80 -> StatusRed
        value > 60 -> Color(0xFFFB923C)
        value > 35 -> StatusAmber
        else       -> StatusGreen
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0x0AFFFFFF) else Color(0xA6FFFFFF))
            .border(1.dp, if (isDark) Color(0x12FFFFFF) else Color(0x0F000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("💻", fontSize = 13.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("CPU 占用", fontSize = 10.sp, color = if (isDark) Color(0x7394A3B8) else Color(0x8C64788A))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val animWidth by animateFloatAsState(
                    targetValue = value / 100f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    label = "cpuBar",
                )
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x14FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animWidth)
                            .background(barColor, RoundedCornerShape(3.dp))
                    )
                }
                Text("$value%", fontSize = 11.sp, color = barColor)
            }
        }
    }
}

@Composable
private fun PulseDot(color: Color, pulse: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by if (pulse) infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha",
    ) else remember { mutableStateOf(1f) }

    Box(modifier = Modifier.size(8.dp), contentAlignment = Alignment.Center) {
        if (pulse) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color.copy(alpha = alpha * 0.5f), CircleShape)
            )
        }
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 主启动按钮
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LaunchButton(loading: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "launchBtn")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "borderPulse",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(LaunchStart, LaunchMid, LaunchEnd),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 300f),
                )
            )
            .border(
                1.dp,
                Color(0xFFFFFFFF).copy(alpha = borderAlpha * 0.2f),
                RoundedCornerShape(18.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (!loading) onClick() },
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Sheen overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 300f),
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (loading) {
                        val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
                            initialValue = 0f, targetValue = 360f,
                            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                            label = "rotate",
                        )
                        Text("↻", fontSize = 18.sp, color = Color.White,
                            modifier = Modifier.graphicsLayer { rotationZ = rotation })
                    } else {
                        Text("▶", fontSize = 16.sp, color = Color.White)
                    }
                }
                Column {
                    Text(
                        if (loading) "正在启动中…" else "启动辅助与游戏",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "一键拉起辅助程序 + 三角洲行动",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }
            Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 快捷操作网格
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActionGrid(isDark: Boolean, state: LauncherUiState, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("快捷操作", isDark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = "↺",
                label = "重启辅助程序",
                sub = "重新载入辅助模块",
                accentColor = CardPurple,
                bgBrush = Brush.linearGradient(listOf(Color(0x1F818CF8), Color(0x14A78BFA))),
                borderColor = if (isDark) Color(0x2E818CF8) else Color(0x1A818CF8),
                loading = state.loadingBtn == ButtonKey.RESTART_AUX,
                isDark = isDark,
                onClick = { vm.onRestartAux() },
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = "⏻",
                label = "完全关闭辅助",
                sub = "停止所有辅助进程",
                accentColor = CardRed,
                bgBrush = Brush.linearGradient(listOf(Color(0x1FF87171), Color(0x14F43F5E))),
                borderColor = if (isDark) Color(0x2EF87171) else Color(0x1AF87171),
                loading = state.loadingBtn == ButtonKey.CLOSE_ALL,
                isDark = isDark,
                onClick = { vm.onCloseAll() },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = "🛡",
                label = "清理防盗号",
                sub = "清理设备 · 加固账号",
                accentColor = CardGreen,
                bgBrush = Brush.linearGradient(listOf(Color(0x1F34D399), Color(0x1410B981))),
                borderColor = if (isDark) Color(0x2E34D399) else Color(0x1A34D399),
                loading = state.loadingBtn == ButtonKey.CLEAN,
                isDark = isDark,
                onClick = { vm.onClean() },
            )
            // 预留第4个卡片位
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    icon: String,
    label: String,
    sub: String,
    accentColor: Color,
    bgBrush: Brush,
    borderColor: Color,
    loading: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val rotation by if (loading) rememberInfiniteTransition(label = "card_spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "cardRotate",
    ) else remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    icon,
                    fontSize = 15.sp,
                    color = accentColor,
                    modifier = if (loading) Modifier.graphicsLayer { rotationZ = rotation } else Modifier,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    label,
                    fontSize = 13.sp,
                    color = if (isDark) Color(0xF2E2E8F0) else Color(0xFF1E2A3A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    sub,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0x8094A3B8) else Color(0x8071859A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Footer
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FooterRow(isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "晚风工作室 · v2.4.1",
            fontSize = 11.sp,
            color = if (isDark) Color(0x4094A3B8) else Color(0x4C64788A),
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("⚡", fontSize = 9.sp)
            Text(
                "云手机服务正常",
                fontSize = 11.sp,
                color = if (isDark) Color(0x4094A3B8) else Color(0x4C64788A),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toast
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().zIndex(10f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.padding(top = 20.dp),
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
                    list.forEach { t ->
                        ToastItem(toast = t, onDone = { onRemove(t.id) })
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

// ─────────────────────────────────────────────────────────────────────────────
// 背景光晕
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BackgroundBlobs(isDark: Boolean) {
    // 使用 Modifier.blur() 模拟 radial-gradient blobs
    if (!isDark) return // 亮色模式使用静态渐变背景即可

    val infiniteTransition = rememberInfiniteTransition(label = "blobs")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(8000, easing = EaseInOut), RepeatMode.Reverse),
        label = "blob1Scale",
    )
    Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-100).dp, y = (-140).dp)
                .graphicsLayer { scaleX = scale1; scaleY = scale1 }
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x0E38BDF8), Color.Transparent)),
                    CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x0C818CF8), Color.Transparent)),
                    CircleShape,
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 辅助扩展
// ─────────────────────────────────────────────────────────────────────────────
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
    ConnStatus.DISCONNECTED -> "未连接"
}

private fun ConnStatus.dotColor() = when (this) {
    ConnStatus.CONNECTED    -> StatusGreen
    ConnStatus.CONNECTING   -> StatusAmber
    ConnStatus.DISCONNECTED -> StatusRed
}

@Composable
private fun SectionLabel(text: String, isDark: Boolean) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        letterSpacing = 2.sp,
        color = if (isDark) Color(0x5994A3B8) else Color(0x6664788A),
        modifier = Modifier.padding(horizontal = 2.dp),
    )
}

private val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
