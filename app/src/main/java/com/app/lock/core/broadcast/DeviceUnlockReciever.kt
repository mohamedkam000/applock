package com.app.lock.core.broadcast

import android.content.*
import com.app.lock.services.AppLockManager

class DeviceUnlockReceiver(private val onDeviceUnlocked: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_USER_PRESENT -> {
                onDeviceUnlocked()
            }

            Intent.ACTION_SCREEN_OFF -> {
                AppLockManager.clearTemporarilyUnlockedApp()
                AppLockManager.appUnlockTimes.clear()
            }
        }
    }
}