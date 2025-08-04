package dev.muhammad.applock.core.navigation

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.muhammad.applock.AppLockApplication
import dev.muhammad.applock.features.appintro.ui.AppIntroScreen
import dev.muhammad.applock.features.applist.ui.MainScreen
import dev.muhammad.applock.features.lockscreen.ui.PasswordOverlayScreen
import dev.muhammad.applock.features.setpassword.ui.SetPasswordScreen
import dev.muhammad.applock.features.settings.ui.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String) {
    val duration = 400

    val application = LocalContext.current.applicationContext as AppLockApplication

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(duration)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(duration))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(duration)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(duration))
        },
    ) {
        composable(Screen.AppIntro.route) { AppIntroScreen(navController) }

        composable(Screen.SetPassword.route) { SetPasswordScreen(navController, true) }

        composable(Screen.ChangePassword.route) { SetPasswordScreen(navController, false) }

        composable(Screen.Main.route) { MainScreen(navController) }

        composable(Screen.PasswordOverlay.route) {
            val context = LocalActivity.current as FragmentActivity

            PasswordOverlayScreen(
                showBiometricButton = application.appLockRepository.isBiometricAuthEnabled(),
                fromMainActivity = true,
                onBiometricAuth = {
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt =
                        BiometricPrompt(
                            context,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    Log.w(
                                        "AppNavigator",
                                        "Authentication error: $errString ($errorCode)"
                                    )
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.PasswordOverlay.route) { inclusive = true }
                                    }
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    Log.w(
                                        "AppNavigator",
                                        "Authentication failed (fingerprint not recognized)"
                                    )
                                }
                            })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Confirm password")
                        .setSubtitle("Confirm biometric to continue")
                        .setNegativeButtonText("Use PIN")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .setConfirmationRequired(false)
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                },
                onAuthSuccess = {
                    // if there is back stack, pop back, otherwise navigate to Main
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.PasswordOverlay.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}

