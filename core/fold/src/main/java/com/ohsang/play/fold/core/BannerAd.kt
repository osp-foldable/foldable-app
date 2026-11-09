package com.ohsang.play.fold.core

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * 화면 하단 고정 적응형 배너.
 *
 * 커버 화면 ↔ 메인 화면 전환으로 폭이 바뀌면 그 폭에 맞는 크기로 새로 불러옴.
 * 광고 높이만큼 자리를 미리 잡아서, 광고가 늦게 떠도 화면이 밀리지 않게 함.
 */
@Composable
fun BottomBannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        val context = LocalContext.current
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        key(widthDp) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(adSize.height.dp),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(adSize)
                        this.adUnitId = adUnitId
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
