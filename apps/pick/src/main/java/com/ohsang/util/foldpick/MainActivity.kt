package com.ohsang.util.foldpick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.MobileAds
import com.ohsang.util.fold.core.postureFlow
import com.ohsang.util.foldpick.ui.AppRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: DrawViewModel by viewModels()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) vm.onScreenOff()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 광고 SDK 초기화는 메인 스레드를 막지 않게 백그라운드에서
        CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@MainActivity) {} }

        // 화면 꺼짐은 시스템 방송이라 NOT_EXPORTED로도 받을 수 있음 (Android 14+ 필수 플래그)
        ContextCompat.registerReceiver(
            this, screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        // 힌지 각도 센서가 있으면 접히는 기기로 봄 (Android 11+)
        val sensors = getSystemService(SensorManager::class.java)
        val hasHinge = Build.VERSION.SDK_INT >= 30 &&
            sensors?.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) != null
        vm.setFoldable(hasHinge)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postureFlow().collect { vm.onPosture(it) }
            }
        }

        setContent { AppRoot(vm) }
    }

    override fun onDestroy() {
        unregisterReceiver(screenOffReceiver)
        super.onDestroy()
    }
}
