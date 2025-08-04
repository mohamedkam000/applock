package dev.muhammad.applock

import android.app.Application
import android.content.Context
import android.os.Build
import dev.muhammad.applock.data.repository.AppLockRepository
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.sui.Sui

class AppLockApplication : Application() {
    lateinit var appLockRepository: AppLockRepository

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
    }

    override fun onCreate() {
        super.onCreate()
        appLockRepository = AppLockRepository(this)
        Sui.init(packageName)
    }
}
