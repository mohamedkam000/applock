package dev.muhammad.applock.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dev.muhammad.applock.R
import dev.muhammad.applock.core.broadcast.DeviceAdmin
import dev.muhammad.applock.core.utils.appLockRepository
import dev.muhammad.applock.data.repository.AppLockRepository
import dev.muhammad.applock.data.repository.AppLockRepository.Companion.shouldStartService
import dev.muhammad.applock.data.repository.BackendImplementation
import dev.muhammad.applock.features.lockscreen.ui.PasswordOverlayActivity
import dev.muhammad.applock.shizuku.ShizukuActivityManager
import rikka.shizuku.Shizuku

class ShizukuAppLockService : Service() {
    private lateinit var appLockRepository: AppLockRepository
    private var shizukuActivityManager: ShizukuActivityManager? = null

    companion object {
        private const val TAG = "ShizukuAppLockService"
        private const val NOTIFICATION_ID = 112
        private const val CHANNEL_ID = "ShizukuAppLockServiceChannel"
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()

        appLockRepository = appLockRepository()
        if (!shouldStartService(appLockRepository, this::class.java)) {
            Log.d(TAG, "Service not needed, stopping service")
            stopSelf()
            return
        }
        Log.d(TAG, "ShizukuAppLockService created")

        AppLockManager.isLockScreenShown.set(false) // Reset lock screen state on service start

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Shizuku permission not granted, stopping service")
            stopSelf()
            return
        }

        shizukuActivityManager =
            ShizukuActivityManager(this, appLockRepository) { packageName, className, timeMillis ->
                if (packageName != AppLockManager.temporarilyUnlockedApp) {
                    AppLockManager.temporarilyUnlockedApp = ""
                }

                Log.d(TAG, "Checking app lock for: $packageName at $timeMillis")

                checkAndLockApp(packageName, timeMillis)
            }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "ShizukuAppLockService started")
        AppLockManager.resetRestartAttempts("ShizukuAppLockService")
        appLockRepository.setActiveBackend(BackendImplementation.SHIZUKU)

        // Stop other services to ensure only one runs at a time
        stopOtherServices()

        val dpm =
            getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(this, DeviceAdmin::class.java)
        val hasDeviceAdmin = dpm.isAdminActive(component)

        createNotificationChannel()
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                if (hasDeviceAdmin) ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED else ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val shizukuStarted = shizukuActivityManager?.start()
        if (shizukuStarted == false) {
            Log.e(TAG, "Shizuku failed to start, triggering fallback")
            AppLockManager.startFallbackServices(this, ShizukuAppLockService::class.java)
            stopSelf()
            return START_NOT_STICKY
        }

        isServiceRunning = true

        return START_STICKY
    }

    private fun stopOtherServices() {
        Log.d(TAG, "Stopping other app lock services")

        try {
            stopService(Intent(this, ExperimentalAppLockService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping other services", e)
        }
    }

    override fun onDestroy() {
        shizukuActivityManager?.stop()
        Log.d(TAG, "ShizukuAppLockService destroyed")
        //AppLockManager.startFallbackServices(this, ShizukuAppLockService::class.java)
        isServiceRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "ShizukuAppLockService unbound")
        if (shouldStartService(appLockRepository, this::class.java)) {
            AppLockManager.startFallbackServices(this, ShizukuAppLockService::class.java)
        }
        return super.onUnbind(intent)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "AppLock Shizuku Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AppLock")
            .setContentText("Protecting your apps with Shizuku")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun checkAndLockApp(packageName: String, currentTime: Long) {
        if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
            Log.d(TAG, "App $packageName is temporarily unlocked, skipping app lock")
            return
        } else {
            if (AppLockManager.appUnlockTimes.isEmpty()) {
                AppLockManager.clearTemporarilyUnlockedApp()
            }
        }
        val lockedApps = appLockRepository.getLockedApps()
        if (!lockedApps.contains(packageName)) {
            return
        }

        val unlockDuration = appLockRepository.getUnlockTimeDuration()
        val unlockTimestamp = AppLockManager.appUnlockTimes[packageName] ?: 0

        Log.d(
            TAG,
            "Checking app lock for $packageName at $currentTime, last unlock timestamp: $unlockTimestamp, unlock duration: $unlockDuration"
        )
        if (unlockDuration > 0 && unlockTimestamp > 0) {
            val elapsedMinutes = (currentTime - unlockTimestamp) / (60 * 1000)
            if (elapsedMinutes < unlockDuration) {
                return
            } else {
                AppLockManager.appUnlockTimes.remove(packageName)
                if (AppLockManager.isAppTemporarilyUnlocked(packageName)) {
                    AppLockManager.clearTemporarilyUnlockedApp()
                }
            }
        }

        Log.d(TAG, "Locked app detected: $packageName")

        val intent = Intent(this, PasswordOverlayActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("locked_package", packageName)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start password overlay: ${e.message}", e)
        }
    }
}
