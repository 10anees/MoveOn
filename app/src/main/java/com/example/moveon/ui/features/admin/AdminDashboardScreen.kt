package com.example.moveon.ui.features.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moveon.ui.components.AdminBottomBar
import com.example.moveon.ui.components.AdminDashboardTab
import com.example.moveon.ui.navigation.NAV_SELECT_ADMIN_TAB
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightTextPrimary
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun AdminDashboardScreen(
    onOpenSettings: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    tabSelectionHandle: SavedStateHandle? = null,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var selectedTab by remember { mutableStateOf(AdminDashboardTab.Users) }

    val pendingTabFlow = remember(tabSelectionHandle) {
        tabSelectionHandle?.getStateFlow<String?>(NAV_SELECT_ADMIN_TAB, null)
            ?: MutableStateFlow<String?>(null)
    }
    val pendingTabKey by pendingTabFlow.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(pendingTabKey, tabSelectionHandle) {
        val key = pendingTabKey ?: return@LaunchedEffect
        val handle = tabSelectionHandle ?: return@LaunchedEffect
        selectedTab = when (key) {
            "users" -> AdminDashboardTab.Users
            "providers" -> AdminDashboardTab.Providers
            "profile" -> AdminDashboardTab.Profile
            else -> return@LaunchedEffect
        }
        handle.remove<String>(NAV_SELECT_ADMIN_TAB)
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            AdminBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        if (!state.isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "You do not have access to the admin panel.",
                    color = LightTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AdminDashboardTab.Users -> AdminUsersTab(
                    state = state,
                    onSave = viewModel::updateUser,
                    onDelete = viewModel::deleteUser
                )
                AdminDashboardTab.Providers -> AdminProvidersTab(
                    state = state,
                    onSave = viewModel::updateProvider,
                    onDelete = viewModel::deleteProvider
                )
                AdminDashboardTab.Profile -> AdminProfileTab(
                    state = state,
                    onOpenSettings = onOpenSettings,
                    onLogout = {
                        viewModel.logout()
                        onLoggedOut()
                    }
                )
            }
        }
    }
}
