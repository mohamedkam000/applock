package dev.muhammad.applock.services

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import dev.muhammad.applock.data.repository.AppLockRepository
import dev.muhammad.applock.data.repository.BackendImplementation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean


var knownRecentsClasses = setOf(
    "com.android.systemui.recents.RecentsActivity",
    "com.android.quickstep.RecentsActivity",
    "com.android.systemui.recents.RecentsView",
    "com.android.systemui.recents.RecentsPanelView"
)

var knownAccessibilitySettingsClasses = setOf(
    "com.android.settings.accessibility.AccessibilitySettings",
    "com.android.settings.accessibility.AccessibilityMenuActivity",
    "com.android.settings.accessibility.AccessibilityShortcutActivity",
    "com.android.settings.Settings\$AccessibilitySettingsActivity"
)

val excludedApps = setOf(
    "com.android.systemui",
    "com.android.intentresolver"
)

object AppLockManager {
    var temporarilyUnlockedApp: String = ""
    val appUnlockTimes = ConcurrentHashMap<String, Long>()
    var currentBiometricState = AppLockAccessibilityService.BiometricState.IDLE
    val isLockScreenShown = AtomicBoolean(false)

    private val serviceRestartAttempts = ConcurrentHashMap<String, Int>()
    private val lastRestartTime = ConcurrentHashMap<String, Long>()
    private const val MAX_RESTART_ATTEMPTS = 3
    private const val RESTART_COOLDOWN_MS = 30000L // 30 seconds
    private const val SERVICE_RESTART_INTERVAL_MS = 5000L // 5 seconds between attempts


    fun unlockApp(packageName: String) {
        temporarilyUnlockedApp = packageName
        appUnlockTimes[packageName] = System.currentTimeMillis()
    }

    fun temporarilyUnlockAppWithBiometrics(packageName: String) {
        unlockApp(packageName)
        reportBiometricAuthFinished()
    }

    fun reportBiometricAuthStarted() {
        currentBiometricState = AppLockAccessibilityService.BiometricState.AUTH_STARTED
    }

    fun reportBiometricAuthFinished() {
        currentBiometricState = AppLockAccessibilityService.BiometricState.IDLE
    }

    fun isAppTemporarilyUnlocked(packageName: String): Boolean {
        return temporarilyUnlockedApp == packageName
    }

    fun clearTemporarilyUnlockedApp() {
        temporarilyUnlockedApp = ""
    }

    fun startFallbackServices(context: Context, failedService: Class<*>) {
        val serviceName = failedService.simpleName

        if (!shouldAttemptRestart(serviceName)) {
            return
        }

        val appLockRepository = AppLockRepository(context)
        val fallbackBackend = appLockRepository.getFallbackBackend()

        when (failedService) {
            AppLockAccessibilityService::class.java -> {
                startServiceByBackend(context, fallbackBackend)
            }

            ExperimentalAppLockService::class.java -> {
                if (AppLockAccessibilityService.isServiceRunning) {
                    return
                }
            }
        }

        recordRestartAttempt(serviceName)
    }

    private fun shouldAttemptRestart(serviceName: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val attempts = serviceRestartAttempts[serviceName] ?: 0
        val lastRestart = lastRestartTime[serviceName] ?: 0

        if (currentTime - lastRestart < SERVICE_RESTART_INTERVAL_MS) {
            return false
        }

        if (attempts >= MAX_RESTART_ATTEMPTS) {
            if (currentTime - lastRestart > RESTART_COOLDOWN_MS) {
                serviceRestartAttempts[serviceName] = 0
                return true
            }
            return false
        }

        return true
    }

    private fun recordRestartAttempt(serviceName: String) {
        val currentTime = System.currentTimeMillis()
        val currentAttempts = serviceRestartAttempts[serviceName] ?: 0
        serviceRestartAttempts[serviceName] = currentAttempts + 1
        lastRestartTime[serviceName] = currentTime
    }

    private fun startServiceByBackend(context: Context, backend: BackendImplementation) {
        try {
            stopAllServices(context)

            when (backend) {
                BackendImplementation.USAGE_STATS -> {
                    context.startService(Intent(context, ExperimentalAppLockService::class.java))
                }

                BackendImplementation.ACCESSIBILITY -> {
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun stopAllServices(context: Context) {
        try {
            context.stopService(Intent(context, ExperimentalAppLockService::class.java))
        } catch (e: Exception) {
        }
    }

    fun resetRestartAttempts(serviceName: String) {
        serviceRestartAttempts.remove(serviceName)
        lastRestartTime.remove(serviceName)
    }


    fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.Companion.MAX_VALUE)) {
            if (serviceClass.getName() == service.service.className) {
                return true
            }
        }
        return false
    }
}

fun Context.isDeviceLocked(): Boolean {
    val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}
