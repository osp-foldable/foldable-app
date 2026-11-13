package com.ohsang.util.fold.core

import android.app.Activity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class Posture { UNKNOWN, FOLDED, UNFOLDED }

/**
 * 현재 화면이 접힌 상태(커버 화면)인지 펼친 상태(메인 화면)인지 흘려주는 Flow.
 *
 * - 메인 화면에서는 창이 힌지를 가로지르므로 FoldingFeature가 들어옴 → 펼침
 * - 커버 화면에서는 FoldingFeature가 없음 → 접힘
 * - 예외 대비: FoldingFeature가 안 와도 짧은 변이 600dp 이상이면 펼침으로 봄
 */
fun Activity.postureFlow(): Flow<Posture> =
    WindowInfoTracker.getOrCreate(this)
        .windowLayoutInfo(this)
        .map { info ->
            val fold = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
            val bounds = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this).bounds
            val density = resources.displayMetrics.density
            val shortSideDp = minOf(bounds.width(), bounds.height()) / density
            if (fold != null || shortSideDp >= 600f) Posture.UNFOLDED else Posture.FOLDED
        }
        .distinctUntilChanged()
