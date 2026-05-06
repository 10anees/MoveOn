package com.example.moveon.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.moveon.domain.model.UserRole
import com.example.moveon.ui.navigation.navAdminShellArgument
import com.example.moveon.ui.navigation.navChromeArguments
import com.example.moveon.ui.navigation.navProviderShellArgument
import com.example.moveon.ui.components.AdminDashboardTab
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.navigation.NAV_ADMIN_SHELL
import com.example.moveon.ui.navigation.NAV_PROVIDER_SHELL
import com.example.moveon.ui.navigation.NAV_SELECT_ADMIN_TAB
import com.example.moveon.ui.navigation.NavRoutes
import com.example.moveon.ui.navigation.appSettingsRoute
import com.example.moveon.ui.navigation.newPasswordRoute
import com.example.moveon.ui.navigation.securityOtpRoute
import com.example.moveon.ui.navigation.securityRoute
import com.example.moveon.ui.navigation.securityUpdatedRoute
import com.example.moveon.ui.navigation.verifyIdentityRoute
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.components.PlaceholderFutureScreen
import com.example.moveon.ui.Screen
import com.example.moveon.ui.features.auth.AuthEvent
import com.example.moveon.ui.features.auth.AuthFlowViewModel
import com.example.moveon.ui.features.auth.AuthViewModel
import com.example.moveon.ui.features.auth.LoginScreen
import com.example.moveon.ui.features.auth.ProviderSetupStepOneScreen
import com.example.moveon.ui.features.auth.ProviderSetupStepThreeScreen
import com.example.moveon.ui.features.auth.ProviderSetupStepTwoScreen
import com.example.moveon.ui.features.auth.RoleChooseScreen
import com.example.moveon.ui.features.auth.SignUpScreen
import com.example.moveon.ui.features.admin.AdminDashboardScreen
import com.example.moveon.ui.features.book.BookScreen
import com.example.moveon.ui.features.book.TrackBookingScreen
import com.example.moveon.ui.features.home.HomeScreen
import com.example.moveon.ui.features.inventory.AddItemCameraScreen
import com.example.moveon.ui.features.inventory.BoxItemsScreen
import com.example.moveon.ui.features.inventory.InventoryScreen
import com.example.moveon.ui.features.inventory.ScanBoxScreen
import com.example.moveon.ui.features.onboarding.OnboardingScreen
import com.example.moveon.ui.features.provider.ProviderDashboardScreen
import com.example.moveon.ui.features.profile.ProfileScreen
import com.example.moveon.ui.features.profile.EditProfileScreen
import com.example.moveon.ui.features.profile.MoveHistoryScreen
import com.example.moveon.ui.features.profile.SavedAddressesScreen
import com.example.moveon.ui.features.settings.PasswordUpdatedScreen
import com.example.moveon.ui.features.settings.SecurityOtpScreen
import com.example.moveon.ui.features.settings.SecurityScreen
import com.example.moveon.ui.features.settings.SettingsScreen
import com.example.moveon.ui.features.settings.AppSettingsScreen
import com.example.moveon.ui.features.settings.NewPasswordScreen
import com.example.moveon.ui.features.settings.VerifyIdentityScreen
import com.example.moveon.ui.features.settings.PasswordUpdateViewModel
import com.example.moveon.ui.features.splash.SplashScreen
import com.example.moveon.ui.theme.MoveOnTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoveOnTheme {
                val navController = rememberNavController()
                val authFlowViewModel: AuthFlowViewModel = viewModel()

                val onProviderDashboardTab: (ProviderDashboardTab) -> Unit = { tab ->
                    when (tab) {
                        ProviderDashboardTab.Dashboard,
                        ProviderDashboardTab.Fleet,
                        ProviderDashboardTab.Drivers -> {
                            navController.navigate(Screen.ProviderDashboard.route) {
                                launchSingleTop = true
                            }
                        }
                        ProviderDashboardTab.Profile -> Unit
                    }
                }

                val onAdminChromeTab: (AdminDashboardTab) -> Unit = { tab ->
                    val key = when (tab) {
                        AdminDashboardTab.Users -> "users"
                        AdminDashboardTab.Providers -> "providers"
                        AdminDashboardTab.Profile -> "profile"
                    }
                    kotlin.runCatching {
                        navController.getBackStackEntry(Screen.AdminDashboard.route)
                            .savedStateHandle[NAV_SELECT_ADMIN_TAB] = key
                    }
                    navController.popBackStack(Screen.AdminDashboard.route, inclusive = false)
                }

                val onTabSelected: (DashboardTab) -> Unit = { tab ->
                    val route = tab.route
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Splash Route
                    composable(Screen.Splash.route) {
                        val authViewModel: AuthViewModel = hiltViewModel()

                        SplashScreen(
                            onResolveSession = {
                                val role = withTimeoutOrNull(4000L) {
                                    authViewModel.currentUser
                                        .filterNotNull()
                                        .first()
                                        .role
                                }

                                val targetRoute = when {
                                    role == UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                    role == UserRole.ADMIN -> Screen.AdminDashboard.route
                                    authViewModel.isUserLoggedIn() -> Screen.Home.route
                                    else -> Screen.Onboarding.route
                                }

                                navController.navigate(targetRoute) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Onboarding Route
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onGetStarted = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. Login Route
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onNavigateToHome = { role ->
                                val destination = when (role) {
                                    UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                    UserRole.ADMIN -> Screen.AdminDashboard.route
                                    else -> Screen.Home.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.SignUp.route)
                            },
                            onForgotPassword = { email ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("forgot_email", email)
                                navController.navigate(verifyIdentityRoute(adminShell = false, providerShell = false)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 4. Sign Up Route
                    composable(Screen.SignUp.route) {
                        SignUpScreen(
                            flowViewModel = authFlowViewModel,
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            },
                            onNavigateToRoleChoose = { navController.navigate(Screen.RoleChoose.route) },
                            onNavigateToHome = { role ->
                                val destination = when (role) {
                                    UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                    UserRole.ADMIN -> Screen.AdminDashboard.route
                                    else -> Screen.Home.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 5. Role Choose Route
                    composable(Screen.RoleChoose.route) {
                        val authViewModel: AuthViewModel = hiltViewModel()
                        val authState by authViewModel.authState
                        val snackbarHostState = remember { SnackbarHostState() }

                        LaunchedEffect(Unit) {
                            authViewModel.eventFlow.collect { event ->
                                when (event) {
                                    is AuthViewModel.UiEvent.NavigateToHome -> {
                                        val destination = when (event.role) {
                                            UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                            UserRole.ADMIN -> Screen.AdminDashboard.route
                                            else -> Screen.Home.route
                                        }
                                        navController.navigate(destination) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                        authFlowViewModel.reset()
                                    }
                                    is AuthViewModel.UiEvent.ShowSnackbar -> {
                                        snackbarHostState.showSnackbar(event.message)
                                    }
                                    else -> Unit
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            RoleChooseScreen(
                                selectedRole = authFlowViewModel.selectedRole,
                                onRoleSelected = { authFlowViewModel.selectedRole = it },
                                onBack = { navController.popBackStack() },
                                onNext = {
                                    when (authFlowViewModel.selectedRole) {
                                        UserRole.USER -> {
                                            val (firstName, lastName) = authFlowViewModel.splitFirstAndLastName()
                                            authViewModel.onEvent(
                                                AuthEvent.RegisterUser(
                                                    email = authFlowViewModel.email,
                                                    password = authFlowViewModel.password,
                                                    fName = firstName,
                                                    lName = lastName,
                                                    pNumber = authFlowViewModel.phoneNumber
                                                )
                                            )
                                        }
                                        UserRole.PROVIDER -> navController.navigate(Screen.ProviderSetupStepOne.route)
                                        else -> Unit
                                    }
                                }
                            )

                            if (authState is AuthViewModel.AuthState.Error) {
                                Text(
                                    text = (authState as AuthViewModel.AuthState.Error).message,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 86.dp)
                                )
                            }

                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }

                    composable(Screen.ProviderSetupStepOne.route) {
                        ProviderSetupStepOneScreen(
                            flowViewModel = authFlowViewModel,
                            onNext = { navController.navigate(Screen.ProviderSetupStepTwo.route) }
                        )
                    }

                    composable(Screen.ProviderSetupStepTwo.route) {
                        ProviderSetupStepTwoScreen(
                            flowViewModel = authFlowViewModel,
                            onNext = { navController.navigate(Screen.ProviderSetupStepThree.route) }
                        )
                    }

                    composable(Screen.ProviderSetupStepThree.route) {
                        val authViewModel: AuthViewModel = hiltViewModel()
                        val authState by authViewModel.authState

                        LaunchedEffect(Unit) {
                            authViewModel.eventFlow.collect { event ->
                                when (event) {
                                    is AuthViewModel.UiEvent.NavigateToHome -> {
                                        val destination = when (event.role) {
                                            UserRole.PROVIDER -> Screen.ProviderDashboard.route
                                            UserRole.ADMIN -> Screen.AdminDashboard.route
                                            else -> Screen.Home.route
                                        }
                                        navController.navigate(destination) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                        authFlowViewModel.reset()
                                    }
                                    else -> Unit
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            ProviderSetupStepThreeScreen(
                                flowViewModel = authFlowViewModel,
                                isLoading = authState is AuthViewModel.AuthState.Loading,
                                onRegister = {
                                    val (firstName, lastName) = authFlowViewModel.splitFirstAndLastName()
                                    authViewModel.onEvent(
                                        AuthEvent.RegisterProvider(
                                            email = authFlowViewModel.email,
                                            password = authFlowViewModel.password,
                                            fName = firstName,
                                            lName = lastName,
                                            pNumber = authFlowViewModel.phoneNumber,
                                            establishmentName = authFlowViewModel.businessName,
                                            businessLat = authFlowViewModel.businessLat ?: 0.0,
                                            businessLng = authFlowViewModel.businessLng ?: 0.0,
                                            vehicleMake = authFlowViewModel.vehicleMake,
                                            vehicleModel = authFlowViewModel.vehicleModel,
                                            vehicleYear = authFlowViewModel.vehicleYear,
                                            vehicleColor = authFlowViewModel.vehicleColor,
                                            plateNumber = authFlowViewModel.plateNumber,
                                            maxCapacityKg = authFlowViewModel.maxCapacityKg.toDoubleOrNull() ?: 0.0,
                                            maxVolumeM3 = authFlowViewModel.maxVolumeM3.toDoubleOrNull() ?: 0.0,
                                            baseRate = authFlowViewModel.baseRate.toDoubleOrNull() ?: 0.0,
                                            ratePerKm = authFlowViewModel.ratePerKm.toDoubleOrNull() ?: 0.0
                                        )
                                    )
                                }
                            )

                            if (authState is AuthViewModel.AuthState.Error) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text((authState as AuthViewModel.AuthState.Error).message)
                                }
                            }
                        }
                    }

                    composable(Screen.ProviderDashboard.route) {
                        ProviderDashboardScreen(
                            onOpenProfile = {
                                navController.navigate(Screen.ProviderProfile.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Screen.AdminDashboard.route) { adminEntry ->
                        AdminDashboardScreen(
                            tabSelectionHandle = adminEntry.savedStateHandle,
                            onOpenSettings = {
                                navController.navigate(appSettingsRoute(adminShell = true)) {
                                    launchSingleTop = true
                                }
                            },
                            onLoggedOut = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }

                    // 6. Home Route
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onTabSelected = onTabSelected,
                            onManageInventoryClick = {
                                onTabSelected(DashboardTab.Inventory)
                            },
                            onScanBoxClick = {
                                navController.navigate(Screen.ScanBox.route)
                            }
                            ,
                            onTrackDriverClick = {
                                navController.navigate(Screen.Book.route)
                            }
                            ,
                            onOpenMoveHistory = {
                                navController.navigate(Screen.MoveHistory.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 7. Book Route (placeholder for future implementation)
                    composable(Screen.Book.route) {
                        BookScreen(onTabSelected = onTabSelected)
                    }

                    // 8. Inventory Route
                    composable(Screen.Inventory.route) {
                        InventoryScreen(
                            onTabSelected = onTabSelected,
                            onScanBoxClick = {
                                navController.navigate(Screen.ScanBox.route)
                            },
                            onAddItemsClick = { boxUuid, boxId ->
                                navController.navigate(
                                    Screen.AddItemCamera.createRoute(
                                        boxUuid = boxUuid,
                                        boxId = boxId
                                    )
                                )
                            },
                            onBoxClick = { boxUuid, scannedFromQr ->
                                navController.navigate(
                                    Screen.BoxItems.createRoute(
                                        boxUuid = boxUuid,
                                        scannedFromQr = scannedFromQr
                                    )
                                )
                            }
                        )
                    }

                    composable(Screen.ScanBox.route) {
                        ScanBoxScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onScanned = { scannedBoxUuid ->
                                navController.navigate(
                                    Screen.BoxItems.createRoute(
                                        boxUuid = scannedBoxUuid,
                                        scannedFromQr = true
                                    )
                                ) {
                                    popUpTo(Screen.ScanBox.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Screen.BoxItems.route) { backStackEntry ->
                        val boxUuid = backStackEntry.arguments?.getString("boxUuid").orEmpty()
                        val scannedFromQr = backStackEntry.arguments
                            ?.getString("scannedFromQr")
                            ?.toBooleanStrictOrNull()
                            ?: false

                        BoxItemsScreen(
                            boxUuid = boxUuid,
                            scannedFromQr = scannedFromQr,
                            onBack = { navController.popBackStack() },
                            onAddItem = { selectedBoxUuid, selectedBoxId ->
                                navController.navigate(
                                    Screen.AddItemCamera.createRoute(
                                        boxUuid = selectedBoxUuid,
                                        boxId = selectedBoxId
                                    )
                                )
                            },
                            onScanAnotherBox = {
                                navController.navigate(Screen.ScanBox.route)
                            }
                        )
                    }

                    composable(Screen.AddItemCamera.route) { backStackEntry ->
                        val boxUuid = backStackEntry.arguments?.getString("boxUuid").orEmpty()
                        val boxId = backStackEntry.arguments?.getString("boxId").orEmpty()

                        AddItemCameraScreen(
                            boxUuid = boxUuid,
                            boxId = boxId,
                            onBack = { navController.popBackStack() },
                            onItemSaved = { navController.popBackStack() }
                        )
                    }

                    // 9. Profile Route
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onTabSelected = onTabSelected,
                            onOpenSettings = {
                                navController.navigate(appSettingsRoute(adminShell = false, providerShell = false)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenEditProfile = {
                                navController.navigate(Screen.EditProfile.route) { launchSingleTop = true }
                            },
                            onOpenSavedAddresses = {
                                navController.navigate(Screen.SavedAddresses.route) { launchSingleTop = true }
                            },
                            onOpenMoveHistory = {
                                navController.navigate(Screen.MoveHistory.route) { launchSingleTop = true }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }

                    composable(Screen.ProviderProfile.route) {
                        ProfileScreen(
                            onTabSelected = onTabSelected,
                            onOpenSettings = {
                                navController.navigate(appSettingsRoute(adminShell = false, providerShell = true)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenEditProfile = {
                                navController.navigate(Screen.EditProfile.route) { launchSingleTop = true }
                            },
                            onOpenSavedAddresses = {
                                navController.navigate(Screen.SavedAddresses.route) { launchSingleTop = true }
                            },
                            onOpenMoveHistory = {
                                navController.navigate(Screen.MoveHistory.route) { launchSingleTop = true }
                            },
                            isProviderMode = true,
                            onProviderTabSelected = onProviderDashboardTab,
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        )
                    }

                    composable(Screen.EditProfile.route) {
                        EditProfileScreen(
                            onBack = { navController.popBackStack() },
                            onTabSelected = onTabSelected
                        )
                    }

                    composable(Screen.SavedAddresses.route) {
                        SavedAddressesScreen(
                            onBack = { navController.popBackStack() },
                            onTabSelected = onTabSelected
                        )
                    }

                    composable(Screen.MoveHistory.route) {
                        MoveHistoryScreen(
                            onClose = { navController.popBackStack() },
                            onViewDetails = { bookingId ->
                                navController.navigate(Screen.TrackBooking.createRoute(bookingId)) {
                                    launchSingleTop = true
                                }
                            },
                            onTabSelected = onTabSelected
                        )
                    }

                    composable(Screen.TrackBooking.route) { backStackEntry ->
                        val bookingId = backStackEntry.arguments?.getString("bookingId").orEmpty()
                        TrackBookingScreen(
                            bookingId = bookingId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onTabSelected = onTabSelected,
                            onOpenSecurity = {
                                navController.navigate(securityRoute(adminShell = false, providerShell = false)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenAppSettings = {
                                navController.navigate(appSettingsRoute(adminShell = false, providerShell = false)) {
                                    launchSingleTop = true
                                }
                            },
                            isAdminShell = false,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }

                    composable(
                        route = NavRoutes.APP_SETTINGS,
                        arguments = navChromeArguments()
                    ) { appSettingsEntry ->
                        val adminShell = appSettingsEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = appSettingsEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false
                        AppSettingsScreen(
                            onBack = { navController.popBackStack() },
                            onTabSelected = onTabSelected,
                            onOpenSecurity = {
                                navController.navigate(
                                    securityRoute(adminShell = adminShell, providerShell = providerShell)
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }

                    composable(
                        route = NavRoutes.SECURITY,
                        arguments = navChromeArguments()
                    ) { secEntry ->
                        val adminShell = secEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = secEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false
                        SecurityScreen(
                            onBack = { navController.popBackStack() },
                            onOpenOtp = {
                                navController.navigate(
                                    verifyIdentityRoute(adminShell = adminShell, providerShell = providerShell)
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            onTabSelected = onTabSelected,
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }

                    composable(
                        route = NavRoutes.VERIFY_IDENTITY,
                        arguments = navChromeArguments()
                    ) { verifyEntry ->
                        val adminShell = verifyEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = verifyEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false
                        val forgotEmail = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("forgot_email")
                        val passwordUpdateViewModel: PasswordUpdateViewModel = hiltViewModel()
                        val context = LocalContext.current

                        LaunchedEffect(Unit) {
                            passwordUpdateViewModel.eventFlow.collect { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        VerifyIdentityScreen(
                            onBack = { navController.popBackStack() },
                            onSelectMethod = { method ->
                                if (!forgotEmail.isNullOrBlank() && method == "email") {
                                    passwordUpdateViewModel.sendPasswordResetEmail(forgotEmail)
                                }
                                navController.navigate(
                                    securityOtpRoute(method, adminShell = adminShell, providerShell = providerShell)
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            onTabSelected = onTabSelected,
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab,
                            prefilledEmail = forgotEmail
                        )
                    }

                    composable(
                        route = NavRoutes.SECURITY_OTP,
                        arguments = listOf(
                            navArgument("method") { type = NavType.StringType },
                            navAdminShellArgument(),
                            navProviderShellArgument()
                        )
                    ) { otpEntry ->
                        val method = otpEntry.arguments?.getString("method").orEmpty()
                        val adminShell = otpEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = otpEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false

                        SecurityOtpScreen(
                            verificationMethod = method,
                            onBack = { navController.popBackStack() },
                            onVerify = {
                                navController.navigate(
                                    newPasswordRoute(adminShell = adminShell, providerShell = providerShell)
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            onTabSelected = onTabSelected,
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }

                    composable(
                        route = NavRoutes.NEW_PASSWORD,
                        arguments = navChromeArguments()
                    ) { npEntry ->
                        val adminShell = npEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = npEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false

                        val verifyRoute =
                            verifyIdentityRoute(adminShell = adminShell, providerShell = providerShell)
                        val forgotEmail = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>("forgot_email")
                            ?: runCatching {
                                navController.getBackStackEntry(verifyRoute).savedStateHandle
                                    .get<String>("forgot_email")
                            }.getOrNull()

                        NewPasswordScreen(
                            onBack = { navController.popBackStack() },
                            onPasswordChanged = {
                                navController.navigate(
                                    securityUpdatedRoute(adminShell = adminShell, providerShell = providerShell)
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            forgotPasswordEmail = forgotEmail,
                            onTabSelected = onTabSelected,
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }

                    composable(
                        route = NavRoutes.SECURITY_UPDATED,
                        arguments = navChromeArguments()
                    ) { updatedEntry ->
                        val adminShell = updatedEntry.arguments?.getBoolean(NAV_ADMIN_SHELL) ?: false
                        val providerShell = updatedEntry.arguments?.getBoolean(NAV_PROVIDER_SHELL) ?: false

                        PasswordUpdatedScreen(
                            onVerifyAgain = {
                                navController.popBackStack(
                                    securityRoute(adminShell = adminShell, providerShell = providerShell),
                                    inclusive = false
                                )
                            },
                            onGoToSettings = {
                                navController.popBackStack(
                                    appSettingsRoute(adminShell = adminShell, providerShell = providerShell),
                                    inclusive = false
                                )
                            },
                            onTabSelected = onTabSelected,
                            isProviderMode = providerShell,
                            onProviderTabSelected = onProviderDashboardTab,
                            isAdminShell = adminShell,
                            onAdminTabSelected = onAdminChromeTab
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomTabPlaceholderScreen(
    title: String,
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            MoveOnBottomBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PlaceholderFutureScreen(
                title = title,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}