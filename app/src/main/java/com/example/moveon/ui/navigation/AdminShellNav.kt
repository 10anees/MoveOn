package com.example.moveon.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

const val NAV_ADMIN_SHELL = "admin_shell"
const val NAV_PROVIDER_SHELL = "provider_shell"
const val NAV_SELECT_ADMIN_TAB = "select_admin_tab"

fun navAdminShellArgument() = navArgument(NAV_ADMIN_SHELL) {
    type = NavType.BoolType
    defaultValue = false
}

fun navProviderShellArgument() = navArgument(NAV_PROVIDER_SHELL) {
    type = NavType.BoolType
    defaultValue = false
}

fun navChromeArguments() = listOf(navAdminShellArgument(), navProviderShellArgument())

object NavRoutes {
    const val APP_SETTINGS =
        "app_settings?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
    const val SECURITY =
        "security?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
    const val VERIFY_IDENTITY =
        "verify_identity?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
    const val SECURITY_OTP =
        "security_otp/{method}?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
    const val NEW_PASSWORD =
        "new_password?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
    const val SECURITY_UPDATED =
        "security_updated?${NAV_ADMIN_SHELL}={${NAV_ADMIN_SHELL}}&${NAV_PROVIDER_SHELL}={${NAV_PROVIDER_SHELL}}"
}

fun appSettingsRoute(adminShell: Boolean = false, providerShell: Boolean = false) =
    "app_settings?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"

fun securityRoute(adminShell: Boolean = false, providerShell: Boolean = false) =
    "security?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"

fun verifyIdentityRoute(adminShell: Boolean = false, providerShell: Boolean = false) =
    "verify_identity?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"

fun securityOtpRoute(method: String, adminShell: Boolean = false, providerShell: Boolean = false) =
    "security_otp/$method?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"

fun newPasswordRoute(adminShell: Boolean = false, providerShell: Boolean = false) =
    "new_password?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"

fun securityUpdatedRoute(adminShell: Boolean = false, providerShell: Boolean = false) =
    "security_updated?$NAV_ADMIN_SHELL=$adminShell&$NAV_PROVIDER_SHELL=$providerShell"
