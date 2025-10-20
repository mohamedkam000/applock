package com.app.lock

import android.app.Application
import android.content.Context
import android.os.Build
import com.app.lock.data.repository.AppLockRepository

class AppLockApplication : Application() {
    lateinit var appLockRepository: AppLockRepository

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appLockRepository = AppLockRepository(this)
   }
}