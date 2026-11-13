package com.ohsang.util.fold.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

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

/** 숫자용 글꼴: Space Grotesk (SIL OFL 1.1, core/fold/SPACE_GROTESK_OFL.txt) */
val NumberFont: FontFamily = FontFamily(
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
)
