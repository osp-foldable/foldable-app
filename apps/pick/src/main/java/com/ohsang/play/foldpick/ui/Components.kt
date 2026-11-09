package com.ohsang.play.foldpick.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.ColorFilter
import com.ohsang.play.fold.core.C
import com.ohsang.play.fold.core.NumberFont
import com.ohsang.play.fold.core.vibrateShort
import com.ohsang.play.foldpick.PRESETS
import com.ohsang.play.foldpick.R
import com.ohsang.play.foldpick.UiState

// ---------- 공통 버튼 ----------

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(C.Accent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = C.OnAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, iconRes: Int? = null) {
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(C.Surface2)
            .border(1.dp, C.Border, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        if (iconRes != null) {
            Image(painterResource(iconRes), null, Modifier.size(16.dp), colorFilter = ColorFilter.tint(C.Text))
        }
        Text(text, color = C.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsIconButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(C.Surface)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "설정" },
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_tune), null, Modifier.size(22.dp), colorFilter = ColorFilter.tint(C.Text))
    }
}

@Composable
fun AppTitle() = Text("펼쳐 뽑기", color = C.Muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)

@Composable
fun RangeChip(s: UiState) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp)).background(C.Surface).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            "1 ~ ${s.max}" + if (s.dedupe) " · 중복 제외" else "",
            color = C.Muted, fontSize = 13.sp,
        )
    }
}

// ---------- 설정 요소 ----------

/** 최대 숫자 입력칸. 입력을 마치거나 포커스가 빠지면 범위로 맞춤 */
@Composable
fun MaxNumberField(
    s: UiState,
    onChange: (String) -> Unit,
    onCommit: () -> Unit,
    height: Dp = 96.dp,
    fontSize: TextUnit = 64.sp,
) {
    val focus = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = s.maxText,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(
            color = C.Accent, fontSize = fontSize, fontWeight = FontWeight.Bold,
            fontFamily = NumberFont, textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(C.Accent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onCommit(); focus.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "최대 숫자" }
            .onFocusChanged {
                if (focused && !it.isFocused) onCommit()
                focused = it.isFocused
            },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(18.dp))
                    .background(C.Bg)
                    .border(2.dp, if (focused) C.Accent else C.Border, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) { inner() }
        },
    )
}

@Composable
fun PresetRow(s: UiState, onPick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PRESETS.forEach { v ->
            val on = s.maxText == v.toString()
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (on) C.Accent else C.Surface2)
                    .border(1.dp, if (on) C.Accent else C.Border, RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button) { onPick(v) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    v.toString(), color = if (on) C.OnAccent else C.Text,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = NumberFont,
                )
            }
        }
    }
}

@Composable
fun SwitchTrack(on: Boolean, width: Dp = 52.dp, height: Dp = 30.dp) {
    val knob = height - 6.dp
    Box(
        Modifier
            .size(width, height)
            .clip(RoundedCornerShape(50))
            .background(if (on) C.Accent else C.Off)
    ) {
        Box(
            Modifier
                .offset(x = if (on) width - knob - 3.dp else 3.dp, y = 3.dp)
                .size(knob)
                .clip(CircleShape)
                .background(if (on) C.OnAccent else C.Text)
        )
    }
}

/** 설명 포함 중복 제외 토글 (설정 화면용) */
@Composable
fun DedupeRow(on: Boolean, onToggle: () -> Unit, background: Color = C.Surface) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .toggleable(value = on, role = Role.Switch, onValueChange = { onToggle() })
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("중복 제외", color = C.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("이미 나온 숫자는 다시 안 나와요", color = C.Muted, fontSize = 13.sp)
        }
        SwitchTrack(on)
    }
}

/** 상단 바용 작은 토글 */
@Composable
fun DedupeChip(on: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(C.Surface)
            .toggleable(value = on, role = Role.Switch, onValueChange = { onToggle() })
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("중복 제외", color = C.Text, fontSize = 14.sp)
        SwitchTrack(on, width = 44.dp, height = 26.dp)
    }
}

// ---------- 숫자 ----------

@Composable
fun NumberTile(value: Int, highlight: Boolean, size: Dp = 40.dp) {
    Box(
        Modifier
            .heightIn(min = size)
            .width(if (value >= 100) size + 14.dp else size)
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight) C.Accent else C.Surface2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            value.toString(), color = if (highlight) C.OnAccent else C.Text,
            fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = NumberFont,
        )
    }
}

/**
 * 가운데에서 튀어나오는 숫자.
 * pending이면 0 → 1.22배 → 0.94배 → 1배 (0.6초) + 진동, 끝나면 onShown 호출.
 * 이미 보여준 숫자면 애니메이션 없이 그대로 표시.
 */
@Composable
fun PopNumber(value: Int, pending: Boolean, onShown: () -> Unit, fontSize: TextUnit = 240.sp) {
    val context = LocalContext.current
    val scale = remember { Animatable(if (pending) 0f else 1f) }
    LaunchedEffect(value, pending) {
        if (!pending) { scale.snapTo(1f); return@LaunchedEffect }
        scale.snapTo(0f)
        context.vibrateShort()
        scale.animateTo(1f, keyframes {
            durationMillis = 600
            1.22f at 270 using FastOutSlowInEasing
            0.94f at 430
        })
        onShown()
    }
    Text(
        value.toString(),
        color = C.Accent,
        fontSize = fontSize,
        lineHeight = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = NumberFont,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = (scale.value * 2f).coerceIn(0f, 1f)
        },
    )
}

// ---------- 그림 ----------

/** 접힌 폰 → 화살표 → 펼친 폰 */
@Composable
fun UnfoldIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier.size(200.dp, 150.dp)) {
        val u = size.width / 200f
        val stroke = Stroke(width = 3 * u, cap = StrokeCap.Round)
        drawRoundRect(C.Muted, Offset(18 * u, 25 * u), Size(54 * u, 100 * u), CornerRadius(10 * u), style = stroke)
        drawLine(C.Accent, Offset(88 * u, 75 * u), Offset(114 * u, 75 * u), 3 * u, StrokeCap.Round)
        drawLine(C.Accent, Offset(106 * u, 66 * u), Offset(115 * u, 75 * u), 3 * u, StrokeCap.Round)
        drawLine(C.Accent, Offset(106 * u, 84 * u), Offset(115 * u, 75 * u), 3 * u, StrokeCap.Round)
        drawRoundRect(C.Text, Offset(128 * u, 25 * u), Size(54 * u, 100 * u), CornerRadius(10 * u), style = stroke)
        drawLine(
            Color(0xFF5A5D67), Offset(155 * u, 30 * u), Offset(155 * u, 120 * u), 2 * u,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4 * u, 5 * u)),
        )
        drawCircle(C.Accent, 5 * u, Offset(155 * u, 75 * u))
    }
}

/** 점선 테두리 (안내 영역용) */
fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
    )
}

@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))
