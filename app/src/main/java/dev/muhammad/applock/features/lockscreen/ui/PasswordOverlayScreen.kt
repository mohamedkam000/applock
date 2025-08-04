package dev.muhammad.applock.features.lockscreen.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dev.muhammad.applock.core.ui.shapes
import dev.muhammad.applock.core.utils.appLockRepository
import dev.muhammad.applock.core.utils.vibrate
import dev.muhammad.applock.data.repository.AppLockRepository
import dev.muhammad.applock.services.AppLockAccessibilityService
import dev.muhammad.applock.services.AppLockManager
import dev.muhammad.applock.ui.icons.Backspace
import dev.muhammad.applock.ui.icons.Fingerprint
import dev.muhammad.applock.ui.theme.AppLockTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class PasswordOverlayActivity : FragmentActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var appLockRepository: AppLockRepository
    private var appLockAccessibilityService: AppLockAccessibilityService? = null
    internal var lockedPackageNameFromIntent: String? = null

    private var isBiometricPromptShowingLocal = false
    private var movedToBackground = false
    private var appName: String = ""

    private val TAG = "PasswordOverlayActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lockedPackageNameFromIntent = intent.getStringExtra("locked_package")
        if (lockedPackageNameFromIntent == null) {
            Log.e(TAG, "No locked_package name provided in intent. Finishing.")
            finishAffinity()
            return
        }

        enableEdgeToEdge()

        appLockRepository = AppLockRepository(applicationContext)
        appLockAccessibilityService = AppLockAccessibilityService.getInstance()

        onBackPressedDispatcher.addCallback(this) {
            // Prevent back navigation to maintain security
        }

        setupWindow()
        loadAppNameAndSetupUI()
    }

    private fun setupWindow() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val layoutParams = window.attributes
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        if (appLockRepository.shouldUseMaxBrightness()) {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        window.attributes = layoutParams
    }

    private fun loadAppNameAndSetupUI() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(lockedPackageNameFromIntent!!, 0)
                ).toString()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading app name: ${e.message}")
                appName = "App"
            }

            setupBiometricPromptInternal()

            runOnUiThread {
                setupUI()
                if (appLockRepository.shouldPromptForBiometricAuth()) {
                    triggerBiometricPromptIfNeeded()
                }
            }
        }
    }

    private fun setupUI() {
        val onPinAttemptCallback = { pin: String ->
            val isValid = appLockRepository.validatePassword(pin)
            if (isValid) {
                lockedPackageNameFromIntent?.let { pkgName ->
                    AppLockManager.unlockApp(pkgName)
                    finishAffinity()
                }
            }
            isValid
        }

        setContent {
            AppLockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PasswordOverlayScreen(
                        modifier = Modifier.padding(innerPadding),
                        showBiometricButton = appLockRepository.isBiometricAuthEnabled(),
                        fromMainActivity = false,
                        onBiometricAuth = { triggerBiometricPromptIfNeeded() },
                        onAuthSuccess = {},
                        lockedAppName = appName,
                        onPinAttempt = onPinAttemptCallback
                    )
                }
            }
        }
    }

    private fun supportsBiometric(): Boolean {
        return if (Build.VERSION.SDK_INT < 29) {
            val keyguardManager =
                applicationContext.getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
            packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) && keyguardManager?.isKeyguardSecure == true
        } else {
            val biometricManager = BiometricManager.from(this)
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    private fun setupBiometricPromptInternal() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, authenticationCallbackInternal)

        val appNameForPrompt = appName.ifEmpty { "this app" }
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock $appNameForPrompt")
            .setSubtitle("Confirm biometric to continue")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()
    }

    private val authenticationCallbackInternal =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isBiometricPromptShowingLocal = false
                AppLockManager.reportBiometricAuthFinished()
                Log.w(TAG, "Authentication error: $errString ($errorCode)")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isBiometricPromptShowingLocal = false
                lockedPackageNameFromIntent?.let { pkgName ->
                    AppLockManager.temporarilyUnlockAppWithBiometrics(pkgName)
                }
                finishAffinity()
            }
        }

    override fun onResume() {
        super.onResume()
        movedToBackground = false
        AppLockManager.isLockScreenShown.set(true) // Set to true when activity is visible
        // Apply user preferences asynchronously to avoid blocking
        Thread {
            applyUserPreferences()
        }.start()
    }

    private fun applyUserPreferences() {
        if (appLockRepository.shouldUseMaxBrightness()) {
            window.attributes = window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            }
            if (window.decorView.isAttachedToWindow) {
                windowManager.updateViewLayout(window.decorView, window.attributes)
            }
        }
    }

    fun triggerBiometricPromptIfNeeded() {
        if (appLockRepository.isBiometricAuthEnabled()) {
            AppLockManager.reportBiometricAuthStarted()
            isBiometricPromptShowingLocal = true
            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                Log.e(TAG, "Error calling biometricPrompt.authenticate: ${e.message}", e)
                isBiometricPromptShowingLocal = false
                AppLockManager.reportBiometricAuthFinished()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        movedToBackground = true
        AppLockManager.isLockScreenShown.set(false) // Set to false when activity is no longer visible
        if (!isFinishing && !isDestroyed) {
            Log.d(TAG, "Activity moved to background: $lockedPackageNameFromIntent")
            AppLockManager.reportBiometricAuthFinished()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLockManager.isLockScreenShown.set(false) // Failsafe: Ensure it's false on destroy
        AppLockManager.reportBiometricAuthFinished()
        Log.d(TAG, "PasswordOverlayActivity onDestroy for $lockedPackageNameFromIntent")
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
fun PasswordOverlayScreen(
    modifier: Modifier = Modifier,
    showBiometricButton: Boolean = false,
    fromMainActivity: Boolean = false,
    onBiometricAuth: () -> Unit = {},
    onAuthSuccess: () -> Unit,
    lockedAppName: String? = null,
    onPinAttempt: ((pin: String) -> Boolean)? = null
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val passwordState = remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }
        val maxLength = 6

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = if (fromMainActivity) 40.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = if (!fromMainActivity && !lockedAppName.isNullOrEmpty())
                    lockedAppName
                else
                    "Enter password to continue",
                style = if (!fromMainActivity && !lockedAppName.isNullOrEmpty())
                    MaterialTheme.typography.titleLargeEmphasized
                else
                    MaterialTheme.typography.headlineMediumEmphasized,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordIndicators(
                passwordLength = passwordState.value.length,
                maxLength = maxLength
            )

            if (showError) {
                Text(
                    text = "Wrong. Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            KeypadSection(
                passwordState = passwordState,
                maxLength = maxLength,
                showBiometricButton = showBiometricButton,
                fromMainActivity = fromMainActivity,
                onBiometricAuth = onBiometricAuth,
                onAuthSuccess = onAuthSuccess,
                onPinAttempt = { pin ->
                    if (onPinAttempt == null) {
                        showError = true
                        false
                    } else {
                        val result = onPinAttempt(pin)
                        showError = !result
                        result
                    }
                },
                onPasswordChange = { showError = false },
                onPinIncorrect = { showError = true }
            )
        }
    }

    if (fromMainActivity) {
        BackHandler {}
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
fun PasswordIndicators(
    passwordLength: Int,
    maxLength: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        repeat(maxLength) { index ->
            val filled = index < passwordLength
            val isNext = index == passwordLength && index < maxLength

            val indicatorState = remember(filled, isNext) {
                when {
                    filled -> "filled"
                    isNext -> "next"
                    else -> "empty"
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (filled) 1.2f else if (isNext) 1.1f else 1.0f,
                animationSpec = tween(
                    durationMillis = 100, // Further reduced duration
                    easing = FastOutSlowInEasing
                ),
                label = "indicatorScale"
            )

            AnimatedContent(
                targetState = indicatorState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 100)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 100))
                },
                label = "indicatorStateAnimation"
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
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .size(24.dp)
                        .background(color = color, shape = shape)
                )
            }
        }
    }
}

@Composable
fun KeypadSection(
    passwordState: MutableState<String>,
    maxLength: Int,
    showBiometricButton: Boolean,
    fromMainActivity: Boolean = false,
    onBiometricAuth: () -> Unit,
    onAuthSuccess: () -> Unit,
    onPinAttempt: ((pin: String) -> Boolean)? = null,
    onPasswordChange: () -> Unit,
    onPinIncorrect: () -> Unit
) {
    val context = LocalContext.current

    val onDigitKeyClick = remember(passwordState, maxLength, onPasswordChange) {
        { key: String ->
            addDigitToPassword(
                passwordState,
                key,
                maxLength,
                onPasswordChange
            )
        }
    }

    val disableHaptics = context.appLockRepository().shouldDisableHaptics()

    val onSpecialKeyClick = remember(
        passwordState,
        maxLength,
        fromMainActivity,
        onAuthSuccess,
        onPinAttempt,
        context,
        onPasswordChange,
        onPinIncorrect
    ) {
        { key: String ->
            handleKeypadSpecialButtonLogic(
                key = key,
                passwordState = passwordState,
                maxLength = maxLength,
                fromMainActivity = fromMainActivity,
                onAuthSuccess = onAuthSuccess,
                onPinAttempt = onPinAttempt,
                context = context,
                onPasswordChange = onPasswordChange,
                onPinIncorrect = onPinIncorrect
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        KeypadRow(
            disableHaptics = disableHaptics,
            keys = listOf("1", "2", "3"),
            onKeyClick = onDigitKeyClick
        )
        KeypadRow(
            disableHaptics = disableHaptics,
            keys = listOf("4", "5", "6"),
            onKeyClick = onDigitKeyClick
        )
        KeypadRow(
            disableHaptics = disableHaptics,
            keys = listOf("7", "8", "9"),
            onKeyClick = onDigitKeyClick
        )
        KeypadRow(
            disableHaptics = disableHaptics,
            keys = listOf("backspace", "0", "proceed"),
            icons = listOf(Backspace, null, Icons.AutoMirrored.Rounded.KeyboardArrowRight),
            onKeyClick = onSpecialKeyClick
        )
        if (showBiometricButton) {
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedButton(
                onClick = onBiometricAuth,
                modifier = Modifier.padding(8.dp),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Fingerprint,
                    contentDescription = "Biometric Authentication",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun addDigitToPassword(
    passwordState: MutableState<String>,
    digit: String,
    maxLength: Int,
    onPasswordChange: () -> Unit
) {
    if (passwordState.value.length < maxLength) {
        passwordState.value += digit
        onPasswordChange()
    }
}

private fun handleKeypadSpecialButtonLogic(
    key: String,
    passwordState: MutableState<String>,
    maxLength: Int,
    fromMainActivity: Boolean,
    onAuthSuccess: () -> Unit,
    onPinAttempt: ((pin: String) -> Boolean)?,
    context: Context,
    onPasswordChange: () -> Unit,
    onPinIncorrect: () -> Unit
) {
    val appLockRepository = context.appLockRepository()

    when (key) {
        "0" -> addDigitToPassword(passwordState, key, maxLength, onPasswordChange)
        "backspace" -> {
            if (passwordState.value.isNotEmpty()) {
                passwordState.value = passwordState.value.dropLast(1)
                onPasswordChange()
            }
        }

        "proceed" -> {
            if (passwordState.value.length == maxLength) {
                if (fromMainActivity) {
                    if (appLockRepository.validatePassword(passwordState.value)) {
                        onAuthSuccess()
                    } else {
                        passwordState.value = ""
                        if (!appLockRepository.shouldDisableHaptics()) {
                            vibrate(context, 100)
                        }
                        onPinIncorrect()
                    }
                } else {
                    onPinAttempt?.let { attempt ->
                        val pinWasCorrectAndProcessed = attempt(passwordState.value)
                        if (!pinWasCorrectAndProcessed) {
                            passwordState.value = ""
                            if (!appLockRepository.shouldDisableHaptics()) {
                                vibrate(context, 100)
                            }
                        }
                    } ?: run {
                        Log.e(
                            "PasswordOverlayScreen",
                            "onPinAttempt callback is null for app unlock path."
                        )
                        passwordState.value = ""
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeypadRow(
    disableHaptics: Boolean = false,
    keys: List<String>,
    icons: List<ImageVector?> = emptyList(),
    onKeyClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEachIndexed { index, key ->
            val interactionSource = remember { MutableInteractionSource() }
            ElevatedButton(
                onClick = {
                    scope.launch {
                        if (!disableHaptics) {
                            vibrate(context, 100)
                        }
                    }
                    onKeyClick(key)
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                shape = CircleShape,
                interactionSource = interactionSource,
            ) {
                if (icons.isNotEmpty() && index < icons.size && icons[index] != null) {
                    Icon(
                        imageVector = icons[index]!!,
                        contentDescription = key,
                        modifier = Modifier.size(40.dp),
                        tint = if (key == "backspace") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.displaySmallEmphasized,
                    )
                }
            }
        }
    }
}
