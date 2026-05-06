package com.example.moveon.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

enum class AdminDashboardTab(
    val label: String,
    val icon: ImageVector
) {
    Users("Users", Icons.Outlined.People),
    Providers("Providers", Icons.Outlined.LocalShipping),
    Profile("Profile", Icons.Outlined.PersonOutline)
}

@Composable
fun AdminBottomBar(
    selectedTab: AdminDashboardTab,
    onTabSelected: (AdminDashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(LightSurface)
    ) {
        HorizontalDivider(color = LightBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminDashboardTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val color = if (selected) Primary else LightTextSecondary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(4.dp)
                                .background(Primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = LightTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
        }
    }
}

/**
 * Card that mirrors the inventory UnpackedBoxCard / SavedAddressCard layout used elsewhere
 * (LightSurface, 16dp rounded, 1dp LightBorder) — used to render users and providers in the
 * admin panel for visual consistency.
 */
@Composable
fun AdminEntityCard(
    initials: String,
    title: String,
    subtitle: String,
    chipText: String,
    chipBackground: Color = Primary.copy(alpha = 0.12f),
    chipForeground: Color = Primary,
    avatarTint: Color = Primary,
    avatarIcon: ImageVector? = null,
    onCardClick: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(avatarTint.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(1.dp, avatarTint.copy(alpha = 0.18f)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarIcon != null) {
                    Icon(
                        imageVector = avatarIcon,
                        contentDescription = null,
                        tint = avatarTint,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = avatarTint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(chipBackground, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelMedium,
                            color = chipForeground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = LightTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AdminEntityActionsMenu(
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    onView = {
                        menuExpanded = false
                        onView()
                    },
                    onEdit = {
                        menuExpanded = false
                        onEdit()
                    },
                    onDelete = onDelete?.let { del ->
                        {
                            menuExpanded = false
                            del()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Three-dot menu modeled exactly after inventory's BoxOptionsMenu so that the dropdown
 * looks and behaves the same throughout the app.
 */
@Composable
private fun AdminEntityActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = LightSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LightBorder),
        shadowElevation = 6.dp
    ) {
        AdminMenuActionRow(
            text = "View Details",
            icon = Icons.Outlined.Visibility,
            tint = LightTextPrimary,
            onClick = onView
        )
        AdminMenuActionRow(
            text = "Edit Information",
            icon = Icons.Outlined.Edit,
            tint = LightTextPrimary,
            onClick = onEdit
        )
        if (onDelete != null) {
            AdminMenuActionRow(
                text = "Delete",
                icon = Icons.Outlined.DeleteOutline,
                tint = ErrorDeep,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun AdminMenuActionRow(
    text: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = tint
        )
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
fun AdminGreetingHeader(
    greeting: String,
    name: String,
    initials: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = LightTextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotBlank()) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = Primary
                )
            }
        }
    }
}
