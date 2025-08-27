package dev.muhammad.applock.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.content.edit
import dev.muhammad.applock.core.utils.hasUsagePermission
import dev.muhammad.applock.core.utils.isAccessibilityServiceEnabled
import dev.muhammad.applock.services.AppLockAccessibilityService
import dev.muhammad.applock.services.ExperimentalAppLockService

class AppLockRepository(context: Context) {
    private val appLockPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME_APP_LOCK, Context.MODE_PRIVATE)

    private val settingsPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME_SETTINGS, Context.MODE_PRIVATE)

    private var activeBackend: BackendImplementation? = null

    fun getLockedApps(): Set<String> {
        return appLockPrefs.getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()
    }

    fun addLockedApp(packageName: String) {
        val currentApps = getLockedApps().toMutableSet()
        currentApps.add(packageName)
        appLockPrefs.edit { putStringSet(KEY_LOCKED_APPS, currentApps) }
    }

    fun removeLockedApp(packageName: String) {
        val currentApps = getLockedApps().toMutableSet()
        currentApps.remove(packageName)
        appLockPrefs.edit { putStringSet(KEY_LOCKED_APPS, currentApps) }
    }

    fun getPassword(): String? {
        return appLockPrefs.getString(KEY_PASSWORD, null)
    }

    fun setPassword(password: String) {
        appLockPrefs.edit { putString(KEY_PASSWORD, password) }
    }

    fun validatePassword(inputPassword: String): Boolean {
        val storedPassword = getPassword()
        return storedPassword != null && inputPassword == storedPassword
    }

    fun setUnlockTimeDuration(minutes: Int) {
        settingsPrefs.edit { putInt(KEY_UNLOCK_TIME_DURATION, minutes) }
    }

    fun getUnlockTimeDuration(): Int {
        return settingsPrefs.getInt(KEY_UNLOCK_TIME_DURATION, 0)
    }

    fun setBiometricAuthEnabled(enabled: Boolean) {
        settingsPrefs.edit { putBoolean(KEY_BIOMETRIC_AUTH_ENABLED, enabled) }
    }

    fun isBiometricAuthEnabled(): Boolean {
        return settingsPrefs.getBoolean(KEY_BIOMETRIC_AUTH_ENABLED, true)
    }

    fun setPromptForBiometricAuth(enabled: Boolean) {
        settingsPrefs.edit { putBoolean(KEY_PROMPT_FOR_BIOMETRIC_AUTH, enabled) }
    }

    fun shouldPromptForBiometricAuth(): Boolean {
        return isBiometricAuthEnabled() && settingsPrefs.getBoolean(
            KEY_PROMPT_FOR_BIOMETRIC_AUTH,
            true
        )
    }

    fun setBackendImplementation(backend: BackendImplementation) {
        settingsPrefs.edit { putString(KEY_BACKEND_IMPLEMENTATION, backend.name) }
    }

    fun getBackendImplementation(): BackendImplementation {
        val backend = settingsPrefs.getString(
            KEY_BACKEND_IMPLEMENTATION,
            BackendImplementation.ACCESSIBILITY.name
        )
        return try {
            BackendImplementation.valueOf(backend ?: BackendImplementation.ACCESSIBILITY.name)
        } catch (e: IllegalArgumentException) {
            BackendImplementation.ACCESSIBILITY
        }
    }

    fun setFallbackBackend(fallback: BackendImplementation) {
        settingsPrefs.edit { putString(KEY_FALLBACK_BACKEND, fallback.name) }
    }

    fun getFallbackBackend(): BackendImplementation {
        val fallback =
            settingsPrefs.getString(KEY_FALLBACK_BACKEND, BackendImplementation.ACCESSIBILITY.name)
        return try {
            BackendImplementation.valueOf(fallback ?: BackendImplementation.ACCESSIBILITY.name)
        } catch (e: IllegalArgumentException) {
            BackendImplementation.ACCESSIBILITY
        }
    }

    fun setActiveBackend(backend: BackendImplementation) {
        activeBackend = backend
    }

    fun getActiveBackend(): BackendImplementation? {
        return activeBackend
    }

    fun isBackendAvailable(backend: BackendImplementation, context: Context): Boolean {
        return when (backend) {
            BackendImplementation.ACCESSIBILITY -> context.isAccessibilityServiceEnabled()
            BackendImplementation.USAGE_STATS -> context.hasUsagePermission()
        }
    }

    fun validateAndSwitchBackend(context: Context): BackendImplementation {
        val currentActive = getActiveBackend()
        val primary = getBackendImplementation()
        val fallback = getFallbackBackend()

        if (isBackendAvailable(currentActive!!, context)) {
            return currentActive
        }

        val newBackend = when {
            currentActive != primary && isBackendAvailable(primary, context) -> primary
            primary != fallback && isBackendAvailable(fallback, context) -> fallback
            else -> primary
        }

        if (newBackend != currentActive) {
            setActiveBackend(newBackend)
            startBackendMonitoring(context)
        }

        return newBackend
    }

    private fun startBackendMonitoring(context: Context) {
        try {
            val intent = Intent(
                context,
                Class.forName("dev.muhammad.applock.core.monitoring.BackendMonitoringService")
            )
            context.startService(intent)
        } catch (e: Exception) { }
    }

    companion object {
        private const val PREFS_NAME_APP_LOCK = "app_lock_prefs"
        private const val PREFS_NAME_SETTINGS = "app_lock_settings"
        private const val KEY_LOCKED_APPS = "locked_apps"
        private const val KEY_PASSWORD = "password"
        private const val KEY_BIOMETRIC_AUTH_ENABLED = "use_biometric_auth"
        private const val KEY_PROMPT_FOR_BIOMETRIC_AUTH = "biometric_auth"
        private const val KEY_UNLOCK_TIME_DURATION = "unlock_time_duration"
        private const val KEY_BACKEND_IMPLEMENTATION = "backend_implementation"
        private const val KEY_FALLBACK_BACKEND = "fallback_backend"
        private const val KEY_ACTIVE_BACKEND = "active_backend"

        fun shouldStartService(rep: AppLockRepository, serviceClass: Class<*>): Boolean {
            rep.getActiveBackend().let { activeBackend ->
                if (activeBackend == rep.getBackendImplementation()) {
                    return false
                }
                val backendClass = when (serviceClass) {
                    AppLockAccessibilityService::class.java -> BackendImplementation.ACCESSIBILITY
                    ExperimentalAppLockService::class.java -> BackendImplementation.USAGE_STATS
                    else -> return false
                }
                if (backendClass == rep.getBackendImplementation()) {
                    return true
                }
                rep.getFallbackBackend().let { fallbackBackend ->
                    if (activeBackend == rep.getBackendImplementation()) {
                        return false
                    }
                    return backendClass == fallbackBackend
                }
            }
        }
    }
}

enum class BackendImplementation {
    ACCESSIBILITY,
    USAGE_STATS
}