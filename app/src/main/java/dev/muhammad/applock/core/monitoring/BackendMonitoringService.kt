package dev.muhammad.applock.core.monitoring

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import dev.muhammad.applock.core.utils.appLockRepository
import dev.muhammad.applock.data.repository.BackendImplementation
import dev.muhammad.applock.services.ExperimentalAppLockService

class BackendMonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null
    private val monitoringInterval = 5000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    private fun startMonitoring() {
        monitoringRunnable = object : Runnable {
            override fun run() {
                try {
                    checkAndSwitchBackend()
                } catch (e: Exception) { }

                handler.postDelayed(this, monitoringInterval)
            }
        }

        handler.post(monitoringRunnable!!)
    }

    private fun stopMonitoring() {
        monitoringRunnable?.let { handler.removeCallbacks(it) }
        monitoringRunnable = null
    }

    private fun checkAndSwitchBackend() {
        val appLockRepository = applicationContext.appLockRepository()
        val currentActive = appLockRepository.getActiveBackend()
        val newActiveBackend = appLockRepository.validateAndSwitchBackend(applicationContext)

        if (newActiveBackend != currentActive) {
            handleBackendSwitch(currentActive!!, newActiveBackend)
        }
    }

    private fun handleBackendSwitch(
        oldBackend: BackendImplementation,
        newBackend: BackendImplementation
    ) {
        stopAllServices()
        startBackendService(newBackend)
    }

    private fun stopAllServices() {
        stopService(Intent(this, ExperimentalAppLockService::class.java))
    }

    private fun stopBackendService(backend: BackendImplementation) {
        when (backend) {
            BackendImplementation.USAGE_STATS -> {
                stopService(Intent(this, ExperimentalAppLockService::class.java))
            } else { }
        }
    }

    private fun startBackendService(backend: BackendImplementation) {
        when (backend) {
            BackendImplementation.USAGE_STATS -> {
                startService(Intent(this, ExperimentalAppLockService::class.java))
            } else { }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BackendMonitoringService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BackendMonitoringService::class.java)
            context.stopService(intent)
        }
    }
}