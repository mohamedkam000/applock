package dev.muhammad.applock.core.navigation

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

sealed class Screen(val route: String) {
    object AppIntro : Screen("app_intro")
    object SetPassword : Screen("set_password")
    object ChangePassword : Screen("change_password")
    object Main : Screen("main")
    object PasswordOverlay : Screen("password_overlay")
    object Settings : Screen("settings")
}

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String) {
    val duration = 500

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
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.PasswordOverlay.route) { inclusive = true }
                                    }
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }
                            })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Enter the PIN")
                        .setSubtitle("Authenticate with your Fingerprint")
                        .setNegativeButtonText("Use PIN")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .setConfirmationRequired(false)
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                },
                onAuthSuccess = {
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