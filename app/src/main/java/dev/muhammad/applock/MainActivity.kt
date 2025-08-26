package dev.muhammad.applock

import android.os.Bundle
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.rememberNavController
import dev.muhammad.applock.core.navigation.AppNavHost
import dev.muhammad.applock.core.navigation.Screen
import dev.muhammad.applock.features.appintro.domain.AppIntroManager
import dev.muhammad.applock.ui.theme.AppLockTheme
import dev.muhammad.applock.data.repository.AppLockRepository

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppLockTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()
                    val startDestination = determineStartDestination()

                    AppNavHost(navController = navController, startDestination = startDestination)

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        if (navController.currentDestination?.route == Screen.AppIntro.route || navController.currentDestination?.route == Screen.SetPassword.route) {
                            return@LifecycleEventEffect
                        }
                        if (navController.currentDestination?.route != Screen.PasswordOverlay.route) {
                            navController.navigate(Screen.PasswordOverlay.route)
                        }
                    }
                }
            }
        }
    }

    private fun determineStartDestination(): String {
        if (AppIntroManager.shouldShowIntro(this)) {
            return Screen.AppIntro.route
        }

        val sharedPrefs = getSharedPreferences("app_lock_prefs", MODE_PRIVATE)
        val isPasswordSet = sharedPrefs.contains("password")

        return if (!isPasswordSet) {
            Screen.SetPassword.route
        } else {
            Screen.PasswordOverlay.route
        }
    }
}

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