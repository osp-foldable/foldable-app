package com.ohsang.play.foldnumber

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ohsang.play.fold.core.postureFlow
import com.ohsang.play.foldnumber.ui.AppRoot
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

        // 화면 꺼짐은 시스템 방송이라 NOT_EXPORTED로도 받을 수 있음 (Android 14+ 필수 플래그)
        ContextCompat.registerReceiver(
            this, screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

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
