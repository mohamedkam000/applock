package com.app.lock.core.utils

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri

@SuppressLint("BatteryLife")
fun launchBatterySettings(context: Context) {
    val pm = context.packageManager
    val requestIgnoreIntent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }

    if (requestIgnoreIntent.resolveActivity(pm) != null) {
        context.startActivity(requestIgnoreIntent)
        Toast.makeText(
            context,
            "Allow App Lock to bypass battery optimisations",
            Toast.LENGTH_LONG
        ).show()
    } else {
        val standardIntent =
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        if (standardIntent.resolveActivity(pm) != null) {
            context.startActivity(standardIntent)
            Toast.makeText(
                context,
                "Could not find the setting. Remove app from battery restrictions.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Very rare case where even the standard settings screen is missing.
            Toast.makeText(
                context,
                "Couldn't open battery settings",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

fun Context.hasUsagePermission(): Boolean {
    try {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationInfo.uid,
            applicationInfo.packageName
        )
        return (mode == AppOpsManager.MODE_ALLOWED)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("AppLockUtils", "Error checking usage permission: ${e.message}")
        return false
    }
}

fun Context.appLockRepository() =
    (applicationContext as? com.app.lock.AppLockApplication)?.appLockRepository
        ?: throw IllegalStateException("AppLockRepository not initialized")
