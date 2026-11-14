package com.ohsang.util.foldmagic.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ohsang.util.fold.core.BottomBannerAd
import com.ohsang.util.fold.core.Posture
import com.ohsang.util.foldmagic.BuildConfig
import com.ohsang.util.foldmagic.MagicViewModel
import com.ohsang.util.foldmagic.Phase
import com.ohsang.util.foldmagic.R
import com.ohsang.util.foldmagic.UiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 시안 색 */
private object M {
    val Bg = Color(0xFF16142A)
    val Surface = Color(0xFF24213D)
    val Text = Color(0xFFF2F0FA)
    val Muted = Color(0xFFA9A5C2)
    val Line = Color(0xFF3A3658)
    val BodyTop = Color(0xFF4A4D63)
    val BodyMid = Color(0xFF303246)
    val BodyBottom = Color(0xFF26283A)
    val Bezel = Color(0xFF1A1B28)
    val LcdOn = Color(0xFFB9CC8B)
    val LcdOff = Color(0xFF6F7A55)
    val Ink = Color(0xFF24301A)
    val Key = Color(0xFF3B3E54)
    val KeyLit = Color(0xFF45496A)
    val KeyText = Color(0xFFE8E6F3)
    val Magic = Color(0xFFB9B0FF)
}

/** 액정 글꼴: 갈무리 (SIL OFL 1.1, apps/magic/GALMURI_OFL.md) */
private val Pixel = FontFamily(
    Font(R.font.galmuri11, FontWeight.Normal),
    Font(R.font.galmuri11_bold, FontWeight.Bold),
)

private val bodyBrush = Brush.linearGradient(listOf(M.BodyTop, M.BodyMid, M.BodyBottom))

@Composable
fun AppRoot(vm: MagicViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(M.Bg).safeDrawingPadding()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (s.posture == Posture.UNFOLDED) OpenPhoneScreen(s, vm) else CoverScreen(s, vm)
        }
        if (s.posture != Posture.UNKNOWN) {
            BottomBannerAd(BuildConfig.BANNER_AD_UNIT_ID, Modifier.padding(top = 8.dp))
        }
    }
}

// ---------------- 커버 화면 (접은 상태 / 일반 폰) ----------------

@Composable
private fun CoverScreen(s: UiState, vm: MagicViewModel) {
    // 일반 폰은 접을 수 없으니 이 화면에서 바로 대답을 보여줌
    val showLcd = !s.foldable && s.phase != Phase.IDLE

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
        Text("마법의 폴더폰", color = M.Muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showLcd) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.95f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(bodyBrush)
                        .padding(14.dp)
                ) { Lcd(s, vm, Modifier.fillMaxSize()) }
            } else {
                ClosedPhone()
                Spacer(Modifier.height(30.dp))
                Text(
                    "궁금한 걸 마음속으로\n물어보고 " + if (s.foldable) "폰을 펼치세요" else "아래 버튼을 누르세요",
                    color = M.Text, fontSize = 26.sp, lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    if (s.foldable) "펼치는 순간 폴더폰이 대답해 줘요" else "폴더블 폰에선 접었다 펴서 물어봐요",
                    color = M.Muted, fontSize = 15.sp,
                )
            }
        }

        VoiceRow(s, vm::toggleVoice)
        if (!s.foldable) {
            Spacer(Modifier.height(12.dp))
            AskButton(vm::ask, Modifier.fillMaxWidth())
        }
    }
}

/** 닫힌 폴더폰 + 바깥 액정 (시계, 질문 대기_) */
@Composable
private fun ClosedPhone() {
    var now by remember { mutableStateOf(clock()) }
    LaunchedEffect(Unit) { while (true) { delay(10_000); now = clock() } }

    Column(
        Modifier
            .size(180.dp, 236.dp)
            .shadow(24.dp, RoundedCornerShape(34.dp, 34.dp, 22.dp, 22.dp))
            .clip(RoundedCornerShape(34.dp, 34.dp, 22.dp, 22.dp))
            .background(bodyBrush)
            .padding(top = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(54.dp, 6.dp).clip(RoundedCornerShape(3.dp)).background(M.Bezel))
        Spacer(Modifier.height(22.dp))
        Box(
            Modifier.size(116.dp, 86.dp).clip(RoundedCornerShape(10.dp)).background(M.Bezel).padding(7.dp)
        ) {
            Column(
                Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)).background(M.LcdOn),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(now, color = M.Ink, fontFamily = Pixel, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row {
                    Text("질문 대기", color = M.Ink, fontFamily = Pixel, fontSize = 11.sp)
                    Text("_", color = M.Ink, fontFamily = Pixel, fontSize = 11.sp, modifier = Modifier.alpha(blink()))
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.padding(bottom = 22.dp).size(6.dp).clip(CircleShape).background(M.Magic))
    }
}

@Composable
private fun VoiceRow(s: UiState, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(M.Surface)
            .toggleable(value = s.voice, role = Role.Switch, onValueChange = { onToggle() })
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("목소리로 읽어주기", color = M.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                if (s.voiceReady == false) "이 기기엔 한국어 음성이 없어 글자로만 보여줘요" else "대답을 소리 내어 읽어줘요",
                color = M.Muted, fontSize = 13.sp,
            )
        }
        Switch(s.voice)
    }
}

@Composable
private fun Switch(on: Boolean) {
    Box(
        Modifier.size(52.dp, 30.dp).clip(RoundedCornerShape(15.dp)).background(if (on) M.LcdOn else Color(0xFF4A4762))
    ) {
        Box(
            Modifier
                .offset(x = if (on) 25.dp else 3.dp, y = 3.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (on) M.Ink else M.Text)
        )
    }
}

@Composable
private fun AskButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(M.LcdOn)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text("물어보기", color = M.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

// ---------------- 펼친 화면: 열린 폴더폰 ----------------

@Composable
private fun OpenPhoneScreen(s: UiState, vm: MagicViewModel) {
    Row(Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 6.dp)) {
        // 윗판: 액정
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(30.dp, 10.dp, 10.dp, 30.dp))
                .background(bodyBrush)
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(70.dp, 7.dp).clip(RoundedCornerShape(4.dp)).background(M.Bezel))
            Spacer(Modifier.height(14.dp))
            Lcd(s, vm, Modifier.weight(1f).fillMaxWidth())
        }
        // 힌지
        Column(Modifier.width(18.dp).fillMaxHeight()) {
            Hinge(); Spacer(Modifier.weight(1f)); Hinge()
        }
        // 아랫판: 키패드
        Keypad(s, vm, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun Hinge() = Box(
    Modifier
        .fillMaxWidth()
        .height(120.dp)
        .clip(RoundedCornerShape(9.dp))
        .background(Brush.horizontalGradient(listOf(Color(0xFF1E2030), Color(0xFF55586F), Color(0xFF1E2030))))
)

/** 초록 액정: 상태 줄, 머리글, 대답(한 글자씩), 소프트키 */
@Composable
private fun Lcd(s: UiState, vm: MagicViewModel, modifier: Modifier) {
    val lit = s.phase != Phase.IDLE || s.answer.isEmpty() || !s.foldable
    val bg by animateColorAsState(if (lit) M.LcdOn else M.LcdOff, tween(250), label = "lcd")
    var now by remember { mutableStateOf(clock()) }
    LaunchedEffect(Unit) { while (true) { delay(10_000); now = clock() } }

    Box(modifier.clip(RoundedCornerShape(14.dp)).background(M.Bezel).padding(10.dp)) {
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SignalBars(s.phase == Phase.RECEIVING)
                Text(now, color = M.Ink, fontFamily = Pixel, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Battery()
            }
            Text(
                when (s.phase) {
                    Phase.RECEIVING -> "수신 중…"
                    Phase.TYPING, Phase.DONE -> "마법의 대답"
                    Phase.IDLE -> "질문 대기"
                },
                color = M.Ink, fontFamily = Pixel, fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp),
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val body = when (s.phase) {
                    Phase.IDLE -> if (s.foldable) "질문을 떠올리고\n접었다 펴세요" else "질문을 떠올리고\n물어보기를 누르세요"
                    else -> s.visibleText
                }
                val typing = s.phase == Phase.RECEIVING || s.phase == Phase.TYPING
                Row(
                    Modifier.semantics { contentDescription = if (s.phase == Phase.DONE) s.answer else body },
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        body, color = M.Ink, fontFamily = Pixel,
                        fontWeight = if (s.phase == Phase.IDLE) FontWeight.Normal else FontWeight.Bold,
                        fontSize = if (s.phase == Phase.IDLE) 18.sp else 30.sp,
                        lineHeight = if (s.phase == Phase.IDLE) 26.sp else 43.sp,
                        textAlign = TextAlign.Center,
                    )
                    if (typing) Text("_", color = M.Ink, fontFamily = Pixel, fontWeight = FontWeight.Bold, fontSize = 30.sp, modifier = Modifier.alpha(blink(600)))
                }
            }
            Box(Modifier.fillMaxWidth().height(2.dp).background(M.Ink))
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    "다시듣기", color = M.Ink, fontFamily = Pixel, fontSize = 12.sp,
                    modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = vm::replay),
                )
                Text(
                    if (s.voice) "소리 켬" else "소리 끔", color = M.Ink, fontFamily = Pixel, fontSize = 12.sp,
                    modifier = Modifier.clickable(role = Role.Button, onClick = vm::toggleVoice),
                )
            }
        }
    }
}

@Composable
private fun SignalBars(receiving: Boolean) {
    val t = rememberInfiniteTransition(label = "bars")
    Row(Modifier.height(12.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        listOf(4, 6, 9, 12).forEachIndexed { i, h ->
            val a by t.animateFloat(
                initialValue = 1f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    keyframes { durationMillis = 700; 1f at 0; 0.15f at 350 },
                    RepeatMode.Restart, StartOffset(i * 120),
                ),
                label = "bar$i",
            )
            Box(Modifier.size(3.dp, h.dp).alpha(if (receiving) a else 1f).background(M.Ink))
        }
    }
}

@Composable
private fun Battery() = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(20.dp, 10.dp).border(2.dp, M.Ink).padding(3.dp)) { Box(Modifier.fillMaxSize().background(M.Ink)) }
    Box(Modifier.size(2.dp, 4.dp).background(M.Ink))
}

/** 키패드: 다시듣기 · 방향키 · 소리, 통화 키, 숫자 키(장식), 안내 또는 물어보기 버튼 */
@Composable
private fun Keypad(s: UiState, vm: MagicViewModel, modifier: Modifier) {
    val lit = s.phase != Phase.IDLE
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp, 30.dp, 30.dp, 10.dp))
            .background(bodyBrush)
            .padding(horizontal = 26.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SoftKey("다시듣기", vm::replay, RoundedCornerShape(14.dp, 6.dp, 6.dp, 14.dp), Modifier.weight(1f))
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(60.dp).clip(CircleShape).background(M.Key).border(3.dp, Color(0xFF4C5068), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFF2A2C3E))) }
            }
            SoftKey(if (s.voice) "소리 켬" else "소리 끔", vm::toggleVoice, RoundedCornerShape(6.dp, 14.dp, 14.dp, 6.dp), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CallKey(Color(0xFF2F6B45), Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
            CallKey(Color(0xFF7A2E3A), Modifier.weight(1f))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("*", "0", "#")).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    row.forEach { k ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(if (lit) M.KeyLit else M.Key),
                            contentAlignment = Alignment.Center,
                        ) { Text(k, color = M.KeyText, fontFamily = Pixel, fontSize = 20.sp) }
                    }
                }
            }
        }
        if (s.foldable) {
            Text("다시 물어보려면 접었다 펴세요", color = M.Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        } else {
            AskButton(vm::ask, Modifier.fillMaxWidth().widthIn(min = 200.dp))
        }
    }
}

@Composable
private fun SoftKey(label: String, onClick: () -> Unit, shape: Shape, modifier: Modifier) = Box(
    modifier.height(48.dp).clip(shape).background(M.Key).clickable(role = Role.Button, onClick = onClick),
    contentAlignment = Alignment.Center,
) { Text(label, color = Color(0xFFDCD9EC), fontSize = 13.sp) }

@Composable
private fun CallKey(color: Color, modifier: Modifier) =
    Box(modifier.height(30.dp).clip(RoundedCornerShape(12.dp)).background(color))

/** 커서 깜빡임 (켜짐/꺼짐) */
@Composable
private fun blink(periodMs: Int = 1100): Float {
    val t = rememberInfiniteTransition(label = "blink")
    val a by t.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(keyframes { durationMillis = periodMs; 1f at 0; 0f at periodMs / 2 }),
        label = "blinkA",
    )
    return if (a > 0.5f) 1f else 0f
}

private fun clock(): String = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())

