package com.app.lock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.lock.core.utils.appLockRepository
import com.app.lock.data.repository.BackendImplementation

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val appLockRepository = context.appLockRepository()

            if (appLockRepository.isAntiUninstallEnabled()) {
                val serviceIntent = Intent(context, AppLockAccessibilityService::class.java)
                context.startService(serviceIntent)
            }

            when (appLockRepository.getBackendImplementation()) {
                BackendImplementation.SHIZUKU -> {
                    val serviceIntent = Intent(context, ShizukuAppLockService::class.java)
                    context.startService(serviceIntent)
                }

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
