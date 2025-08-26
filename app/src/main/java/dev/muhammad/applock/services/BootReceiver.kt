package dev.muhammad.applock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.muhammad.applock.core.utils.appLockRepository
import dev.muhammad.applock.data.repository.BackendImplementation

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val appLockRepository = context.appLockRepository()

            when (appLockRepository.getBackendImplementation()) {
                BackendImplementation.ACCESSIBILITY -> {
                    val serviceIntent = Intent(context, AppLockAccessibilityService::class.java)
                    context.startService(serviceIntent)
                }

                BackendImplementation.USAGE_STATS -> {
                    val serviceIntent = Intent(context, ExperimentalAppLockService::class.java)
                    context.startService(serviceIntent)
                }
            }
        }
    }
}