package com.app.lock.features.appintro.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.app.intro.AppIntro
import com.app.intro.IntroPage
import com.app.lock.core.navigation.Screen
import com.app.lock.core.utils.appLockRepository
import com.app.lock.core.utils.hasUsagePermission
//import com.app.lock.core.utils.isAccessibilityServiceEnabled
import com.app.lock.core.utils.launchBatterySettings
import com.app.lock.data.repository.BackendImplementation
import com.app.lock.features.appintro.domain.AppIntroManager
import com.app.lock.services.ExperimentalAppLockService
//import com.app.lock.ui.icons.Accessibility
//import com.app.lock.ui.icons.BatterySaver
import com.app.lock.ui.icons.Display

enum class AppUsageMethod {
//    ACCESSIBILITY,
    USAGE_STATS
}

@Composable
fun MethodSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(
                alpha = 0.1f
            )
        ),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun AppIntroScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

//    var selectedMethod by remember { mutableStateOf(AppUsageMethod.ACCESSIBILITY) }
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var notificationPermissionGranted by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var usageStatsPermissionGranted by remember { mutableStateOf(context.hasUsagePermission()) }
//    var accessibilityServiceEnabled by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }

    val requestPermissionLauncher =
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    notificationPermissionGranted = true
                } else {
                    Toast.makeText(
                        context,
                        "Notification permission is required for AppLock to function properly.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else null

    LaunchedEffect(key1 = context) {
        overlayPermissionGranted = Settings.canDrawOverlays(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted =
                NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
//        accessibilityServiceEnabled = context.isAccessibilityServiceEnabled()
    }

    val onFinishCallback = {
        AppIntroManager.markIntroAsCompleted(context)
        navController.navigate(Screen.SetPassword.route) {
            popUpTo(Screen.AppIntro.route) { inclusive = true }
        }
    }

    val basicPages = listOf(
        IntroPage(
            title = "Welcome to App Lock",
            description = "This is an app locker made for a university project.",
            icon = Icons.Filled.Lock,
            backgroundColor = Color(0xFF00A5FF),
            contentColor = Color.White,
            onNext = { true }
        ),
        IntroPage(
            title = "Display Over Apps",
            description = "App Lock needs the permission to display over other apps to show the lock screen.",
            icon = Display,
            backgroundColor = Color(0xFFDC143C),
            contentColor = Color.White,
            onNext = {
                overlayPermissionGranted = Settings.canDrawOverlays(context)
                if (!overlayPermissionGranted) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = "package:${context.packageName}".toUri()
                    Toast.makeText(
                        context,
                        "Allow App Lock to display over other apps.",
                        Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(intent)
                    false
                } else {
                    true
                }
            }
        )
//         IntroPage(
//             title = "Notification Permission",
//             description = "This permission is needed just to make sure the system won't terminate the app.",
//             icon = Icons.Default.Notifications,
//             backgroundColor = Color(0xFFFFA700),
//             contentColor = Color.White,
//             onNext = {
//                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                     val isGrantedCurrently =
//                         NotificationManagerCompat.from(context).areNotificationsEnabled()
//                     notificationPermissionGranted = isGrantedCurrently
//                     if (!isGrantedCurrently) {
//                         requestPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
//                         return@IntroPage false
//                     } else {
//                         return@IntroPage true
//                     }
//                 } else {
//                     true
//                 }
//             }
//         )
    )

//     val methodSelectionPage = IntroPage(
//         title = "Choose App Detection Method",
//         description = "Select how you want App Lock to detect when protected apps are launched.",
//         icon = Icons.Default.Lock,
//         backgroundColor = Color(0xFF6B46C1),
//         contentColor = Color.White,
//         customContent = {
//             Column(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .background(Color(0xFF6B46C1))
//                     .padding(16.dp),
//                 horizontalAlignment = Alignment.CenterHorizontally,
//                 verticalArrangement = Arrangement.Center
//             ) {
//                 Text(
//                     text = "App Detection Method",
//                     fontSize = 20.sp,
//                     fontWeight = FontWeight.Bold,
//                     color = Color.White,
//                     textAlign = TextAlign.Center
//                 )
//                 Spacer(modifier = Modifier.height(12.dp))
//                 Text(
//                     text = "Select how you want App Lock to detect when protected apps are launched. Each method has its own advantages, disadvantages, and requirements.",
//                     fontSize = 14.sp,
//                     lineHeight = 19.sp,
//                     color = Color.White.copy(alpha = 0.9f),
//                     textAlign = TextAlign.Center
//                 )
//                 Spacer(modifier = Modifier.height(12.dp))
// 
//                 MethodSelectionCard(
//                     title = "Accessibility Service",
//                     description = "Standard method that works on all devices. Requires the accessibility permission.",
//                     icon = Accessibility,
//                     isSelected = selectedMethod == AppUsageMethod.ACCESSIBILITY,
//                     onClick = { selectedMethod = AppUsageMethod.ACCESSIBILITY },
//                 )
// 
//                 MethodSelectionCard(
//                     title = "Usage Stats",
//                     description = "Experimental method utilizing system usage statistics. Works better on newer devices.",
//                     icon = Icons.Default.QueryStats,
//                     isSelected = selectedMethod == AppUsageMethod.USAGE_STATS,
//                     onClick = { selectedMethod = AppUsageMethod.USAGE_STATS },
//                 )
// 
//                 Spacer(modifier = Modifier.height(16.dp))
//             }
//         },
//         onNext = { true }
//     )

    val methodSpecificPages = IntroPage(
        title = "Usage Stats Permission",
        description = "This permission is required to detect when locked apps are launched.\n\nIf you get the message \"Restricted Setting\", go to Settings > Apps > App Lock > Upper Right menu, and press \"Allow restricted settings\".\n\nTap 'Next' to enable it.",
        icon = Icons.Default.QueryStats,
        backgroundColor = Color(0xFFB453A4),
        contentColor = Color.White,
        onNext = {
            usageStatsPermissionGranted = context.hasUsagePermission()
            if (!usageStatsPermissionGranted) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                false
            } else {
                context.appLockRepository()
                    .setBackendImplementation(BackendImplementation.USAGE_STATS)
                context.startService(
                    Intent(context, ExperimentalAppLockService::class.java)
                )
                true
            }
        }
    )

    val finalPage = IntroPage(
        title = "Perfect Privacy",
        description = "This app runs offline. There's no way your device ever gets hacked by this app.",
        icon = Icons.Default.Lock,
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
//        onNext = {
//            overlayPermissionGranted = Settings.canDrawOverlays(context)
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                 notificationPermissionGranted =
//                     NotificationManagerCompat.from(context).areNotificationsEnabled()
//             }

//             val methodPermissionGranted = when (selectedMethod) {
//                 AppUsageMethod.USAGE_STATS -> context.hasUsagePermission()
//                 else -> false
//             }

//             val allPermissionsGranted = if (selectedMethod == AppUsageMethod.ACCESSIBILITY) {
//                 overlayPermissionGranted && notificationPermissionGranted && methodPermissionGranted
//             } else {
//                 overlayPermissionGranted && notificationPermissionGranted && methodPermissionGranted
//             }

//             if (!allPermissionsGranted) {
//                 Toast.makeText(
//                     context,
//                     "All permissions are required to proceed.",
//                     Toast.LENGTH_SHORT
//                 ).show()
//             }
//            allPermissionsGranted
//        }
    )

    val allPages = basicPages + methodSpecificPages + finalPage

    AppIntro(
        pages = allPages,
        onSkip = {
            AppIntroManager.markIntroAsCompleted(context)
            navController.navigate(Screen.SetPassword.route) {
                popUpTo(Screen.AppIntro.route) { inclusive = true }
            }
        },
        onFinish = onFinishCallback,
        showSkipButton = false,
        useAnimatedPager = true,
        nextButtonText = "Next",
        finishButtonText = "Get Started"
    )
}