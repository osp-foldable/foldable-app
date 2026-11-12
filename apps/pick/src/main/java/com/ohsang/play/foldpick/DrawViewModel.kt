package com.ohsang.play.foldpick

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.ohsang.play.fold.core.Posture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

const val MIN_MAX = 2
const val MAX_MAX = 999
const val DEFAULT_MAX = 10
val PRESETS = listOf(10, 20, 50, 100)

data class UiState(
    /** 입력칸에 보이는 글자 (입력 중엔 빈칸일 수도 있음) */
    val maxText: String = DEFAULT_MAX.toString(),
    /** 실제로 적용된 최대 숫자 */
    val max: Int = DEFAULT_MAX,
    val dedupe: Boolean = true,
    /** 커버 화면에서 설정을 마치고 시작했는지 (false면 설정 화면) */
    val started: Boolean = false,
    /** 뽑힌 순서대로 */
    val history: List<Int> = emptyList(),
    /** 지금 보여줄 숫자. null이면 아직 안 뽑음 */
    val current: Int? = null,
    /** 숫자 튀어나오기 애니메이션·진동을 아직 안 보여줬으면 true */
    val pendingPop: Boolean = false,
    /** 펼친 화면에서 설정을 열어둔 상태 */
    val showSettings: Boolean = false,
    /** 중복 제외 모드에서 다 뽑은 뒤 한 번 더 펼쳤을 때 완료 화면 */
    val showDone: Boolean = false,
    val posture: Posture = Posture.UNKNOWN,
    /**
     * 접히는 기기인지. 힌지 센서가 있거나 접힘↔펼침 전환이 한 번이라도 감지되면 true.
     * 일반 폰·태블릿(심사관 기기 포함)에선 false → 버튼으로 뽑는 방식으로 동작
     */
    val foldable: Boolean = false,
) {
    val exhausted: Boolean get() = dedupe && history.size >= max
    val remaining: Int get() = (max - history.size).coerceAtLeast(0)
}

class DrawViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        prefs.getInt(KEY_MAX, DEFAULT_MAX).let { savedMax ->
            UiState(
                maxText = savedMax.toString(),
                max = savedMax,
                dedupe = prefs.getBoolean(KEY_DEDUPE, true),
            )
        }
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** 화면이 꺼진 뒤 다시 돌아오면 '접었다 폈다'로 간주하기 위한 표시 */
    private var screenWentOff = false

    // ---- 접힘 상태 ----

    fun setFoldable(value: Boolean) = _state.update { it.copy(foldable = it.foldable || value) }

    /** 폴더블이 아닌 기기에서 '뽑기' 버튼. 다 뽑았으면 처음부터 다시 */
    fun drawByButton() {
        if (_state.value.exhausted) reset() else draw()
    }

    fun onPosture(next: Posture) {
        val prev = _state.value.posture
        val switched = prev != Posture.UNKNOWN && prev != next
        _state.update { it.copy(posture = next, foldable = it.foldable || switched) }

        val unfoldedNow = next == Posture.UNFOLDED
        val wasFolded = prev == Posture.FOLDED ||
            (prev == Posture.UNFOLDED && screenWentOff && _state.value.foldable)
        screenWentOff = false

        if (unfoldedNow && wasFolded) draw()
    }

    /**
     * 메인 화면에서 접으면 기본 설정상 화면이 꺼지고 앱이 멈춰서
     * '접힘' 상태가 들어오지 않을 수 있음. 그래서 화면 꺼짐을 접힘 대신으로 기록.
     * (전원 버튼으로 껐다 켜도 새 숫자가 나오는 점은 실제 기기에서 확인 필요)
     */
    fun onScreenOff() {
        if (_state.value.posture == Posture.UNFOLDED) screenWentOff = true
    }

    // ---- 뽑기 ----

    fun draw() {
        commitMax()
        val s = _state.value
        if (s.exhausted) {
            _state.update { it.copy(showDone = true, showSettings = false) }
            return
        }
        val pool = if (s.dedupe) (1..s.max).filterNot { it in s.history } else (1..s.max).toList()
        val value = pool[Random.nextInt(pool.size)]
        val history = (s.history + value).takeLast(HISTORY_LIMIT)
        _state.update {
            it.copy(
                current = value,
                history = history,
                pendingPop = true,
                started = true,
                showSettings = false,
                showDone = false,
            )
        }
    }

    fun onPopShown() = _state.update { it.copy(pendingPop = false) }

    fun reset() = _state.update {
        it.copy(history = emptyList(), current = null, pendingPop = false, showDone = false)
    }

    // ---- 설정 ----

    fun onMaxTextChange(text: String) {
        val digits = text.filter { it.isDigit() }.take(3)
        _state.update { it.copy(maxText = digits) }
    }

    /** 입력을 마쳤을 때 범위(2~999)로 맞추고, 값이 바뀌었으면 기록 초기화 */
    fun commitMax() {
        val s = _state.value
        val value = (s.maxText.toIntOrNull() ?: s.max).coerceIn(MIN_MAX, MAX_MAX)
        if (value != s.max) {
            prefs.edit().putInt(KEY_MAX, value).apply()
            _state.update { it.copy(max = value, maxText = value.toString()) }
            reset()
        } else if (s.maxText != value.toString()) {
            _state.update { it.copy(maxText = value.toString()) }
        }
    }

    fun pickPreset(value: Int) {
        _state.update { it.copy(maxText = value.toString()) }
        commitMax()
    }

    fun toggleDedupe() {
        val next = !_state.value.dedupe
        prefs.edit().putBoolean(KEY_DEDUPE, next).apply()
        _state.update { it.copy(dedupe = next) }
        reset()
    }

    /** 커버 화면 '시작하기' */
    fun start() {
        commitMax()
        _state.update { it.copy(started = true) }
    }

    fun openSettings() {
        if (_state.value.posture == Posture.UNFOLDED) {
            _state.update { it.copy(showSettings = true) }
        } else {
            _state.update { it.copy(started = false) }
        }
    }

    fun closeSettings() {
        commitMax()
        _state.update { it.copy(showSettings = false) }
    }

    /** 완료 화면의 '최대 숫자 바꾸기' */
    fun changeMaxFromDone() {
        reset()
        _state.update { it.copy(showSettings = true) }
    }

    private companion object {
        const val KEY_MAX = "max"
        const val KEY_DEDUPE = "dedupe"
        /** 중복 허용 모드에서 기록이 끝없이 쌓이지 않게 */
        const val HISTORY_LIMIT = 999
    }
}
