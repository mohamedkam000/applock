package dev.muhammad.applock.features.applist.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.muhammad.applock.R
import dev.muhammad.applock.core.broadcast.DeviceAdmin
import dev.muhammad.applock.core.navigation.Screen
import dev.muhammad.applock.core.utils.appLockRepository
import dev.muhammad.applock.core.utils.hasUsagePermission
import dev.muhammad.applock.core.utils.isAccessibilityServiceEnabled
import dev.muhammad.applock.core.utils.openAccessibilitySettings
import dev.muhammad.applock.data.repository.BackendImplementation
import dev.muhammad.applock.ui.components.AccessibilityServiceGuideDialog
import dev.muhammad.applock.ui.components.AntiUninstallAccessibilityPermissionDialog
import dev.muhammad.applock.ui.components.ShizukuPermissionDialog
import dev.muhammad.applock.ui.components.UsageStatsPermission
import rikka.shizuku.Shizuku

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val filteredApps by mainViewModel.filteredApps.collectAsState()


    // Check if accessibility service is enabled
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showShizukuDialog by remember { mutableStateOf(false) }
    var showUsageStatsDialog by remember { mutableStateOf(false) }
    var showAntiUninstallAccessibilityDialog by remember { mutableStateOf(false) }
    var showAntiUninstallDeviceAdminDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val appLockRepository = context.appLockRepository()

        val selectedBackend = appLockRepository.getBackendImplementation()

        val dpm =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(context, DeviceAdmin::class.java)

        if (appLockRepository.isAntiUninstallEnabled()) {
            Log.d("MainScreen", "Anti-uninstall is enabled")
            if (!context.isAccessibilityServiceEnabled()) {
                showAntiUninstallAccessibilityDialog = true
            } else if (!dpm.isAdminActive(component)) {
                showAntiUninstallDeviceAdminDialog = true
            }
        }

        when (selectedBackend) {
            BackendImplementation.ACCESSIBILITY -> {
                if (!context.isAccessibilityServiceEnabled()) {
                    showAccessibilityDialog = true
                }
            }

            BackendImplementation.USAGE_STATS -> {
                if (!context.hasUsagePermission()) {
                    showUsageStatsDialog = true
                }
            }

            BackendImplementation.SHIZUKU -> {
                try {
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_DENIED) {
                        showShizukuDialog = true
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "Shizuku is not available.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Show accessibility service guide dialog if needed
    if (showAccessibilityDialog && !showAntiUninstallAccessibilityDialog && !showAntiUninstallDeviceAdminDialog && !context.isAccessibilityServiceEnabled()) {
        AccessibilityServiceGuideDialog(
            onOpenSettings = {
                openAccessibilitySettings(context)
                showAccessibilityDialog = false
            },
            onDismiss = {
                showAccessibilityDialog = false
            }
        )
    }

    if (showShizukuDialog && !showAntiUninstallAccessibilityDialog && !showAntiUninstallDeviceAdminDialog && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_DENIED) {
        ShizukuPermissionDialog(
            onOpenSettings = {
                try {
                    if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                        Toast.makeText(
                            context,
                            "Grant Shizuku permission manually",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showShizukuDialog = false

                        Shizuku.requestPermission(423)
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "Shizuku is not available.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDismiss = {
                showShizukuDialog = false
            }
        )
    }

    if (showUsageStatsDialog && !showAntiUninstallAccessibilityDialog && !showAntiUninstallDeviceAdminDialog && !context.hasUsagePermission()) {
        UsageStatsPermission(
            onOpenSettings = {
                context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                showUsageStatsDialog = false
            },
            onDismiss = {
                showUsageStatsDialog = false
            }
        )
    }

    if (showAntiUninstallAccessibilityDialog) {
        AntiUninstallAccessibilityPermissionDialog(
            onOpenSettings = {
                openAccessibilitySettings(context)
                showAntiUninstallAccessibilityDialog = false
            },
            onDismiss = {
                showAntiUninstallAccessibilityDialog = false
            }
        )
    }

    if (showAntiUninstallDeviceAdminDialog) {
        AntiUninstallAccessibilityPermissionDialog(
            onOpenSettings = {
                val component = ComponentName(context, DeviceAdmin::class.java)
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "App Lock requires Device Admin permission to prevent removal."
                    )
                }
                context.startActivity(intent)
                showAntiUninstallDeviceAdminDialog = false
            },
            onDismiss = {
                showAntiUninstallDeviceAdminDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Lock",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val focusManager = LocalFocusManager.current

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading ...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AppList(
                    apps = filteredApps,
                    context = context,
                    onAppClick = { appInfo, isChecked ->
                        mainViewModel.toggleAppLock(appInfo, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun AppList(
    apps: List<ApplicationInfo>,
    context: Context,
    onAppClick: (ApplicationInfo, Boolean) -> Unit
) {
    val viewModel = viewModel<MainViewModel>()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            apps.size,
        ) { index ->
            val appInfo = apps[index]
            AppItem(
                appInfo = appInfo,
                context = context,
                viewModel = viewModel,
                onClick = { isChecked ->
                    onAppClick(appInfo, isChecked)
                }
            )
            if (index < apps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppItem(
    appInfo: ApplicationInfo,
    context: Context,
    viewModel: MainViewModel,
    onClick: (Boolean) -> Unit
) {
    val packageManager = context.packageManager
    val appName = remember(appInfo) { appInfo.loadLabel(packageManager).toString() }
    val icon = remember(appInfo) { appInfo.loadIcon(packageManager)?.toBitmap()?.asImageBitmap() }

    val isChecked = remember(appInfo) {
        mutableStateOf(viewModel.isAppLocked(appInfo.packageName))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isChecked.value = !isChecked.value
                onClick(isChecked.value)
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            bitmap = icon ?: ImageBitmap.imageResource(R.drawable.ic_notification),
            contentDescription = appName,
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
        )

        Text(
            text = appName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = isChecked.value,
            onCheckedChange = { isCheckedValue ->
                isChecked.value = isCheckedValue
                onClick(isCheckedValue)
            },
        )
    }
}
