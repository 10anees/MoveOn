package com.example.moveon.ui.components

import androidx.compose.runtime.Composable

@Composable
fun SettingsChromeBottomBar(
    isAdminShell: Boolean,
    isProviderMode: Boolean,
    dashboardTab: DashboardTab,
    providerTab: ProviderDashboardTab,
    adminTab: AdminDashboardTab,
    onUserTabSelected: (DashboardTab) -> Unit,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit,
    onAdminTabSelected: (AdminDashboardTab) -> Unit
) {
    when {
        isAdminShell -> AdminBottomBar(
            selectedTab = adminTab,
            onTabSelected = onAdminTabSelected
        )

        isProviderMode -> ProviderBottomBar(
            selectedTab = providerTab,
            onTabSelected = onProviderTabSelected
        )

        else -> MoveOnBottomBar(
            selectedTab = dashboardTab,
            onTabSelected = onUserTabSelected
        )
    }
}
