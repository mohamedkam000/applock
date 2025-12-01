package com.app.lock.services

import android.content.*
import com.app.lock.core.utils.appLockRepository
import com.app.lock.data.repository.BackendImplementation

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val appLockRepository = context.appLockRepository()

            when (appLockRepository.getBackendImplementation()) {
                BackendImplementation.USAGE_STATS -> {
                    val serviceIntent = Intent(context, ExperimentalAppLockService::class.java)
                    context.startService(serviceIntent)
                }
            }
        }
    }
}