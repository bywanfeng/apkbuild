package com.wanfeng.launcher.ui.theme

import androidx.compose.ui.graphics.Color

// ── 浅色模式主色（蓝色系）────────────────────────────────────────────────────
val LightPrimary     = Color(0xFF3D7EFF)
val LightSecondary   = Color(0xFF5B8EFF)
val LightTertiary    = Color(0xFF00BFFF)
val LightAccentGreen = Color(0xFF00C896)

// ── 深色模式主色（紫色系）────────────────────────────────────────────────────
val DarkPrimary     = Color(0xFF9B6DFF)
val DarkSecondary   = Color(0xFFB490FF)
val DarkTertiary    = Color(0xFFCF9FFF)
val DarkAccentGreen = Color(0xFF34D399)

// ── 向后兼容别名（Theme.kt / LauncherScreen 旧引用）──────────────────────────
val AccentBlue   = LightPrimary
val AccentIndigo = LightSecondary
val AccentCyan   = LightTertiary
val AccentGreen  = LightAccentGreen

// ── 动作卡片颜色 ──────────────────────────────────────────────────────────────
val CardPurple = Color(0xFF9B6DFF)
val CardRed    = Color(0xFFEF4444)
val CardGreen  = Color(0xFF22C55E)

// ── 状态颜色 ──────────────────────────────────────────────────────────────────
val StatusGreen = Color(0xFF22C55E)
val StatusRed   = Color(0xFFEF4444)
val StatusAmber = Color(0xFFF59E0B)

// ── 渐变按钮（深色紫，浅色蓝）────────────────────────────────────────────────
val LaunchStartBlue   = Color(0xFF3D7EFF)
val LaunchMidBlue     = Color(0xFF5B8EFF)
val LaunchEndBlue     = Color(0xFF00BFFF)
val LaunchStartPurple = Color(0xFF7C3AED)
val LaunchMidPurple   = Color(0xFF9B6DFF)
val LaunchEndPurple   = Color(0xFFB490FF)
// 向后兼容
val LaunchStart = LaunchStartBlue
val LaunchMid   = LaunchMidBlue
val LaunchEnd   = LaunchEndBlue

// ── 背景色 ────────────────────────────────────────────────────────────────────
val DarkBg0   = Color(0xFF0A0714)
val DarkBg45  = Color(0xFF0F0C1E)
val DarkBg100 = Color(0xFF130D24)
val LightBg0   = Color(0xFFEEF5FF)
val LightBg50  = Color(0xFFE3EDFF)
val LightBg100 = Color(0xFFD8E6FF)

// ── Theme.kt 需要的 Surface / Text / Outline 变量 ────────────────────────────
val DarkSurface        = Color(0xFF15102A)
val DarkSurfaceVariant = Color(0xFF1E1535)
val DarkText           = Color(0xFFF3F0FF)
val DarkSubText        = Color(0xFFB0A0CC)
val DarkOutline        = Color(0x30FFFFFF)

val LightSurface        = Color(0xFFF4F8FF)
val LightSurfaceVariant = Color(0xFFE8F0FF)
val LightText           = Color(0xFF0D1535)
val LightSubText        = Color(0xFF4A5E80)
val LightOutline        = Color(0x1A3D7EFF)

// ── Toast ─────────────────────────────────────────────────────────────────────
val ToastBg   = Color(0xF0100820)
val ToastText = Color(0xFFF3F0FF)
