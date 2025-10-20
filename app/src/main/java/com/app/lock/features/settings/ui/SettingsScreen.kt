package com.app.lock.features.settings.ui

//import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.QueryStats
//import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
//import com.app.lock.core.broadcast.DeviceAdmin
import com.app.lock.core.navigation.Screen
// import com.app.lock.core.utils.hasUsagePermission
// import com.app.lock.core.utils.isAccessibilityServiceEnabled
// import com.app.lock.core.utils.openAccessibilitySettings
import com.app.lock.data.repository.AppLockRepository
// import com.app.lock.data.repository.BackendImplementation
//import com.app.lock.services.ExperimentalAppLockService
//import com.app.lock.ui.icons.Accessibility
//import com.app.lock.ui.icons.BrightnessHigh
import com.app.lock.ui.icons.Fingerprint
import com.app.lock.ui.icons.FingerprintOff
import com.app.lock.ui.icons.Github
//import com.app.lock.ui.icons.Timer
//import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val appLockRepository = remember { AppLockRepository(context) }
    var useBiometricAuth by remember {
        mutableStateOf(appLockRepository.isBiometricAuthEnabled())
    }
    var popBiometricAuth by remember {
        mutableStateOf(appLockRepository.shouldPromptForBiometricAuth())
    }
    val biometricManager = BiometricManager.from(context)
    val isBiometricAvailable = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLargeEmphasized) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Lock Screen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column {
                        SettingItem(
                            icon = if (useBiometricAuth) Fingerprint else FingerprintOff,
                            title = "Biometric Unlock",
                            description = if (isBiometricAvailable) "Use your fingerprint to unlock" else "Biometrics are not available on this device",
                            checked = useBiometricAuth && isBiometricAvailable,
                            enabled = isBiometricAvailable,
                            onCheckedChange = { isChecked ->
                                useBiometricAuth = isChecked
                                appLockRepository.setBiometricAuthEnabled(isChecked)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Default.Person,
                            title = "Prompt for Biometric",
                            description = "Prompt for biometric authentication before entering PIN",
                            checked = popBiometricAuth,
                            enabled = useBiometricAuth,
                            onCheckedChange = { isChecked ->
                                popBiometricAuth = isChecked
                                appLockRepository.setPromptForBiometricAuth(isChecked)
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 0.dp, bottom = 12.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column {
                        ActionSettingItem(icon = Github, title = "View Source Code", onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "https://github.com/mohamedkam000/applock".toUri()
                                )
                            )
                        })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ActionSettingItem(
                            icon = Icons.Filled.Person,
                            title = "Developer Profile",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://github.com/mohamedkam000".toUri()
                                    )
                                )
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { if (enabled) onCheckedChange(!checked) }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.38f
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun ActionSettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = iconTint
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// @Composable
// fun UnlockTimeDurationDialog(
//     currentDuration: Int,
//     onDismiss: () -> Unit,
//     onConfirm: (Int) -> Unit
// ) {
//     val durations = listOf(0, 1, 5, 15, 30, 60)
//     var selectedDuration by remember { mutableIntStateOf(currentDuration) }
// 
//     if (!durations.contains(selectedDuration)) {
//         selectedDuration = durations.minByOrNull { abs(it - currentDuration) } ?: 0
//     }
// 
//     AlertDialog(
//         onDismissRequest = onDismiss,
//         title = { Text("App Unlock Duration") },
//         text = {
//             Column {
//                 Text("Pick how long apps should remain unlocked:")
// 
//                 durations.forEach { duration ->
//                     Row(
//                         modifier = Modifier
//                             .fillMaxWidth()
//                             .clickable { selectedDuration = duration }
//                             .padding(vertical = 12.dp),
//                         verticalAlignment = Alignment.CenterVertically
//                     ) {
//                         RadioButton(
//                             selected = selectedDuration == duration,
//                             onClick = { selectedDuration = duration }
//                         )
//                         Text(
//                             text = when (duration) {
//                                 0 -> "Lock immediately"
//                                 1 -> "1 minute"
//                                 60 -> "1 hour"
//                                 else -> "$duration minutes"
//                             },
//                             modifier = Modifier.padding(start = 8.dp)
//                         )
//                     }
//                 }
//             }
//         },
//         confirmButton = {
//             TextButton(onClick = { onConfirm(selectedDuration) }) {
//                 Text("Confirm")
//             }
//         },
//         dismissButton = {
//             TextButton(onClick = onDismiss) {
//                 Text("Cancel")
//             }
//         }
//     )
// }
// 
// @Composable
// fun PermissionRequiredDialog(
//     onDismiss: () -> Unit,
//     onConfirm: () -> Unit
// ) {
//     AlertDialog(
//         onDismissRequest = onDismiss,
//         title = { Text("Permissions Required") },
//         text = {
//             Text(
//                 "To enable Anti Uninstall, App Lock needs Device Admin permission"
//             )
//         },
//         confirmButton = {
//             TextButton(onClick = onConfirm) {
//                 Text("Grant Permission")
//             }
//         },
//         dismissButton = {
//             TextButton(onClick = onDismiss) {
//                 Text("Cancel")
//             }
//         }
//     )
// }
// 
// @Composable
// fun DeviceAdminDialog(
//     onDismiss: () -> Unit,
//     onConfirm: () -> Unit
// ) {
//     AlertDialog(
//         onDismissRequest = onDismiss,
//         title = { Text("Device Admin") },
//         text = {
//             Text(
//                 "App Lock needs Device Admin permission to prevent uninstallation."
//             )
//         },
//         confirmButton = {
//             TextButton(onClick = onConfirm) {
//                 Text("Enable")
//             }
//         },
//         dismissButton = {
//             TextButton(onClick = onDismiss) {
//                 Text("Cancel")
//             }
//         }
//     )
// }

// @Composable
// fun AccessibilityDialog(
//     onDismiss: () -> Unit,
//     onConfirm: () -> Unit
// ) {
//     AlertDialog(
//         onDismissRequest = onDismiss,
//         title = { Text("Accessibility Service") },
//         text = {
//             Text(
//                 "App Lock needs Accessibility Service permission to monitor app usage."
//             )
//         },
//         confirmButton = {
//             TextButton(onClick = onConfirm) {
//                 Text("Enable")
//             }
//         },
//         dismissButton = {
//             TextButton(onClick = onDismiss) {
//                 Text("Cancel")
//             }
//         }
//     )
// }
// 