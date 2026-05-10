package com.example.moveon.ui.features.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.components.MoveOnProfileActionRowItem
import com.example.moveon.ui.components.MoveOnProfileHeaderCard
import com.example.moveon.ui.components.MoveOnStatCard
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminProfileTab(
    state: AdminDashboardUiState,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val admin = state.adminUser
    val displayName = listOfNotNull(admin?.firstName, admin?.lastName)
        .joinToString(" ")
        .trim()
        .ifBlank { "Admin" }
    val initials = (admin?.firstName.orEmpty().firstOrNull()?.uppercaseChar()?.toString().orEmpty() +
        admin?.lastName.orEmpty().firstOrNull()?.uppercaseChar()?.toString().orEmpty())
        .ifBlank { "A" }
    val email = admin?.email.orEmpty().ifBlank { "No email available" }
    val memberSinceDate = admin?.createdAt
        ?.takeIf { it > 0L }
        ?.let { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it)) }
        ?: "—"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        MoveOnProfileHeaderCard(
            name = displayName,
            email = email,
            photoUrl = null,
            initials = initials,
            memberSinceDate = memberSinceDate
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoveOnStatCard(
                value = state.users.size.toString(),
                label = "Users",
                modifier = Modifier.weight(1f)
            )
            MoveOnStatCard(
                value = state.providers.size.toString(),
                label = "Providers",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LightTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                MoveOnProfileActionRowItem(
                    title = "App Settings",
                    leadingIcon = Icons.Outlined.Settings,
                    onClick = onOpenSettings,
                    showDivider = false
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(LightBackground, shape = RoundedCornerShape(20.dp))
                .border(BorderStroke(1.117.dp, ErrorDeep), shape = RoundedCornerShape(20.dp))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    color = ErrorDeep,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
