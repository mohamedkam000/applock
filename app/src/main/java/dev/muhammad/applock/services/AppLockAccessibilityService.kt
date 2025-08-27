package dev.muhammad.applock.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import dev.muhammad.applock.data.repository.AppLockRepository
import dev.muhammad.applock.data.repository.BackendImplementation
import dev.muhammad.applock.features.lockscreen.ui.PasswordOverlayActivity
import dev.muhammad.applock.services.AppLockManager.isServiceRunning

@SuppressLint("AccessibilityPolicy")
class AppLockAccessibilityService : AccessibilityService() {
    private lateinit var appLockRepository: AppLockRepository

    // The last app that was on screen
    private var lastForegroundPackage = ""

    // Keeps a record of last 3 events stored to prevents false lock screen due to recents bug
    private val lastEvents = mutableListOf<Pair<AccessibilityEvent, Long>>()

    // Package name of the system app that provides the recent apps functionality
    private var recentsPackage = ""

    enum class BiometricState {
        IDLE, AUTH_STARTED
    }

    companion object {
        private const val TAG = "AppLockAccessibility"
        var isServiceRunning = false
        private var instance: AppLockAccessibilityService? = null
        private const val APP_PACKAGE_PREFIX = "dev.muhammad.applock"

        fun getInstance(): AppLockAccessibilityService? = instance
    }

    @SuppressLint("PrivateApi")
    override fun onCreate() {
        super.onCreate()
        appLockRepository = AppLockRepository(applicationContext)
        isServiceRunning = true
        instance = this

        startServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.packageNames = null
        serviceInfo = info
        AppLockManager.resetRestartAttempts("AppLockAccessibilityService")
        appLockRepository.setActiveBackend(BackendImplementation.ACCESSIBILITY)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!::appLockRepository.isInitialized) return

        val currentBackend = appLockRepository.getBackendImplementation()
        val shouldHandleAppLocking = when (currentBackend) {
            BackendImplementation.ACCESSIBILITY -> {
                true
            }
            BackendImplementation.USAGE_STATS -> {
                val shouldFallback = !isServiceRunning(ExperimentalAppLockService::class.java)
                if (shouldFallback) {
                }
                shouldFallback
            }
        }

        if (!shouldHandleAppLocking) {
            return
        }

        if (isDeviceLocked()) {
            AppLockManager.appUnlockTimes.clear()
            AppLockManager.clearTemporarilyUnlockedApp()
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName.startsWith(APP_PACKAGE_PREFIX) || packageName in getKeyboardPackageNames()) {
            return
        }

        if (event.className in knownRecentsClasses) {
            recentsPackage = packageName
            return
        }

        val lockedApps = appLockRepository.getLockedApps()

        if (lastEvents.size >= 2) {
            val firstEvent = lastEvents.first()
            val lastEvent = lastEvents.last()
            val secondLastEvent = lastEvents[lastEvents.size - 2]

            if (secondLastEvent.first.packageName in lockedApps && lastEvent.first.packageName == "com.android.vending" && lastEvent.second - secondLastEvent.second < 5000) {
                return
            }

            if (secondLastEvent.first.packageName in getLauncherPackageNames(this) && AppLockManager.isAppTemporarilyUnlocked(
                    lastEvent.first.packageName.toString()
                ) && lastEvent.second - secondLastEvent.second < 5000
            ) {
                return
            }

            if (firstEvent.first.packageName in lockedApps && firstEvent.first.packageName == lastEvent.first.packageName && lastEvent.second - firstEvent.second < 5000) {
                return
            }
        }

        if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
            return
        }
        AppLockManager.clearTemporarilyUnlockedApp()
        lastForegroundPackage = packageName
        checkAndLockApp(packageName, event.eventTime)
    }

    private fun findNodeWithTextContaining(
        node: AccessibilityNodeInfo, text: String
    ): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeWithTextContaining(child, text)
            if (result != null) {
                return result
            }
        }

        return null
    }

    override fun onInterrupt() {
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        instance = null
        AppLockManager.startFallbackServices(this, AppLockAccessibilityService::class.java)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        instance = null
        AppLockManager.startFallbackServices(this, AppLockAccessibilityService::class.java)
    }

    fun checkAndLockApp(packageName: String, currentTime: Long) {
        if (AppLockManager.isLockScreenShown.get()) {
            return
        }

        if (shouldBeIgnored(packageName)) {
            return
        }
        if (AppLockManager.currentBiometricState == BiometricState.AUTH_STARTED) {
            return
        }
        if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
            return
        } else {
            AppLockManager.clearTemporarilyUnlockedApp()
        }

        val lockedApps = appLockRepository.getLockedApps()
        if (!lockedApps.contains(packageName)) {
            return
        }

        val unlockDuration = appLockRepository.getUnlockTimeDuration()
        val unlockTimestamp = AppLockManager.appUnlockTimes[packageName] ?: 0

        if (unlockDuration > 0 && unlockTimestamp > 0) {
            val elapsedMinutes = (currentTime - unlockTimestamp) / (60 * 1000)
            if (elapsedMinutes < unlockDuration) {
                if (!AppLockManager.isAppTemporarilyUnlocked(packageName)) {
                    AppLockManager.unlockApp(packageName)
                }
                return
            } else {
                AppLockManager.appUnlockTimes.remove(packageName)
                if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
                    AppLockManager.clearTemporarilyUnlockedApp()
                }
            }
        }

        val intent = Intent(this, PasswordOverlayActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("locked_package", packageName)
        }

        try {
            AppLockManager.isLockScreenShown.set(true)
            startActivity(intent)
        } catch (e: Exception) {
            AppLockManager.isLockScreenShown.set(false)
        }
    }

    private fun shouldBeIgnored(packageName: String): Boolean {
        return packageName in getLauncherPackageNames(this)
    }

    private fun getKeyboardPackageNames(): List<String> {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.map { it.packageName }
    }

    private fun getLauncherPackageNames(context: Context): List<String> {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo.map { it.activityInfo.packageName }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun startServices() {
        stopAllServices()

        when (appLockRepository.getBackendImplementation()) {
            BackendImplementation.USAGE_STATS -> {
                startService(Intent(this, ExperimentalAppLockService::class.java))
            }

            else -> { }
        }
    }

    private fun stopAllServices() {
        try {
            stopService(Intent(this, ExperimentalAppLockService::class.java))
        } catch (e: Exception) { }
    }
}