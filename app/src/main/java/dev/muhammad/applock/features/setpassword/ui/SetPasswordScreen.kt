package dev.muhammad.applock.features.setpassword.ui

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import dev.muhammad.applock.AppLockApplication
import dev.muhammad.applock.core.navigation.Screen
import dev.muhammad.applock.core.ui.shapes
import dev.muhammad.applock.features.lockscreen.ui.KeypadRow
import dev.muhammad.applock.ui.icons.Backspace

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun SetPasswordScreen(
    navController: NavController,
    isFirstTimeSetup: Boolean
) {
    var passwordState by remember { mutableStateOf("") }
    var confirmPasswordState by remember { mutableStateOf("") }
    var isConfirmationMode by remember { mutableStateOf(false) }

    var isVerifyOldPasswordMode by remember { mutableStateOf(!isFirstTimeSetup) }

    var showMismatchError by remember { mutableStateOf(false) }
    var showLengthError by remember { mutableStateOf(false) }
    var showInvalidOldPasswordError by remember { mutableStateOf(false) }
    val maxLength = 6

    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity
    val appLockRepository = remember {
        (context.applicationContext as? AppLockApplication)?.appLockRepository
    }

    BackHandler {
        if (isFirstTimeSetup) {
            Toast.makeText(context, "Set a PIN to continue", Toast.LENGTH_SHORT).show()
        } else {
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            } else {
                activity?.finish()
            }
        }
    }

    val fragmentActivity = LocalActivity.current as? androidx.fragment.app.FragmentActivity

    fun launchDeviceCredentialAuth() {
        if (fragmentActivity == null) return
        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to reset PIN")
            .setSubtitle("Use your device's PIN, pattern, or password")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        val biometricPrompt = BiometricPrompt(
            fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isVerifyOldPasswordMode = false
                    passwordState = ""
                    confirmPasswordState = ""
                    showInvalidOldPasswordError = false
                }

            })
        biometricPrompt.authenticate(promptInfo)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isFirstTimeSetup -> "Welcome to App Lock"
                            isVerifyOldPasswordMode -> "Enter your Current PIN"
                            isConfirmationMode -> "Confirm your PIN"
                            else -> "Set a new PIN"
                        },
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when {
                        isVerifyOldPasswordMode -> "Enter your current PIN"
                        isConfirmationMode -> "Confirm your new PIN"
                        else -> "Set a new PIN"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            if (showMismatchError) {
                Text(
                    text = "Wrong! Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (showLengthError) {
                Text(
                    text = "PIN must be 6 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (showInvalidOldPasswordError) {
                Text(
                    text = "Wrong! Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                val currentPassword = when {
                    isVerifyOldPasswordMode -> passwordState
                    isConfirmationMode -> confirmPasswordState
                    else -> passwordState
                }
                repeat(maxLength) { index ->
                    val filled = index < currentPassword.length
                    val isNext = index == currentPassword.length && index < maxLength
                    val indicatorState = remember(filled, isNext) {
                        when {
                            filled -> "filled"; isNext -> "next"; else -> "empty"
                        }
                    }
                    val scale by animateFloatAsState(
                        targetValue = if (filled) 1.2f else if (isNext) 1.1f else 1.0f,
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = FastOutSlowInEasing
                        ),
                        label = "indicatorScale"
                    )
                    AnimatedContent(
                        targetState = indicatorState,
                        transitionSpec = {
                            fadeIn(tween(100)) togetherWith fadeOut(tween(50))
                        },
                        label = "indicatorAnimation"
                    ) { state ->
                        val shape = when (state) {
                            "filled" -> shapes[index % shapes.size].toShape()
                            "next" -> MaterialShapes.Diamond.toShape()
                            else -> MaterialShapes.Circle.toShape()
                        }
                        val color = when (state) {
                            "filled" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .size(24.dp)
                                .background(color = color, shape = shape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isVerifyOldPasswordMode) {
                TextButton(onClick = { launchDeviceCredentialAuth() }) {
                    Text("Reset using the device's lock")
                }
            }

            if (isVerifyOldPasswordMode || isConfirmationMode) {
                TextButton(
                    onClick = {
                        if (isVerifyOldPasswordMode) {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                activity?.finish()
                            }
                        } else {
                            isConfirmationMode = false
                            if (!isFirstTimeSetup) {
                                isVerifyOldPasswordMode = true
                            }
                        }
                        // Reset states
                        passwordState = ""
                        confirmPasswordState = ""
                        showMismatchError = false
                        showLengthError = false
                        showInvalidOldPasswordError = false
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(if (isVerifyOldPasswordMode) "Cancel" else "Start Over")
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val onKeyClick: (String) -> Unit = { key ->
                    val currentActivePassword = when {
                        isVerifyOldPasswordMode -> passwordState
                        isConfirmationMode -> confirmPasswordState
                        else -> passwordState
                    }
                    val updatePassword: (String) -> Unit = when {
                        isVerifyOldPasswordMode -> { newPass -> passwordState = newPass }
                        isConfirmationMode -> { newPass -> confirmPasswordState = newPass }
                        else -> { newPass -> passwordState = newPass }
                    }

                    when (key) {
                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                            if (currentActivePassword.length < maxLength) {
                                updatePassword(currentActivePassword + key)
                            }
                        }

                        "backspace" -> {
                            if (currentActivePassword.isNotEmpty()) {
                                updatePassword(currentActivePassword.dropLast(1))
                            }
                            showMismatchError = false
                            showLengthError = false
                            showInvalidOldPasswordError = false
                        }

                        "proceed" -> {
                            when {
                                isVerifyOldPasswordMode -> {
                                    if (passwordState.length == maxLength) {
                                        if (appLockRepository!!.validatePassword(passwordState)) {
                                            isVerifyOldPasswordMode = false
                                            passwordState = "" // Clear for setting new PIN
                                            showInvalidOldPasswordError = false
                                        } else {
                                            showInvalidOldPasswordError = true
                                            passwordState = ""
                                        }
                                    } else {
                                        showLengthError = true
                                    }
                                }

                                !isConfirmationMode -> {
                                    if (passwordState.length == maxLength) {
                                        isConfirmationMode = true
                                        showLengthError = false
                                    } else {
                                        showLengthError = true
                                    }
                                }

                                else -> { // Confirmation mode
                                    if (confirmPasswordState.length == maxLength) {
                                        if (passwordState == confirmPasswordState) {
                                            appLockRepository?.setPassword(passwordState)
                                            Toast.makeText(
                                                context,
                                                "Password set successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Navigate to Main screen after setting password
                                            navController.navigate(Screen.Main.route) {
                                                popUpTo(Screen.SetPassword.route) {
                                                    inclusive = true
                                                }
                                                if (isFirstTimeSetup) {
                                                    popUpTo(Screen.AppIntro.route) {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        } else {
                                            showMismatchError = true
                                            confirmPasswordState = ""
                                        }
                                    } else {
                                        showLengthError = true
                                    }
                                }
                            }
                        }
                    }
                }

                KeypadRow(
                    keys = listOf("1", "2", "3"),
                    onKeyClick = onKeyClick
                )
                KeypadRow(
                    keys = listOf("4", "5", "6"),
                    onKeyClick = onKeyClick
                )
                KeypadRow(
                    keys = listOf("7", "8", "9"),
                    onKeyClick = onKeyClick
                )
                KeypadRow(
                    keys = listOf("backspace", "0", "proceed"),
                    icons = listOf(
                        Backspace,
                        null,
                        if (isConfirmationMode || isVerifyOldPasswordMode) Icons.Default.Check else Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    ),
                    onKeyClick = onKeyClick
                )
            }
        }
    }
}
