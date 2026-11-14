package com.ohsang.util.foldmagic

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
import com.ohsang.util.foldmagic.ui.AppRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: MagicViewModel by viewModels()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) vm.onScreenOff()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@MainActivity) {} }

        ContextCompat.registerReceiver(
            this, screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

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

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        unregisterReceiver(screenOffReceiver)
        super.onDestroy()
    }
}
