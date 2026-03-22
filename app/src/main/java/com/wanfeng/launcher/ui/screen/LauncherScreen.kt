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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import com.wanfeng.launcher.ui.theme.CardGreen
import com.wanfeng.launcher.ui.theme.CardPurple
import com.wanfeng.launcher.ui.theme.CardRed
import com.wanfeng.launcher.ui.theme.DarkBg0
import com.wanfeng.launcher.ui.theme.DarkBg100
import com.wanfeng.launcher.ui.theme.DarkBg45
import com.wanfeng.launcher.ui.theme.DarkAccentGreen
import com.wanfeng.launcher.ui.theme.DarkPrimary
import com.wanfeng.launcher.ui.theme.DarkSecondary
import com.wanfeng.launcher.ui.theme.DarkTertiary
import com.wanfeng.launcher.ui.theme.LaunchEndBlue
import com.wanfeng.launcher.ui.theme.LaunchEndPurple
import com.wanfeng.launcher.ui.theme.LaunchMidBlue
import com.wanfeng.launcher.ui.theme.LaunchMidPurple
import com.wanfeng.launcher.ui.theme.LaunchStartBlue
import com.wanfeng.launcher.ui.theme.LaunchStartPurple
import com.wanfeng.launcher.ui.theme.LightAccentGreen
import com.wanfeng.launcher.ui.theme.LightBg0
import com.wanfeng.launcher.ui.theme.LightBg100
import com.wanfeng.launcher.ui.theme.LightBg50
import com.wanfeng.launcher.ui.theme.LightPrimary
import com.wanfeng.launcher.ui.theme.LightSecondary
import com.wanfeng.launcher.ui.theme.LightTertiary
import com.wanfeng.launcher.ui.theme.StatusAmber
import com.wanfeng.launcher.ui.theme.StatusGreen
import com.wanfeng.launcher.ui.theme.StatusRed
import com.wanfeng.launcher.ui.theme.ToastText
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.ui.res.painterResource
import com.wanfeng.launcher.R

private fun Long.toComposeColor() = Color(this)
private val SpringDefault = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness    = Spring.StiffnessMediumLow,
)
private val SpringSnappy = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness    = Spring.StiffnessMedium,
)

// ── 主题色包：深色=紫，浅色=蓝 ───────────────────────────────────────────────
private data class ThemePalette(
    val primary:   Color,   // 主强调色
    val secondary: Color,   // 次强调色
    val tertiary:  Color,   // 第三色
    val accent:    Color,   // 绿色
    val launchStart: Color,
    val launchMid:   Color,
    val launchEnd:   Color,
    // 玻璃层颜色
    val glassHigh: Color,
    val glassMid:  Color,
    val glassLow:  Color,
    val glassDark: Color,
    val glassDarkHigh: Color,
    // 背景
    val bg0: Color, val bg45: Color, val bg100: Color,
    // 文字
    val textPrimary: Color,
    val textSecondary: Color,
)

private fun themePalette(isDark: Boolean): ThemePalette = if (isDark) ThemePalette(
    primary   = DarkPrimary,
    secondary = DarkSecondary,
    tertiary  = DarkTertiary,
    accent    = DarkAccentGreen,
    launchStart = LaunchStartPurple,
    launchMid   = LaunchMidPurple,
    launchEnd   = LaunchEndPurple,
    glassHigh     = Color.White.copy(alpha = 0.12f),
    glassMid      = Color.White.copy(alpha = 0.07f),
    glassLow      = Color.White.copy(alpha = 0.04f),
    glassDark     = Color(0xFF160D2A).copy(alpha = 0.58f),
    glassDarkHigh = Color.White.copy(alpha = 0.10f),
    bg0   = DarkBg0,
    bg45  = DarkBg45,
    bg100 = DarkBg100,
    textPrimary   = Color.White.copy(alpha = 0.92f),
    textSecondary = Color.White.copy(alpha = 0.50f),
) else ThemePalette(
    primary   = LightPrimary,
    secondary = LightSecondary,
    tertiary  = LightTertiary,
    accent    = LightAccentGreen,
    launchStart = LaunchStartBlue,
    launchMid   = LaunchMidBlue,
    launchEnd   = LaunchEndBlue,
    glassHigh     = Color.White.copy(alpha = 0.18f),
    glassMid      = Color.White.copy(alpha = 0.11f),
    glassLow      = Color.White.copy(alpha = 0.06f),
    glassDark     = Color.Transparent,
    glassDarkHigh = Color.Transparent,
    bg0   = LightBg0,
    bg45  = LightBg50,
    bg100 = LightBg100,
    textPrimary   = Color(0xFF0D1E35),
    textSecondary = Color(0xFF3C5A80).copy(alpha = 0.78f),
)

// ── Liquid Glass Modifier ─────────────────────────────────────────────────────
private fun Modifier.liquidGlass(
    p: ThemePalette,
    isDark: Boolean,
    cornerRadius: Dp = 24.dp,
    tintColor: Color = Color.Transparent,
    topColorStrength: Float = if (isDark) 0.14f else 0.07f,
): Modifier = this
    .background(
        brush = if (isDark) Brush.verticalGradient(colors = listOf(p.glassDarkHigh, p.glassDark, Color(0xFF0F0720).copy(alpha = 0.60f)))
                else        Brush.verticalGradient(colors = listOf(p.glassHigh, p.glassMid, p.glassLow)),
        shape = RoundedCornerShape(size = cornerRadius),
    )
    .then(
        if (tintColor != Color.Transparent)
            Modifier.background(
                color = tintColor.copy(alpha = topColorStrength),
                shape = RoundedCornerShape(size = cornerRadius),
            )
        else Modifier
    )
    .drawWithContent {
        drawContent()
        val cr = cornerRadius.toPx()
        // 顶部高光条
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) listOf(
                    // 深色：用主色（紫）轻微覆盖顶部，不用白色
                    tintColor.takeIf { it != Color.Transparent }
                        ?.copy(alpha = 0.18f) ?: Color.White.copy(alpha = 0.08f),
                    Color.Transparent,
                ) else listOf(
                    Color.White.copy(alpha = 0.48f),
                    Color.Transparent,
                ),
                startY = 0f, endY = size.height * 0.42f,
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
            size = size,
        )
        // 边缘光
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.75f),
                    if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.18f),
                    if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.55f),
                ),
                start = Offset(0f, 0f), end = Offset(size.width, size.height),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
            style = Stroke(width = 1.2.dp.toPx()),
            size = size,
        )
    }

private fun Modifier.glassChip(isDark: Boolean, accentColor: Color): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = if (isDark) listOf(accentColor.copy(alpha = 0.24f), accentColor.copy(alpha = 0.13f))
                     else        listOf(accentColor.copy(alpha = 0.14f), accentColor.copy(alpha = 0.07f)),
        ),
        shape = RoundedCornerShape(size = 999.dp),
    )
    .drawWithContent {
        drawContent()
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = if (isDark) 0.16f else 0.40f), Color.Transparent),
                startY = 0f, endY = size.height * 0.5f,
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f), size = size,
        )
        drawRoundRect(
            color = accentColor.copy(alpha = if (isDark) 0.36f else 0.22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f),
            style = Stroke(width = 1.dp.toPx()), size = size,
        )
    }

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
// 主屏幕
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LauncherScreen(vm: LauncherViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isDark = state.isDark
    val p      = remember(isDark) { themePalette(isDark) }
    val isBusy = state.loadingBtn != ButtonKey.NONE
    var showNotice by rememberSaveable { mutableStateOf(false) }
    var showGuide  by rememberSaveable { mutableStateOf(false) }
    var autoShown     by rememberSaveable { mutableStateOf(false) }
    var logClickCount by rememberSaveable { mutableStateOf(0) }
    var showLogDialog by rememberSaveable { mutableStateOf(false) }
    var logPwdInput   by rememberSaveable { mutableStateOf("") }
    var logPwdError   by rememberSaveable { mutableStateOf(false) }

    // 每分钟检查时间，自动切换主题
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            vm.refreshThemeByTime()
        }
    }
    LaunchedEffect(state.announcement) {
        if (state.announcement.isNotBlank() && !autoShown) { showNotice = true; autoShown = true }
    }

    // 深浅色1秒平滑渐变
    val bgC by androidx.compose.animation.animateColorAsState(
        if (isDark) Color(0xFF130920) else Color(0xFFE8F3FF),
        androidx.compose.animation.core.tween(1000), label = "bgC")
    val bgM by androidx.compose.animation.animateColorAsState(
        if (isDark) Color(0xFF0A0714) else Color(0xFFD8ECFF),
        androidx.compose.animation.core.tween(1000), label = "bgM")
    val bgE by androidx.compose.animation.animateColorAsState(
        if (isDark) Color(0xFF060410) else Color(0xFFC5E0FF),
        androidx.compose.animation.core.tween(1000), label = "bgE")

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.radialGradient(colors = listOf(bgC, bgM, bgE), radius = 2000f)
        )
    ) {
        AmbientLight(isDark = isDark, p = p)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            LGAnim(0)   { HeaderRow(isDark = isDark, p = p, onToggleTheme = vm::toggleTheme) }
            LGAnim(80)  { HeroCard(isDark = isDark, p = p, state = state, quote = state.gameQuote, onShowNotice = { showNotice = true }, onShowGuide = { showGuide = true }) }
            LGAnim(160) { LaunchButton(loading = state.loadingBtn == ButtonKey.LAUNCH, enabled = !isBusy, isDark = isDark, p = p, onClick = vm::onLaunch) }
            LGAnim(220) { ActionList(isDark = isDark, p = p, state = state, enabled = !isBusy, vm = vm) }
            LGAnim(280) {
                FooterRow(
                    isDark = isDark, p = p, busy = isBusy,
                    onVersionClick = {
                        logClickCount++
                        if (logClickCount >= 5) {
                            logClickCount = 0
                            showLogDialog = true
                            logPwdInput   = ""
                            logPwdError   = false
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        ToastLayer(toasts = state.toasts, onRemove = vm::removeToast)

        // 收货提醒：第3局后游戏进程刚起时顶部显示3秒
        androidx.compose.animation.AnimatedVisibility(
            visible = state.showDeliveryHint,
            enter = androidx.compose.animation.fadeIn(
                androidx.compose.animation.core.tween(400)) +
                androidx.compose.animation.slideInVertically(
                    androidx.compose.animation.core.tween(400)) { -it },
            exit  = androidx.compose.animation.fadeOut(
                androidx.compose.animation.core.tween(400)) +
                androidx.compose.animation.slideOutVertically(
                    androidx.compose.animation.core.tween(400)) { -it },
            modifier = Modifier.align(Alignment.TopCenter).zIndex(15f),
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xEE1A0D2E) else Color(0xEEEAF3FF))
                    .border(1.dp, p.primary.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "感觉好用的话，记得回启动器确认收货哦 😊",
                    fontSize = 13.sp,
                    color = p.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // 连点版本号 5 次 → Log 提取弹窗
        if (showLogDialog) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (logPwdInput == "WanFeng69996@!") {
                            showLogDialog = false
                            logPwdError   = false
                            Thread {
                                try {
                                    Runtime.getRuntime().exec(arrayOf(
                                        "su", "-c",
                                        "logcat -d -f /sdcard/wf_debug.log -v time 2>&1"
                                    )).waitFor()
                                } catch (_: Exception) {}
                            }.start()
                            vm.showToastPublic("日志已导出到 /sdcard/wf_debug.log", 0xFF9B6DFF)
                        } else {
                            logPwdError = true
                        }
                    }) { Text("确认", color = p.primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showLogDialog = false }) {
                        Text("取消", color = p.textSecondary)
                    }
                },
                title = {
                    Text("开发者选项", color = p.textPrimary, fontWeight = FontWeight.SemiBold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.OutlinedTextField(
                            value = logPwdInput,
                            onValueChange = { logPwdInput = it; logPwdError = false },
                            label = { Text("访问密码") },
                            singleLine = true,
                            isError = logPwdError,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        )
                        if (logPwdError) {
                            Text("密码错误", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                containerColor = if (isDark) Color(0xFF1A0D2E).copy(alpha = 0.96f)
                                 else Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(24.dp),
            )
        }


        InfoDialog(visible = showNotice, isDark = isDark, p = p, title = "系统公告", subtitle = "请阅读后再继续操作。",
            bodyText = state.announcement, footerText = "可随时点击首页按钮再次查看。", onDismiss = { showNotice = false })
        InfoDialog(visible = showGuide, isDark = isDark, p = p, title = "使用教程", subtitle = "操作说明，按需查阅。",
            bodyText = state.tutorial, footerText = "可随时点击首页按钮再次查看。", onDismiss = { showGuide = false })

        if (state.showDialog) {
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                confirmButton = { TextButton(onClick = vm::dismissDialog) { Text("确定", color = p.primary, fontWeight = FontWeight.SemiBold) } },
                title = { Text("提示", fontWeight = FontWeight.SemiBold, color = p.textPrimary) },
                text  = { Text(state.dialogMessage, lineHeight = 21.sp, color = p.textSecondary) },
                containerColor = if (isDark) Color(0xFF1A0E2E).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(28.dp),
            )
        }
    }
}

@Composable
private fun LGAnim(delayMs: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(600, delayMs, FastOutSlowInEasing)) +
            scaleIn(tween(600, delayMs, FastOutSlowInEasing), initialScale = 0.94f) +
            slideInVertically(tween(600, delayMs, FastOutSlowInEasing)) { it / 6 },
    ) { content() }
}

// ── 环境光 ────────────────────────────────────────────────────────────────────
@Composable
private fun AmbientLight(isDark: Boolean, p: ThemePalette) {
    val inf = rememberInfiniteTransition(label = "amb")
    val d1 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(12000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "d1")
    val d2 by inf.animateFloat(1f, 0f, infiniteRepeatable(tween(9000,  easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "d2")

    Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
        listOf(
            Triple(420.dp, Offset(-80f + 30f * d1, -100f + 20f * d2), Brush.radialGradient(listOf(p.primary.copy(alpha = if (isDark) 0.22f else 0.11f), p.secondary.copy(alpha = if (isDark) 0.12f else 0.06f), Color.Transparent))),
            Triple(300.dp, Offset(60f - 20f * d2, -40f + 30f * d1),   Brush.radialGradient(listOf(p.tertiary.copy(alpha = if (isDark) 0.16f else 0.08f), Color.Transparent))),
            Triple(260.dp, Offset(40f - 15f * d1, 50f - 15f * d2),    Brush.radialGradient(listOf(p.accent.copy(alpha = if (isDark) 0.10f else 0.06f), Color.Transparent))),
            Triple(220.dp, Offset(-30f + 20f * d2, 50f + 20f * d1),   Brush.radialGradient(listOf(p.secondary.copy(alpha = if (isDark) 0.12f else 0.06f), Color.Transparent))),
        ).forEachIndexed { i, (sz, off, brush) ->
            Box(
                modifier = Modifier
                    .size(sz)
                    .align(when (i) { 1 -> Alignment.TopEnd; 2 -> Alignment.BottomEnd; 3 -> Alignment.BottomStart; else -> Alignment.TopStart })
                    .offset(x = off.x.dp, y = off.y.dp)
                    .blur(100.dp)
                    .background(brush = brush, shape = CircleShape)
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderRow(isDark: Boolean, p: ThemePalette, onToggleTheme: () -> Unit) {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { delay(1000L); time = Calendar.getInstance() } }
    val timeStr = remember(time) { "%02d:%02d:%02d".format(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.SECOND)) }
    val dateStr = remember(time) {
        val d = arrayOf("周日","周一","周二","周三","周四","周五","周六")
        "${time.get(Calendar.MONTH)+1}月${time.get(Calendar.DAY_OF_MONTH)}日 ${d[time.get(Calendar.DAY_OF_WEEK)-1]}"
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LGLogo(logoSize = 44.dp, isDark = isDark, p = p)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("WANFENG STUDIO", fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium,
                    color = p.textSecondary)
                Text("三角洲服务面板", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    // 底色：主色渐变上半截 → 透明，iOS 风格胶囊
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                p.primary.copy(alpha = if (isDark) 0.28f else 0.18f),
                                p.primary.copy(alpha = if (isDark) 0.10f else 0.06f),
                            )
                        ),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .drawWithContent {
                        drawContent()
                        // 顶部白色高光条（液态玻璃感）
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.16f else 0.45f),
                                    Color.Transparent,
                                ),
                                startY = 0f, endY = size.height * 0.45f,
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                            size = size,
                        )
                        // 边框细线
                        drawRoundRect(
                            color = p.primary.copy(alpha = if (isDark) 0.35f else 0.22f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx()),
                            size = size,
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = p.textPrimary,
                    )
                    Text(text = dateStr, fontSize = 10.sp, color = p.textSecondary)
                }
            }
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp))
                    .liquidGlass(p = p, isDark = isDark, cornerRadius = 14.dp, tintColor = p.primary)
                    .clickable(remember { MutableInteractionSource() }, null, onClick = onToggleTheme),
                contentAlignment = Alignment.Center,
            ) {
                // 深色=紫月 浅色=蓝日
                Text(if (isDark) "🌙" else "☀", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun LGLogo(logoSize: Dp, isDark: Boolean, p: ThemePalette) {
    Box(
        modifier = Modifier
            .size(size = logoSize)
            // iOS 风格圆角：约 22% 边长
            .clip(RoundedCornerShape(size = logoSize * 0.28f))
            // 边框发光
            .drawWithContent {
                drawContent()
                val cr = (logoSize.value * 0.28f).dp.toPx()
                // 顶部主色渐变（仅上半截）
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            p.primary.copy(alpha = if (isDark) 0.55f else 0.35f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = size.height * 0.52f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
                    size = size,
                )
                // 顶部白色高光
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = if (isDark) 0.22f else 0.50f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.38f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
                    size = size,
                )
                // 边框
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.30f else 0.70f),
                            p.primary.copy(alpha = if (isDark) 0.20f else 0.15f),
                        ),
                        start = Offset(0f, 0f), end = Offset(size.width, size.height),
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr),
                    style = Stroke(width = 1.2.dp.toPx()),
                    size = size,
                )
            },
    ) {
        // 图片填充
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// ── HeroCard ──────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(isDark: Boolean, p: ThemePalette, state: LauncherUiState, quote: String, onShowNotice: () -> Unit, onShowGuide: () -> Unit) {
    val greeting = remember { getGreeting() }
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .liquidGlass(p = p, isDark = isDark, cornerRadius = 28.dp, tintColor = p.primary, topColorStrength = if (isDark) 0.22f else 0.14f)
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                LGChip("游戏 ${state.gameStatus.label()}", state.gameStatus.dotColor(), isDark)
                LGChip("辅助 ${state.auxStatus.label()}", state.auxStatus.dotColor(), isDark)
                if (state.serverStatus != ConnStatus.DISCONNECTED)
                    LGChip("服务器 ${state.serverStatus.label()}", state.serverStatus.dotColor(), isDark)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(greeting.main, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
                Text(greeting.sub, fontSize = 14.sp, color = p.textSecondary, lineHeight = 20.sp)
            }
            if (quote.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .liquidGlass(p = p, isDark = isDark, cornerRadius = 18.dp, tintColor = p.secondary)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Text("💬", fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
                        Text(quote, fontSize = 13.sp, fontStyle = FontStyle.Italic,
                            color = if (isDark) Color.White.copy(alpha = 0.68f) else p.secondary.copy(alpha = 0.82f),
                            lineHeight = 19.sp)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // 实心主按钮
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(p.launchStart, p.launchEnd)))
                        .drawWithContent {
                            drawContent()
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    if (isDark) listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                                    else        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    0f, size.height * 0.45f,
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()), size = size,
                            )
                            drawRoundRect(color = Color.White.copy(alpha = if (isDark) 0.12f else 0.22f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                                style = Stroke(1.dp.toPx()), size = size)
                        }
                        .clickable(remember { MutableInteractionSource() }, null, onClick = onShowNotice)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("查看公告", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White) }
                // 透明玻璃按钮
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .liquidGlass(p = p, isDark = isDark, cornerRadius = 16.dp)
                        .clickable(remember { MutableInteractionSource() }, null, onClick = onShowGuide)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("使用教程", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White.copy(alpha = 0.82f) else p.primary.copy(alpha = 0.90f)) }
            }
        }
    }
}

@Composable
private fun LGChip(text: String, accent: Color, isDark: Boolean) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).glassChip(isDark = isDark, accentColor = accent).padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        LGPulseDot(color = accent)
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) accent.copy(alpha = 0.95f) else accent.copy(alpha = 0.85f))
    }
}

@Composable
private fun LGPulseDot(color: Color) {
    val a by rememberInfiniteTransition(label = "pd").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pda",
    )
    Box(modifier = Modifier.size(6.dp).background(color = color.copy(alpha = a), shape = CircleShape))
}

// ── LaunchButton ──────────────────────────────────────────────────────────────
@Composable
private fun LaunchButton(loading: Boolean, enabled: Boolean, isDark: Boolean, p: ThemePalette, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (loading) 0.97f else 1f, SpringDefault, label = "ls")
    val shimmer by rememberInfiniteTransition(label = "sh").animateFloat(
        -1f, 2f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "shv",
    )
    Box(
        modifier = Modifier.fillMaxWidth().scale(scale).clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(p.launchStart, p.launchMid, p.launchEnd), Offset(0f,0f), Offset(1200f,300f)))
            .drawWithContent {
                drawContent()
                val sw = size.width * 0.5f
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.08f), Color.Transparent),
                        startX = shimmer * size.width - sw * 0.5f,
                        endX   = shimmer * size.width + sw,
                    )
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        if (isDark) listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                        else        listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                        0f, size.height * 0.40f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()), size = size,
                )
                drawRoundRect(color = Color.White.copy(alpha = if (isDark) 0.12f else 0.26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx()),
                    style = Stroke(1.2.dp.toPx()), size = size)
            }
            .clickable(remember { MutableInteractionSource() }, null) { if (enabled && !loading) onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .drawWithContent {
                            drawContent()
                            drawCircle(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), 0f, size.height * 0.5f))
                            drawCircle(Color.White.copy(alpha = 0.20f), style = Stroke(1.dp.toPx()))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (loading) {
                        val rot by rememberInfiniteTransition(label = "spin").animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "r")
                        Text(text = "↻", color = Color.White, fontSize = 22.sp, modifier = Modifier.graphicsLayer { rotationZ = rot })
                    } else Text(text = "▶", color = Color.White, fontSize = 18.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when { loading -> "正在启动中…"; !enabled -> "请等待当前任务完成"; else -> "开始启动" },
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                    )
                    Text(
                        if (loading) "正在执行启动流程，请勿重复点击" else "一键启动辅助程序与游戏",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
            Text("›", fontSize = 26.sp, color = Color.White.copy(alpha = 0.70f), fontWeight = FontWeight.Light)
        }
    }
}

// ── ActionList ────────────────────────────────────────────────────────────────
@Composable
private fun ActionList(isDark: Boolean, p: ThemePalette, state: LauncherUiState, enabled: Boolean, vm: LauncherViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("快捷操作", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp,
            color = p.textSecondary, modifier = Modifier.padding(horizontal = 4.dp))
        LGActionCard("🚫","完全关闭辅助","停止所有相关进程",  CardRed,    state.loadingBtn==ButtonKey.CLOSE_ALL,   enabled, isDark, p, vm::onCloseAll)
        LGActionCard("🛡","清理防盗号",  "清理设备并加固账号", CardGreen,  state.loadingBtn==ButtonKey.CLEAN,       enabled, isDark, p, vm::onClean)
    }
}

@Composable
private fun LGActionCard(icon: String, title: String, subtitle: String, accent: Color, loading: Boolean, enabled: Boolean, isDark: Boolean, p: ThemePalette, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (loading) 0.97f else 1f, SpringDefault, label = "ac$title")
    val rot by if (loading) rememberInfiniteTransition(label = "ar$title").animateFloat(0f,360f,infiniteRepeatable(tween(900,easing=LinearEasing)),label="rot")
               else remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier.fillMaxWidth().scale(scale).clip(RoundedCornerShape(22.dp))
            .liquidGlass(p = p, isDark = isDark, cornerRadius = 22.dp, tintColor = accent)
            .clickable(remember { MutableInteractionSource() }, null) { if (enabled && !loading) onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(accent.copy(alpha = if (isDark) 0.22f else 0.14f))
                        .drawWithContent {
                            drawContent()
                            drawCircle(Brush.verticalGradient(listOf(Color.White.copy(alpha = if (isDark) 0.20f else 0.40f), Color.Transparent), 0f, size.height * 0.55f))
                            drawCircle(accent.copy(alpha = if (isDark) 0.30f else 0.18f), style = Stroke(1.dp.toPx()))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(icon, fontSize = 20.sp, modifier = if (loading) Modifier.graphicsLayer { rotationZ = rot } else Modifier)
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (!enabled && !loading) "当前有任务执行中" else subtitle, fontSize = 12.sp, color = p.textSecondary)
                }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).glassChip(isDark = isDark, accentColor = accent).padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(if (loading) "执行中" else "进入", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) accent.copy(alpha = 0.95f) else accent.copy(alpha = 0.85f))
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
private fun FooterRow(isDark: Boolean, p: ThemePalette, busy: Boolean, onVersionClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .liquidGlass(p = p, isDark = isDark, cornerRadius = 20.dp).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("晚风工作室", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
                Text("版本 v${rememberAppVersionName()} · 客户服务版", fontSize = 11.sp, color = p.textSecondary)
            }
            LGChip(if (busy) "处理中" else "服务正常", if (busy) p.primary else p.accent, isDark)
        }
    }
}

// ── InfoDialog ────────────────────────────────────────────────────────────────
@Composable
private fun InfoDialog(visible: Boolean, isDark: Boolean, p: ThemePalette, title: String, subtitle: String, bodyText: String, footerText: String, onDismiss: () -> Unit) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了", color = p.primary, fontWeight = FontWeight.SemiBold) } },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = p.textSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .liquidGlass(p = p, isDark = isDark, cornerRadius = 16.dp, tintColor = StatusAmber)
                        .padding(14.dp),
                ) {
                    Text(bodyText.ifBlank { "暂无内容" }.trim(), fontSize = 14.sp, lineHeight = 22.sp, color = p.textPrimary)
                }
                Text(footerText, fontSize = 11.sp, color = p.textSecondary)
            }
        },
        containerColor = if (isDark) Color(0xFF1A0D2E).copy(alpha = 0.92f) else Color(0xFFF0F7FF).copy(alpha = 0.92f),
        shape = RoundedCornerShape(30.dp),
    )
}

// ── Toast ─────────────────────────────────────────────────────────────────────
@Composable
private fun ToastLayer(toasts: List<ToastData>, onRemove: (Long) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().zIndex(10f), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.statusBarsPadding().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(toasts, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }, label = "tl") { list ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    list.forEach { t -> LGToast(t) { onRemove(t.id) } }
                }
            }
        }
    }
}

@Composable
private fun LGToast(toast: ToastData, onDone: () -> Unit) {
    LaunchedEffect(toast.id) { delay(3200L); onDone() }
    val dot = toast.color.toComposeColor()
    Row(
        modifier = Modifier.widthIn(max = 340.dp).clip(RoundedCornerShape(999.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1C1030).copy(alpha = 0.92f), Color(0xFF100820).copy(alpha = 0.88f))))
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent), 0f, size.height * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f), size = size,
                )
                drawRoundRect(color = dot.copy(alpha = 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f),
                    style = Stroke(1.dp.toPx()), size = size)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(7.dp).background(dot, CircleShape))
        Text(toast.msg, fontSize = 13.sp, color = ToastText, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ── 扩展函数 ──────────────────────────────────────────────────────────────────
private fun RunStatus.label() = when (this) { RunStatus.RUNNING->"运行中"; RunStatus.LOADING->"启动中"; RunStatus.STOPPED->"已停止" }
private fun RunStatus.dotColor() = when (this) { RunStatus.RUNNING->StatusGreen; RunStatus.LOADING->StatusAmber; RunStatus.STOPPED->StatusRed }
private fun ConnStatus.label() = when (this) { ConnStatus.CONNECTED->"已连接"; ConnStatus.CONNECTING->"连接中"; ConnStatus.DISCONNECTED->"离线" }
private fun ConnStatus.dotColor() = when (this) { ConnStatus.CONNECTED->StatusGreen; ConnStatus.CONNECTING->StatusAmber; ConnStatus.DISCONNECTED->StatusRed }
