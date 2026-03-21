package com.wanfeng.launcher.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * 半透明靠右边悬浮窗，不遮挡游戏主体区域。
 * 只有一个确认按钮，用户点击后消失。
 */
@Composable
fun FloatingOverlay(
    stage: FloatStage,
    isDark: Boolean,
    busy: Boolean,
    onConfirmHero: () -> Unit,
    onConfirmMatchEnd: () -> Unit,
) {
    val visible = stage != FloatStage.NONE

    AnimatedVisibility(
        visible = visible,
        enter  = fadeIn() + slideInHorizontally { it },
        exit   = fadeOut() + slideOutHorizontally { it },
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f),
    ) {
        // 贴右边居中
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            val accentColor = if (isDark) Color(0xFF9B6DFF) else Color(0xFF3D7EFF)
            val bgColor     = if (isDark) Color(0xBB0F0C1E) else Color(0xBBE8F3FF)

            Box(
                modifier = Modifier
                    .widthIn(max = 148.dp)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .drawWithContent {
                        drawContent()
                        // 顶部高光
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = if (isDark) 0.12f else 0.45f), Color.Transparent),
                                startY = 0f, endY = size.height * 0.4f,
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                            size = size,
                        )
                        // 边框
                        drawRoundRect(
                            color = accentColor.copy(alpha = if (isDark) 0.35f else 0.22f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx()),
                            size = size,
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 图标
                    Text(
                        text = if (stage == FloatStage.WAIT_HERO) "\u2694\uFE0F" else "\uD83C\uDFC1",
                        fontSize = 22.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    // 提示文字
                    Text(
                        text = if (stage == FloatStage.WAIT_HERO)
                            "已拉起游戏\n是否进入\n选英雄界面？"
                        else
                            "本局对局\n是否结束？",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.88f) else Color(0xFF0D1E35),
                    )
                    Spacer(Modifier.height(12.dp))
                    // 确认按钮
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    if (isDark) listOf(Color(0xFF7C3AED), Color(0xFF9B6DFF))
                                    else        listOf(Color(0xFF3D7EFF), Color(0xFF5B8EFF)),
                                    start = Offset(0f, 0f),
                                    end   = Offset(300f, 100f),
                                )
                            )
                            .drawWithContent {
                                drawContent()
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = if (isDark) 0.08f else 0.28f), Color.Transparent),
                                        0f, size.height * 0.45f,
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                    size = size,
                                )
                                drawRoundRect(
                                    color = Color.White.copy(alpha = if (isDark) 0.10f else 0.22f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                                    style = Stroke(1.dp.toPx()),
                                    size = size,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(
                            onClick = {
                                if (!busy) {
                                    if (stage == FloatStage.WAIT_HERO) onConfirmHero()
                                    else onConfirmMatchEnd()
                                }
                            },
                            modifier = Modifier.width(88.dp).height(36.dp),
                        ) {
                            Text(
                                text = "是",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                    if (busy) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "处理中…",
                            fontSize = 10.sp,
                            color = accentColor.copy(alpha = 0.70f),
                        )
                    }
                }
            }
        }
    }
}
