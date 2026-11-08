package com.ohsang.play.fold.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** 시안의 색 그대로 */
object C {
    val Bg = Color(0xFF141519)
    val Surface = Color(0xFF1F2127)
    val Surface2 = Color(0xFF2A2D35)
    val Border = Color(0xFF3A3D46)
    val Off = Color(0xFF4A4D57)
    val Text = Color(0xFFF4F4F6)
    val Muted = Color(0xFF9EA1AB)
    val Accent = Color(0xFFFFD23F)
    val OnAccent = Color(0xFF141519)
}

/**
 * 숫자용 글꼴. 시안은 Space Grotesk.
 * core/fold/src/main/res/font/space_grotesk.ttf 를 넣고 아래를 FontFamily(Font(R.font.space_grotesk))로 바꾸면 됨.
 */
val NumberFont: FontFamily = FontFamily.Default
