package com.wanfeng.launcher.ui.theme

import androidx.compose.ui.graphics.Color

// ── 浅色模式主色（蓝色系）────────────────────────────────────────────────────
val LightPrimary   = Color(0xFF3D7EFF)   // 主蓝
val LightSecondary = Color(0xFF5B8EFF)   // 浅蓝
val LightTertiary  = Color(0xFF00BFFF)   // 青蓝
val LightAccentGreen = Color(0xFF00C896)

// ── 深色模式主色（紫色系）────────────────────────────────────────────────────
val DarkPrimary   = Color(0xFF9B6DFF)    // 主紫
val DarkSecondary = Color(0xFFB490FF)    // 浅紫
val DarkTertiary  = Color(0xFFCF9FFF)    // 淡紫
val DarkAccentGreen = Color(0xFF34D399)

// ── 向后兼容别名（LauncherScreen 使用）───────────────────────────────────────
val AccentBlue   = LightPrimary
val AccentIndigo = LightSecondary
val AccentCyan   = LightTertiary
val AccentGreen  = LightAccentGreen

// ── 动作卡片颜色（跟随主题重写）──────────────────────────────────────────────
val CardPurple = Color(0xFF9B6DFF)
val CardRed    = Color(0xFFEF4444)
val CardGreen  = Color(0xFF22C55E)

// ── 状态颜色（通用）──────────────────────────────────────────────────────────
val StatusGreen = Color(0xFF22C55E)
val StatusRed   = Color(0xFFEF4444)
val StatusAmber = Color(0xFFF59E0B)

// ── 渐变按钮（深色紫，浅色蓝，运行时动态选择）────────────────────────────────
val LaunchStartBlue = Color(0xFF3D7EFF)
val LaunchMidBlue   = Color(0xFF5B8EFF)
val LaunchEndBlue   = Color(0xFF00BFFF)

val LaunchStartPurple = Color(0xFF7C3AED)
val LaunchMidPurple   = Color(0xFF9B6DFF)
val LaunchEndPurple   = Color(0xFFB490FF)

// 向后兼容
val LaunchStart = LaunchStartBlue
val LaunchMid   = LaunchMidBlue
val LaunchEnd   = LaunchEndBlue

// ── 背景色 ────────────────────────────────────────────────────────────────────
val DarkBg0   = Color(0xFF0A0714)   // 深紫黑
val DarkBg45  = Color(0xFF0F0C1E)
val DarkBg100 = Color(0xFF130D24)

val LightBg0   = Color(0xFFEEF5FF)
val LightBg50  = Color(0xFFE3EDFF)
val LightBg100 = Color(0xFFD8E6FF)

// ── Toast ─────────────────────────────────────────────────────────────────────
val ToastBg   = Color(0xF0101020)
val ToastText = Color(0xFFF3F7FF)
