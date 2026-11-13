package com.ohsang.util.fold.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** 짧은 진동 한 번 (숫자 공개 같은 순간용). AndroidManifest에 VIBRATE 권한 필요 */
fun Context.vibrateShort() {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
    }
    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
}
