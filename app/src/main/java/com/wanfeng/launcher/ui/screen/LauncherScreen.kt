package com.wanfeng.launcher.ui.screen

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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

// ─── 工具 ─────────────────────────────────────────────────────────────────────
private fun Long.toComposeColor() = Color(this)

// Liquid Glass 玻璃色系
private val GlassWhiteHigh  = Color.White.copy(alpha = 0.18f)  // 高光层
private val GlassWhiteMid   = Color.White.copy(alpha = 0.10f)  // 玻璃主体
private val GlassWhiteLow   = Color.White.copy(alpha = 0.06f)  // 底层
private val GlassEdge       = Color.White.copy(alpha = 0.30f)  // 边缘高光
private val GlassEdgeDark   = Color.White.copy(alpha = 0.08f)  // 暗边
private val GlassDark       = Color(0xFF1C2333).copy(alpha = 0.55f) // 暗色玻璃主体
private val GlassDarkHigh   = Color.White.copy(alpha = 0.12f)

// Spring 动画规格
private val SpringDefault   = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
private val SpringSnappy    = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy,    stiffness = Spring.StiffnessMedium)

private data class Greeting(val main: String, val sub: String)
private fun getGreeting(): Greeting {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 0..4   -> Greeting("夜深了，注意休息。",     "该休息了，明天继续。")
        in 5..7   -> Greeting("早上好，今天也加油！",   "新的一天，状态不错。")
        in 8..11  -> Greeting("上午好，状态不错。",     "一切就绪，随时可以开始。")
        in 12..13 -> Greeting("中午了，先去吃饭吧。",   "吃好喝好，下午更有力气。")
        in 14..17 -> Greeting("下午好，继续加油。",     "稳住，收尾阶段往往是关键。")
        in 18..20 -> Greeting("晚上好，欢迎回来。",     "今晚也拜托你了。")
        else      -> Greeting("辛苦了，今天也谢谢你。", "收工了，好好休息。")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Liquid Glass 核心 Modifier
// 模拟 iOS 26 Liquid Glass：磨砂玻璃 + 顶部高光条 + 边缘光
// ─────────────────────────────────────────────────────────────────────────────
private fun Modifier.liquidGlass(
    isDark: Boolean,
    cornerRadius: Dp = 24.dp,
    tintColor: Color = Color.Transparent,
): Modifier = this
    // 1. 玻璃主体填充
    .background(
        brush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    GlassDarkHigh,
                    GlassDark,
                    Color(0xFF161E2E).copy(alpha = 0.60f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    GlassWhiteHigh,
                    GlassWhiteMid,
                    GlassWhiteLow,
                )
            )
        },
        shape = RoundedCornerShape(size = cornerRadius),
    )
    // 2. 彩色调色板叠加（品牌色 tint）
    .then(
        if (tintColor != Color.Transparent)
            Modifier.background(
                color = tintColor.copy(alpha = if (isDark) 0.12f else 0.07f),
                shape = RoundedCornerShape(size = cornerRadius),
            )
        else Modifier
    )
    // 3. 顶部高光条 + 边缘光
    .drawWithContent {
        drawContent()
        val cr = cornerRadius.toPx()
        // 顶部高光条（液态玻璃标志性光泽）
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.55f),
                    Color.Transparent,
                ),
                startY = 0f,
                endY   = size.height * 0.38f,
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
            size = size,
        )
        // 外边缘光圈
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    if (isDark) GlassEdge else Color.White.copy(alpha = 0.80f),
                    if (isDark) GlassEdgeDark else Color.White.copy(alpha = 0.20f),
                    if (isDark) GlassEdge else Color.White.copy(alpha = 0.60f),
                ),
                start = Offset(0f, 0f),
                end   = Offset(size.width, size.height),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
            style = Stroke(width = 1.2.dp.toPx()),
            size = size,
        )
    }

// 悬浮玻璃胶囊 Chip
private fun Modifier.glassChip(isDark: Boolean, accentColor: Color): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = if (isDark) listOf(accentColor.copy(alpha = 0.22f), accentColor.copy(alpha = 0.12f))
                     else        listOf(accentColor.copy(alpha = 0.14f), accentColor.copy(alpha = 0.07f)),
        ),
        shape = RoundedCornerShape(size = 999.dp),
    )
    .drawWithContent {
        drawContent()
        // 顶部高光
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = if (isDark) 0.18f else 0.40f), Color.Transparent),
                startY = 0f, endY = size.height * 0.5f,
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f),
            size = size,
        )
        // 边框
        drawRoundRect(
            color = accentColor.copy(alpha = if (isDark) 0.35f else 0.22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f),
            style = Stroke(width = 1.dp.toPx()),
            size = size,
        )
    }

// ─────────────────────────────────────────────────────────────────────────────
// 主屏幕
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
        if (state.announcement.isNotBlank() && !autoShown) { showNotice = true; autoShown = true }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = if (isDark)
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1526), Color(0xFF060B14), Color(0xFF04070F)),
                    center = Offset.Unspecified, radius = 1800f,
                )
            else
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE8F3FF), Color(0xFFD8ECFF), Color(0xFFC8E0F8)),
                    center = Offset.Unspecified, radius = 1800f,
                )
        )
    ) {
        // 背景 Liquid 光晕层
        LiquidAmbientLight(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            LGAnimSec(delayMs = 0)   { HeaderRow(isDark = isDark, onToggleTheme = vm::toggleTheme) }
            LGAnimSec(delayMs = 80)  { HeroCard(isDark = isDark, state = state, quote = state.gameQuote, onShowNotice = { showNotice = true }, onShowGuide = { showGuide = true }) }
            LGAnimSec(delayMs = 160) { LaunchButton(loading = state.loadingBtn == ButtonKey.LAUNCH, enabled = !isBusy, isDark = isDark, onClick = vm::onLaunch) }
            LGAnimSec(delayMs = 220) { ActionList(isDark = isDark, state = state, enabled = !isBusy, vm = vm) }
            LGAnimSec(delayMs = 280) { FooterRow(isDark = isDark, busy = isBusy) }
            Spacer(modifier = Modifier.height(8.dp))
        }

        ToastLayer(toasts = state.toasts, onRemove = vm::removeToast)

        InfoDialog(visible = showNotice, isDark = isDark, title = "系统公告", subtitle = "请阅读后再继续操作。",
            bodyText = state.announcement, footerText = "可随时点击首页按钮再次查看。", onDismiss = { showNotice = false })
        InfoDialog(visible = showGuide, isDark = isDark, title = "使用教程", subtitle = "操作说明，按需查阅。",
            bodyText = state.tutorial, footerText = "可随时点击首页按钮再次查看。", onDismiss = { showGuide = false })

        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                confirmButton = { TextButton(onClick = vm::dismissDialog) { Text(text = "确定", color = AccentBlue) } },
                title = { Text(text = "提示", fontWeight = FontWeight.SemiBold) },
                text  = { Text(text = state.dialogMessage, lineHeight = 21.sp) },
                containerColor = if (isDark) Color(0xFF1C2640).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(size = 28.dp),
            )
        }
    }
}

// Liquid Glass 弹簧入场动画
@Composable
private fun LGAnimSec(delayMs: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = delayMs, easing = FastOutSlowInEasing)) +
            scaleIn(
                animationSpec = tween(durationMillis = 600, delayMillis = delayMs, easing = FastOutSlowInEasing),
                initialScale = 0.94f,
            ) +
            slideInVertically(
                animationSpec = tween(durationMillis = 600, delayMillis = delayMs, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 6 },
            ),
    ) { content() }
}

// ─────────────────────────────────────────────────────────────────────────────
// 背景环境光（Liquid Glass 特征：柔和彩色光球）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LiquidAmbientLight(isDark: Boolean) {
    val inf = rememberInfiniteTransition(label = "ambient")
    val drift by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d",
    )
    val drift2 by inf.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d2",
    )

    Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
        // 主光球 - 蓝紫
        Box(
            modifier = Modifier
                .size(size = 420.dp)
                .offset(x = (-80).dp + 30.dp * drift, y = (-100).dp + 20.dp * drift2)
                .blur(radiusX = 120.dp, radiusY = 120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentBlue.copy(alpha = 0.20f)   else AccentBlue.copy(alpha = 0.12f),
                            if (isDark) AccentIndigo.copy(alpha = 0.12f) else AccentIndigo.copy(alpha = 0.07f),
                            Color.Transparent,
                        )
                    ),
                    shape = CircleShape,
                )
        )
        // 右上青色光
        Box(
            modifier = Modifier
                .size(size = 300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp - 20.dp * drift2, y = (-40).dp + 30.dp * drift)
                .blur(radiusX = 100.dp, radiusY = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentCyan.copy(alpha = 0.16f)  else AccentCyan.copy(alpha = 0.09f),
                            Color.Transparent,
                        )
                    ),
                    shape = CircleShape,
                )
        )
        // 右下绿色光
        Box(
            modifier = Modifier
                .size(size = 280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp - 15.dp * drift, y = 60.dp + 20.dp * drift2)
                .blur(radiusX = 90.dp, radiusY = 90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentGreen.copy(alpha = 0.10f) else AccentGreen.copy(alpha = 0.07f),
                            Color.Transparent,
                        )
                    ),
                    shape = CircleShape,
                )
        )
        // 左下紫光
        Box(
            modifier = Modifier
                .size(size = 240.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp + 20.dp * drift2, y = 50.dp - 15.dp * drift)
                .blur(radiusX = 80.dp, radiusY = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) AccentIndigo.copy(alpha = 0.12f) else AccentIndigo.copy(alpha = 0.07f),
                            Color.Transparent,
                        )
                    ),
                    shape = CircleShape,
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderRow(isDark: Boolean, onToggleTheme: () -> Unit) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { delay(1000L); time = Calendar.getInstance() } }
    val timeStr = remember(time) { "%02d:%02d".format(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE)) }
    val dateStr = remember(time) {
        val d = arrayOf("周日","周一","周二","周三","周四","周五","周六")
        "${time.get(Calendar.MONTH)+1}月${time.get(Calendar.DAY_OF_MONTH)}日 ${d[time.get(Calendar.DAY_OF_WEEK)-1]}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LGLogo(size = 44.dp, isDark = isDark)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "WANFENG STUDIO",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF3C6496).copy(alpha = 0.70f),
                )
                Text(
                    text = "三角洲服务面板",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF12203A),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 时间胶囊 - Liquid Glass
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 18.dp))
                    .liquidGlass(isDark = isDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = timeStr, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF12203A))
                    Text(text = dateStr, fontSize = 10.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF3C6496).copy(alpha = 0.65f))
                }
            }

            // 主题切换按钮
            val scaleBtn by animateFloatAsState(targetValue = 1f, animationSpec = SpringSnappy, label = "themeBtn")
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .scale(scaleBtn)
                    .clip(RoundedCornerShape(size = 14.dp))
                    .liquidGlass(isDark = isDark)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTheme,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = if (isDark) "☀" else "◐", fontSize = 16.sp)
            }
        }
    }
}

// Liquid Glass Logo
@Composable
private fun LGLogo(size: Dp, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(size = size)
            .clip(RoundedCornerShape(size = size * 0.36f))
            .liquidGlass(isDark = isDark, tintColor = AccentBlue),
        contentAlignment = Alignment.Center,
    ) {
        // 内部三角标
        Text(
            text = "▲",
            fontSize = (size.value * 0.38f).sp,
            color = if (isDark) AccentCyan.copy(alpha = 0.90f) else AccentBlue.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    isDark: Boolean,
    state: LauncherUiState,
    quote: String,
    onShowNotice: () -> Unit,
    onShowGuide: () -> Unit,
) {
    val greeting = remember { getGreeting() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 28.dp))
            .liquidGlass(isDark = isDark, cornerRadius = 28.dp, tintColor = AccentBlue)
            .padding(all = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            // 状态胶囊行
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                LGChip(text = "游戏 ${state.gameStatus.label()}", accent = state.gameStatus.dotColor(), isDark = isDark)
                LGChip(text = "辅助 ${state.auxStatus.label()}", accent = state.auxStatus.dotColor(), isDark = isDark)
                if (state.serverStatus != ConnStatus.DISCONNECTED) {
                    LGChip(text = "服务器 ${state.serverStatus.label()}", accent = state.serverStatus.dotColor(), isDark = isDark)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = greeting.main,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF0D1E35),
                )
                Text(
                    text = greeting.sub,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.52f) else Color(0xFF3C5A80).copy(alpha = 0.80f),
                    lineHeight = 20.sp,
                )
            }

            // 寄语 — 嵌套玻璃卡片
            if (quote.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(size = 18.dp))
                        .liquidGlass(isDark = isDark, cornerRadius = 18.dp, tintColor = AccentIndigo)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Text(text = "💬", fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
                        Text(
                            text = quote,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = if (isDark) Color.White.copy(alpha = 0.68f) else Color(0xFF1A3A6A).copy(alpha = 0.80f),
                            lineHeight = 19.sp,
                        )
                    }
                }
            }

            // 按钮行
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // 查看公告 - 实心玻璃按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(size = 16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AccentBlue.copy(alpha = 0.90f), AccentIndigo.copy(alpha = 0.85f)),
                            ),
                            shape = RoundedCornerShape(size = 16.dp),
                        )
                        .drawWithContent {
                            drawContent()
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    startY = 0f, endY = size.height * 0.45f,
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                                size = size,
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.22f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx()),
                                size = size,
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onShowNotice,
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "查看公告", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }

                // 使用教程 - 透明玻璃按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(size = 16.dp))
                        .liquidGlass(isDark = isDark, cornerRadius = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onShowGuide,
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "使用教程",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.82f) else AccentBlue.copy(alpha = 0.90f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LGChip(text: String, accent: Color, isDark: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 999.dp))
            .glassChip(isDark = isDark, accentColor = accent)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 呼吸点
        LGPulseDot(color = accent)
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) accent.copy(alpha = 0.95f) else accent.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun LGPulseDot(color: Color) {
    val inf = rememberInfiniteTransition(label = "pd")
    val alpha by inf.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pda",
    )
    Box(modifier = Modifier.size(size = 6.dp).background(color = color.copy(alpha = alpha), shape = CircleShape))
}

// ─────────────────────────────────────────────────────────────────────────────
// Launch Button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LaunchButton(loading: Boolean, enabled: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (loading) 0.97f else 1f,
        animationSpec = SpringDefault,
        label = "launchScale",
    )
    val inf = rememberInfiniteTransition(label = "lb")
    val shimmer by inf.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "shimmer",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(size = 26.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(LaunchStart, LaunchMid, LaunchEnd),
                    start = Offset(0f, 0f), end = Offset(1200f, 300f),
                ),
                shape = RoundedCornerShape(size = 26.dp),
            )
            // 流光扫过效果（Liquid Glass 特征）
            .drawWithContent {
                drawContent()
                val sw = size.width * 0.5f
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        startX = shimmer * size.width - sw * 0.5f,
                        endX   = shimmer * size.width + sw,
                    ),
                )
                // 顶部高光
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.40f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()),
                    size = size,
                )
                // 边框
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx()),
                    size = size,
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled && !loading) onClick() },
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // 图标球
                Box(
                    modifier = Modifier
                        .size(size = 46.dp)
                        .clip(CircleShape)
                        .background(color = Color.White.copy(alpha = 0.20f))
                        .drawWithContent {
                            drawContent()
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                    startY = 0f, endY = size.height * 0.5f,
                                )
                            )
                            drawCircle(color = Color.White.copy(alpha = 0.20f), style = Stroke(width = 1.dp.toPx()))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (loading) {
                        val rot by rememberInfiniteTransition(label = "spin").animateFloat(
                            initialValue = 0f, targetValue = 360f,
                            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                            label = "r",
                        )
                        Text(text = "↻", color = Color.White, fontSize = 22.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = rot })
                    } else {
                        Text(text = "▶", color = Color.White, fontSize = 18.sp)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = when { loading -> "正在启动中…"; !enabled -> "请等待当前任务完成"; else -> "开始启动" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    Text(
                        text = if (loading) "正在执行启动流程，请勿重复点击" else "一键启动辅助程序与游戏",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
            Text(text = "›", fontSize = 26.sp, color = Color.White.copy(alpha = 0.70f), fontWeight = FontWeight.Light)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action List
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActionList(isDark: Boolean, state: LauncherUiState, enabled: Boolean, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Text(
            text = "快捷操作",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF3C5A80).copy(alpha = 0.65f),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        LGActionCard(icon = "🔄", title = "重启辅助程序", subtitle = "重新载入辅助模块",   accent = CardPurple, loading = state.loadingBtn == ButtonKey.RESTART_AUX, enabled = enabled, isDark = isDark, onClick = vm::onRestartAux)
        LGActionCard(icon = "🚫", title = "完全关闭辅助", subtitle = "停止所有相关进程",   accent = CardRed,    loading = state.loadingBtn == ButtonKey.CLOSE_ALL,   enabled = enabled, isDark = isDark, onClick = vm::onCloseAll)
        LGActionCard(icon = "🛡", title = "清理防盗号",   subtitle = "清理设备并加固账号", accent = CardGreen,  loading = state.loadingBtn == ButtonKey.CLEAN,       enabled = enabled, isDark = isDark, onClick = vm::onClean)
    }
}

@Composable
private fun LGActionCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    loading: Boolean,
    enabled: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (loading) 0.97f else 1f,
        animationSpec = SpringDefault,
        label = "ac$title",
    )
    val rot by if (loading) {
        rememberInfiniteTransition(label = "ar$title").animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "rot$title",
        )
    } else { remember { mutableStateOf(0f) } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(size = 22.dp))
            .liquidGlass(isDark = isDark, cornerRadius = 22.dp, tintColor = accent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled && !loading) onClick() },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // 图标球
                Box(
                    modifier = Modifier
                        .size(size = 44.dp)
                        .clip(CircleShape)
                        .background(color = accent.copy(alpha = if (isDark) 0.22f else 0.14f))
                        .drawWithContent {
                            drawContent()
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = if (isDark) 0.20f else 0.40f), Color.Transparent),
                                    startY = 0f, endY = size.height * 0.55f,
                                )
                            )
                            drawCircle(
                                color = accent.copy(alpha = if (isDark) 0.30f else 0.18f),
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = icon,
                        fontSize = 20.sp,
                        modifier = if (loading) Modifier.graphicsLayer { rotationZ = rot } else Modifier,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF0D1E35),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (!enabled && !loading) "当前有任务执行中" else subtitle,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.48f) else Color(0xFF3C5A80).copy(alpha = 0.70f),
                    )
                }
            }
            // 状态标签
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 999.dp))
                    .glassChip(isDark = isDark, accentColor = accent)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = if (loading) "执行中" else "进入",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) accent.copy(alpha = 0.95f) else accent.copy(alpha = 0.85f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Footer
// ─────────────────────────────────────────────────────────────────────────────
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 20.dp))
            .liquidGlass(isDark = isDark, cornerRadius = 20.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "晚风工作室",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(alpha = 0.80f) else Color(0xFF0D1E35),
                )
                Text(
                    text = "版本 v${rememberAppVersionName()} · 客户服务版",
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF3C5A80).copy(alpha = 0.60f),
                )
            }
            LGChip(
                text = if (busy) "处理中" else "服务正常",
                accent = if (busy) AccentBlue else AccentGreen,
                isDark = isDark,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// InfoDialog — 玻璃弹窗
// ─────────────────────────────────────────────────────────────────────────────
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
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "知道了", color = AccentBlue, fontWeight = FontWeight.SemiBold) } },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.50f) else Color(0xFF3C5A80).copy(alpha = 0.70f))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(size = 16.dp))
                        .liquidGlass(isDark = isDark, cornerRadius = 16.dp, tintColor = StatusAmber)
                        .padding(all = 14.dp),
                ) {
                    Text(
                        text = bodyText.ifBlank { "暂无内容" }.trim(),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.82f) else Color(0xFF0D1E35),
                    )
                }
                Text(
                    text = footerText,
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.38f) else Color(0xFF3C5A80).copy(alpha = 0.55f),
                )
            }
        },
        containerColor = if (isDark) Color(0xFF111827).copy(alpha = 0.88f) else Color(0xFFF0F7FF).copy(alpha = 0.92f),
        shape = RoundedCornerShape(size = 30.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Toast — 悬浮玻璃胶囊
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().zIndex(10f), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.statusBarsPadding().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = toasts,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "tl",
            ) { list ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    list.forEach { t -> LGToastItem(toast = t, onDone = { onRemove(t.id) }) }
                }
            }
        }
    }
}

@Composable
private fun LGToastItem(toast: ToastData, onDone: () -> Unit) {
    LaunchedEffect(toast.id) { delay(3200L); onDone() }
    val dot = toast.color.toComposeColor()
    Row(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .clip(RoundedCornerShape(size = 999.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1C2B45).copy(alpha = 0.92f), Color(0xFF111827).copy(alpha = 0.88f))
                ),
                shape = RoundedCornerShape(size = 999.dp),
            )
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.5f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f),
                    size = size,
                )
                drawRoundRect(
                    color = dot.copy(alpha = 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f),
                    style = Stroke(width = 1.dp.toPx()),
                    size = size,
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(7.dp).background(color = dot, shape = CircleShape))
        Text(text = toast.msg, fontSize = 13.sp, color = Color.White.copy(alpha = 0.88f), maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 扩展函数
// ─────────────────────────────────────────────────────────────────────────────
private fun RunStatus.label() = when (this) { RunStatus.RUNNING -> "运行中"; RunStatus.LOADING -> "启动中"; RunStatus.STOPPED -> "已停止" }
private fun RunStatus.dotColor() = when (this) { RunStatus.RUNNING -> StatusGreen; RunStatus.LOADING -> StatusAmber; RunStatus.STOPPED -> StatusRed }
private fun ConnStatus.label() = when (this) { ConnStatus.CONNECTED -> "已连接"; ConnStatus.CONNECTING -> "连接中"; ConnStatus.DISCONNECTED -> "离线" }
private fun ConnStatus.dotColor() = when (this) { ConnStatus.CONNECTED -> StatusGreen; ConnStatus.CONNECTING -> StatusAmber; ConnStatus.DISCONNECTED -> StatusRed }
