package com.ohsang.play.foldnumber.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ohsang.play.foldnumber.DrawViewModel
import com.ohsang.play.foldnumber.MAX_MAX
import com.ohsang.play.foldnumber.MIN_MAX
import com.ohsang.play.fold.core.C
import com.ohsang.play.fold.core.NumberFont
import com.ohsang.play.fold.core.Posture
import com.ohsang.play.foldnumber.R
import com.ohsang.play.foldnumber.UiState

@Composable
fun AppRoot(vm: DrawViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()

    BackHandler(enabled = s.posture == Posture.UNFOLDED && s.showSettings && s.current != null) {
        vm.closeSettings()
    }

    Box(Modifier.fillMaxSize().background(C.Bg).safeDrawingPadding()) {
        if (s.posture == Posture.UNFOLDED) {
            when {
                s.showDone -> DoneScreen(s, vm)
                s.showSettings || s.current == null -> GuideScreen(s, vm)
                else -> RevealScreen(s, vm)
            }
        } else {
            if (!s.started) SetupScreen(s, vm) else ReadyScreen(s, vm)
        }
    }
}

// ① 커버 · 최대 숫자 설정
@Composable
private fun SetupScreen(s: UiState, vm: DrawViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
    ) {
        AppTitle()
        Text(
            "몇 번까지\n뽑을까요?", color = C.Text, fontSize = 28.sp, lineHeight = 36.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
        )
        Text("1부터 정한 숫자 사이에서 하나씩 뽑아요", color = C.Muted, fontSize = 15.sp)

        Column(
            Modifier
                .padding(top = 36.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(C.Surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("최대 숫자", color = C.Muted, fontSize = 14.sp)
            MaxNumberField(s, vm::onMaxTextChange, vm::commitMax)
            Text(
                "범위 1 ~ ${s.maxText.toIntOrNull()?.coerceIn(MIN_MAX, MAX_MAX) ?: s.max}",
                color = C.Muted, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            PresetRow(s, vm::pickPreset)
        }

        VSpace(16.dp)
        DedupeRow(s.dedupe, vm::toggleDedupe)

        VSpace(32.dp)
        PrimaryButton("시작하기", vm::start, Modifier.fillMaxWidth())
        Text(
            "2부터 999까지 입력할 수 있어요", color = C.Muted, fontSize = 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
    }
}

// ② 커버 · 펼치기 대기
@Composable
private fun ReadyScreen(s: UiState, vm: DrawViewModel) {
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppTitle()
                Text(
                    "1 ~ ${s.max} · 중복 제외 " + if (s.dedupe) "켬" else "끔",
                    color = C.Muted, fontSize = 13.sp,
                )
            }
            SettingsIconButton(vm::openSettings)
        }

        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            UnfoldIllustration()
            VSpace(28.dp)
            Text(
                if (s.exhausted) "모든 숫자를\n다 뽑았어요" else "폰을 펼치면\n숫자가 나와요",
                color = C.Text, fontSize = 26.sp, lineHeight = 35.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            )
            VSpace(10.dp)
            Text(
                if (s.exhausted) "펼쳐서 결과를 확인하세요" else "펼치는 순간 진동과 함께 공개돼요",
                color = C.Muted, fontSize = 15.sp,
            )
        }

        if (s.current != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(C.Surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Stat("마지막 숫자", s.current.toString(), accent = true, modifier = Modifier.weight(1f))
                if (s.dedupe) {
                    Stat("남은 숫자", "${s.remaining} / ${s.max}", modifier = Modifier.weight(1f))
                } else {
                    Stat("뽑은 횟수", "${s.history.size}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = C.Muted, fontSize = 13.sp)
        Text(
            value, color = if (accent) C.Accent else C.Text, fontSize = 28.sp,
            fontWeight = FontWeight.Bold, fontFamily = NumberFont,
        )
    }
}

// ③ 펼침 · 숫자 공개
@Composable
private fun RevealScreen(s: UiState, vm: DrawViewModel) {
    val current = s.current ?: return
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTitle()
                RangeChip(s)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DedupeChip(s.dedupe, vm::toggleDedupe)
                SettingsIconButton(vm::openSettings)
            }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("${s.history.size}번째 숫자", color = C.Muted, fontSize = 16.sp)
            PopNumber(current, s.pendingPop, vm::onPopShown)
            Text("다시 뽑으려면 접었다 펴세요", color = C.Muted, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp))
        }

        HistoryBar(s, onReset = vm::reset)
    }
}

@Composable
private fun HistoryBar(s: UiState, onReset: () -> Unit) {
    val scroll = rememberScrollState()
    LaunchedEffect(s.history.size) { scroll.animateScrollTo(scroll.maxValue) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(C.Surface)
            .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("나온 숫자", color = C.Muted, fontSize = 14.sp)
        Row(
            Modifier.weight(1f).horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val last = s.history.lastIndex
            s.history.forEachIndexed { i, v -> NumberTile(v, highlight = i == last) }
        }
        if (s.dedupe) Text("남은 ${s.remaining}개", color = C.Muted, fontSize = 13.sp)
        SecondaryButton("초기화", onReset, iconRes = R.drawable.ic_reset)
    }
}

// ④ 펼친 상태로 실행했거나, 펼친 화면에서 설정을 열었을 때
@Composable
private fun GuideScreen(s: UiState, vm: DrawViewModel) {
    val canClose = s.showSettings && s.current != null
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 32.dp, vertical = 24.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { AppTitle() }
            if (canClose) SecondaryButton("닫기", vm::closeSettings)
        }
        VSpace(20.dp)
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(C.Surface)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("최대 숫자", color = C.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("1부터 정한 숫자 사이에서 뽑아요 (2~999)", color = C.Muted, fontSize = 14.sp)
                }
                MaxNumberField(s, vm::onMaxTextChange, vm::commitMax, height = 88.dp, fontSize = 60.sp)
                PresetRow(s, vm::pickPreset)
                DedupeRow(s.dedupe, vm::toggleDedupe, background = C.Surface2)
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .dashedBorder(C.Border, 24.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                UnfoldIllustration()
                VSpace(24.dp)
                Text(
                    if (canClose) "설정을 바꾸면\n기록이 초기화돼요" else "한 번 접었다 펴면\n첫 숫자가 나와요",
                    color = C.Text, fontSize = 26.sp, lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                )
                VSpace(10.dp)
                Text("설정은 언제든 바꿀 수 있어요", color = C.Muted, fontSize = 15.sp)
            }
        }
    }
}

// ⑤ 중복 제외 · 모두 뽑음
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoneScreen(s: UiState, vm: DrawViewModel) {
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTitle()
            RangeChip(s)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            VSpace(24.dp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${s.history.size}", color = C.Accent, fontSize = 64.sp, fontWeight = FontWeight.Bold, fontFamily = NumberFont)
                Text(" / ${s.max}", color = C.Muted, fontSize = 64.sp, fontWeight = FontWeight.Bold, fontFamily = NumberFont)
            }
            Text("모든 숫자를 다 뽑았어요", color = C.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text("뽑힌 순서대로 보여드려요", color = C.Muted, fontSize = 15.sp, modifier = Modifier.padding(top = 10.dp, bottom = 28.dp))
            FlowRow(
                Modifier.widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val last = s.history.lastIndex
                s.history.forEachIndexed { i, v -> NumberTile(v, highlight = i == last, size = 48.dp) }
            }
            VSpace(24.dp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
            SecondaryButton("최대 숫자 바꾸기", vm::changeMaxFromDone, Modifier.widthIn(min = 220.dp).heightIn(min = 56.dp))
            PrimaryButton("처음부터 다시", vm::reset, Modifier.widthIn(min = 220.dp))
        }
    }
}
