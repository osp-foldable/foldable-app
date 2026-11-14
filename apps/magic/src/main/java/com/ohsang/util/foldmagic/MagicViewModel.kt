package com.ohsang.util.foldmagic

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ohsang.util.fold.core.Posture
import com.ohsang.util.fold.core.vibrateShort
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

enum class Phase { IDLE, RECEIVING, TYPING, DONE }

data class UiState(
    val posture: Posture = Posture.UNKNOWN,
    /** 힌지 센서가 있거나 접힘↔펼침 전환이 감지되면 true. 아니면 '물어보기' 버튼으로 동작 */
    val foldable: Boolean = false,
    /** 목소리로 읽어주기 */
    val voice: Boolean = true,
    /** 한국어 음성 엔진 사용 가능 여부. null = 확인 중 */
    val voiceReady: Boolean? = null,
    val phase: Phase = Phase.IDLE,
    /** 지금(또는 마지막) 대답 */
    val answer: String = "",
    /** 액정에 찍힌 글자 수 */
    val shown: Int = 0,
) {
    val visibleText: String
        get() = when (phase) {
            Phase.IDLE, Phase.RECEIVING -> ""
            Phase.TYPING -> answer.take(shown)
            Phase.DONE -> answer
        }
}

class MagicViewModel(app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {

    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(UiState(voice = prefs.getBoolean(KEY_VOICE, true)))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val tts = TextToSpeech(app, this)
    private var job: Job? = null
    private var screenWentOff = false

    override fun onInit(status: Int) {
        val ok = status == TextToSpeech.SUCCESS &&
            tts.setLanguage(Locale.KOREAN) >= TextToSpeech.LANG_AVAILABLE
        if (ok) {
            // 살짝 느리고 낮게: 옛날 기계 같은 느낌
            tts.setSpeechRate(0.95f)
            tts.setPitch(0.9f)
        }
        _state.update { it.copy(voiceReady = ok) }
    }

    // ---- 접힘 상태 ----

    fun setFoldable(value: Boolean) = _state.update { it.copy(foldable = it.foldable || value) }

    fun onPosture(next: Posture) {
        val prev = _state.value.posture
        val switched = prev != Posture.UNKNOWN && prev != next
        _state.update { it.copy(posture = next, foldable = it.foldable || switched) }

        val wasFolded = prev == Posture.FOLDED ||
            (prev == Posture.UNFOLDED && screenWentOff && _state.value.foldable)
        screenWentOff = false

        when {
            next == Posture.FOLDED && prev == Posture.UNFOLDED -> quiet()
            next == Posture.UNFOLDED && wasFolded -> ask()
        }
    }

    /** 메인 화면에서 접으면 화면이 꺼져서 '접힘'이 안 들어올 수 있어, 화면 꺼짐을 접힘 대신으로 기록 */
    fun onScreenOff() {
        if (_state.value.posture == Posture.UNFOLDED) screenWentOff = true
    }

    /** 앱이 가려지면 말하기를 멈추고 대답은 끝까지 보여줌 */
    fun onStop() {
        job?.cancel()
        tts.stop()
        _state.update { if (it.phase == Phase.IDLE) it else it.copy(phase = Phase.DONE, shown = it.answer.length) }
    }

    // ---- 대답 ----

    /** 수신 중(0.9초) → 진동 → 한 글자씩 찍기 + 목소리 */
    fun ask() {
        job?.cancel()
        tts.stop()
        val last = _state.value.answer
        val pool = Answers.all.filter { it != last }
        val answer = pool[Random.nextInt(pool.size)]
        _state.update { it.copy(phase = Phase.RECEIVING, answer = answer, shown = 0) }

        job = viewModelScope.launch {
            delay(RECEIVING_MS)
            getApplication<Application>().vibrateShort()
            _state.update { it.copy(phase = Phase.TYPING, shown = 0) }
            speak(answer)
            for (i in 1..answer.length) {
                delay(TYPE_MS)
                _state.update { it.copy(shown = i) }
            }
            _state.update { it.copy(phase = Phase.DONE) }
        }
    }

    fun replay() {
        val s = _state.value
        if (s.phase == Phase.TYPING || s.phase == Phase.DONE) speak(s.answer, force = true)
    }

    fun toggleVoice() {
        val next = !_state.value.voice
        prefs.edit().putBoolean(KEY_VOICE, next).apply()
        _state.update { it.copy(voice = next) }
        if (!next) tts.stop()
    }

    /** 접었을 때: 말 멈추고 대기 화면으로 (마지막 대답은 기억) */
    private fun quiet() {
        job?.cancel()
        tts.stop()
        _state.update { it.copy(phase = Phase.IDLE) }
    }

    private fun speak(text: String, force: Boolean = false) {
        val s = _state.value
        if (s.voiceReady != true) return
        if (!s.voice && !force) return
        tts.speak(text.replace('\n', ' '), TextToSpeech.QUEUE_FLUSH, null, "answer")
    }

    override fun onCleared() {
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val KEY_VOICE = "voice"
        const val RECEIVING_MS = 900L
        const val TYPE_MS = 90L
    }
}
