package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.LocationSelectorDialog
import com.example.ui.components.RedeemDialog
import com.example.ui.components.ReminderDialog
import com.example.ui.screens.AiScoutScreen
import com.example.ui.screens.AuthProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OfferDetailScreen
import com.example.ui.screens.PatternInsightsScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.OfferViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: OfferViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) } // Default bright, vibrant, appealing catchy theme
            MyApplicationTheme(darkTheme = isDarkTheme) {
                OfferScoutApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun OfferScoutApp(
    viewModel: OfferViewModel,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedOffer by viewModel.selectedOffer.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val showLocationDialog by viewModel.showLocationDialog.collectAsState()
    val reminderTargetOffer by viewModel.reminderTargetOffer.collectAsState()
    val redeemTargetOffer by viewModel.redeemTargetOffer.collectAsState()
    val savedIds by viewModel.savedOfferIds.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Request POST_NOTIFICATIONS permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Deal alerts & expiry notifications enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Show user feedback messages with action (e.g. Undo when dismissing offers)
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            val isDismissMsg = msg.contains("Dismissed", ignoreCase = true) || msg.contains("Undo", ignoreCase = true)
            val actionLabel = if (isDismissMsg) "Undo" else null
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = actionLabel,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed && isDismissMsg) {
                viewModel.undoDismiss()
            }
            viewModel.clearUserMessage()
        }
    }

    val reminderOfferIds = remember(reminders) {
        reminders.map { it.offerId }.toSet()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedOffer == null) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Explore
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.LocalOffer else Icons.Outlined.LocalOffer,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore") },
                        modifier = Modifier.testTag("nav_explore")
                    )

                    // Wishlist / Saved Deals
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.WISHLIST,
                        onClick = { viewModel.navigateTo(AppScreen.WISHLIST) },
                        icon = {
                            if (savedIds.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                            Text("${savedIds.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.WISHLIST) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Wishlist"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.WISHLIST) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Wishlist"
                                )
                            }
                        },
                        label = { Text("Wishlist") },
                        modifier = Modifier.testTag("nav_wishlist")
                    )

                    // AI Scout
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AI_SCOUT,
                        onClick = { viewModel.navigateTo(AppScreen.AI_SCOUT) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.AI_SCOUT) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Scout"
                            )
                        },
                        label = { Text("AI Scout") },
                        modifier = Modifier.testTag("nav_ai_scout")
                    )

                    // Reminders & Alerts
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.REMINDERS,
                        onClick = { viewModel.navigateTo(AppScreen.REMINDERS) },
                        icon = {
                            if (reminders.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${reminders.size}") } }) {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.REMINDERS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                        contentDescription = "Reminders"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.REMINDERS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Reminders"
                                )
                            }
                        },
                        label = { Text("Alerts") },
                        modifier = Modifier.testTag("nav_alerts")
                    )

                    // Profile
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILE,
                        onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedOffer,
                transitionSpec = {
                    if (targetState != null) {
                        // Navigating from results list into Deal Detailed View:
                        // Smooth slide-in from right with slight vertical lift and soft fade-in
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        ) + slideInVertically(
                            initialOffsetY = { fullHeight -> (fullHeight * 0.05f).toInt() },
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 260)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (-fullWidth * 0.2f).toInt() },
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 200)
                            )
                        )
                    } else {
                        // Returning from Deal Detailed View back to the results list:
                        // Smooth slide-out to right with graceful reveal of the filtered list
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (-fullWidth * 0.2f).toInt() },
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 240)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                            ) + slideOutVertically(
                                targetOffsetY = { fullHeight -> (fullHeight * 0.05f).toInt() },
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 200)
                            )
                        )
                    }
                },
                label = "deal_detail_transition"
            ) { targetOffer ->
                if (targetOffer != null) {
                    BackHandler {
                        viewModel.selectOffer(null)
                    }
                    OfferDetailScreen(
                        offer = targetOffer,
                        isSaved = savedIds.contains(targetOffer.id),
                        hasReminder = reminderOfferIds.contains(targetOffer.id),
                        onBack = { viewModel.selectOffer(null) },
                        onToggleSave = { viewModel.toggleSaveOffer(targetOffer) },
                        onSetReminderClick = { viewModel.openReminderDialog(targetOffer) },
                        onRecordRedeemClick = { viewModel.openRedeemDialog(targetOffer) }
                    )
                } else {
                    if (currentScreen != AppScreen.HOME) {
                        BackHandler {
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    }

                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme
                        )
                        AppScreen.WISHLIST -> WishlistScreen(viewModel = viewModel)
                        AppScreen.AI_SCOUT -> AiScoutScreen(viewModel = viewModel)
                        AppScreen.REMINDERS -> RemindersScreen(viewModel = viewModel)
                        AppScreen.PATTERNS -> PatternInsightsScreen(viewModel = viewModel)
                        AppScreen.PROFILE -> AuthProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Dialogs
    if (showLocationDialog) {
        LocationSelectorDialog(
            currentCountry = filterState.selectedCountry,
            currentCity = filterState.selectedCity,
            onLocationSelected = { country, city ->
                viewModel.updateCountry(country)
                viewModel.updateCity(city)
            },
            onDismiss = { viewModel.setLocationDialog(false) }
        )
    }

    reminderTargetOffer?.let { targetOffer ->
        ReminderDialog(
            offer = targetOffer,
            onConfirm = { alertOption ->
                viewModel.scheduleReminder(targetOffer, alertOption)
            },
            onDismiss = { viewModel.closeReminderDialog() }
        )
    }

    redeemTargetOffer?.let { targetOffer ->
        RedeemDialog(
            offer = targetOffer,
            onConfirm = { savingsAmount ->
                viewModel.confirmRedemption(targetOffer, savingsAmount)
            },
            onDismiss = { viewModel.closeRedeemDialog() }
        )
    }
}
