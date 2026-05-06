package com.example.moveon.ui.features.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.ui.components.AdminEntityCard
import com.example.moveon.ui.components.AdminSectionHeader
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success

@Composable
fun AdminUsersTab(
    state: AdminDashboardUiState,
    onSave: (User) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogUser by remember { mutableStateOf<User?>(null) }
    var dialogEditable by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<User?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AdminSectionHeader(
                title = "Users",
                subtitle = "${state.users.size} accounts • Manage app users"
            )
        }

        if (state.usersError != null) {
            item { Text(state.usersError, color = ErrorDeep) }
        }

        if (state.isLoadingUsers && state.users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Loading users...",
                        modifier = Modifier.padding(16.dp),
                        color = LightTextSecondary
                    )
                }
            }
        } else if (state.users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No users yet.",
                        modifier = Modifier.padding(16.dp),
                        color = LightTextSecondary
                    )
                }
            }
        } else {
            items(state.users) { user ->
                val (chipBg, chipFg) = roleChipColors(user.role)
                val isSelf = user.id == state.adminUser?.id
                AdminEntityCard(
                    initials = userInitials(user),
                    title = "${user.firstName} ${user.lastName}".trim().ifBlank { "Unnamed" },
                    subtitle = user.email.ifBlank { "No email" },
                    chipText = user.role.name,
                    chipBackground = chipBg,
                    chipForeground = chipFg,
                    avatarTint = chipFg,
                    onCardClick = {
                        dialogUser = user
                        dialogEditable = false
                    },
                    onView = {
                        dialogUser = user
                        dialogEditable = false
                    },
                    onEdit = {
                        dialogUser = user
                        dialogEditable = true
                    },
                    onDelete = if (isSelf) null else {
                        { deleteCandidate = user }
                    }
                )
            }
        }
    }

    dialogUser?.let { selected ->
        AdminUserDetailsDialog(
            user = selected,
            startInEditMode = dialogEditable,
            onDismiss = { dialogUser = null },
            onSave = {
                onSave(it)
                dialogUser = null
            }
        )
    }

    deleteCandidate?.let { candidate ->
        ConfirmDeleteUserDialog(
            user = candidate,
            onConfirm = {
                onDelete(candidate.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }
}

@Composable
private fun AdminUserDetailsDialog(
    user: User,
    startInEditMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var editing by remember(user.id) { mutableStateOf(startInEditMode) }
    var firstName by remember(user.id) { mutableStateOf(user.firstName) }
    var lastName by remember(user.id) { mutableStateOf(user.lastName) }
    var phone by remember(user.id) { mutableStateOf(user.phoneNumber) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdminDialogHeader(
                    title = "${user.firstName} ${user.lastName}".trim().ifBlank { "Unnamed user" },
                    subtitle = user.role.name,
                    initials = userInitials(user),
                    accent = roleChipColors(user.role).second
                )

                if (editing) {
                    AdminFormField(
                        label = "First name",
                        value = firstName,
                        onValueChange = { firstName = it }
                    )
                    AdminFormField(
                        label = "Last name",
                        value = lastName,
                        onValueChange = { lastName = it }
                    )
                    AdminFormField(
                        label = "Phone",
                        value = phone,
                        onValueChange = { phone = it }
                    )
                    AdminFormField(
                        label = "Email",
                        value = user.email,
                        onValueChange = {},
                        enabled = false
                    )
                } else {
                    AdminInfoRow(
                        label = "Full name",
                        value = "${user.firstName} ${user.lastName}".trim(),
                        leadingIcon = Icons.Outlined.PersonOutline
                    )
                    AdminInfoRow(
                        label = "Email",
                        value = user.email,
                        leadingIcon = Icons.Outlined.Email
                    )
                    AdminInfoRow(
                        label = "Phone",
                        value = user.phoneNumber,
                        leadingIcon = Icons.Outlined.Phone
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Text(if (editing) "Cancel" else "Close", color = LightTextPrimary)
                    }
                    MoveOnPillButton(
                        text = if (editing) "Save Changes" else "Edit",
                        onClick = {
                            if (editing) {
                                onSave(
                                    user.copy(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        phoneNumber = phone.trim()
                                    )
                                )
                            } else {
                                editing = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        background = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteUserDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete user?",
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "This will permanently remove ${user.firstName} ${user.lastName} (${user.email}). This cannot be undone.",
                color = LightTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorDeep),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Delete", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Primary) }
        },
        containerColor = LightSurface
    )
}

private fun userInitials(user: User): String {
    val first = user.firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val last = user.lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    return (first + last).ifBlank { "U" }
}

private fun roleChipColors(role: UserRole): Pair<Color, Color> = when (role) {
    UserRole.ADMIN -> Accent.copy(alpha = 0.14f) to Accent
    UserRole.PROVIDER -> Primary.copy(alpha = 0.12f) to Primary
    UserRole.DRIVER -> Success.copy(alpha = 0.16f) to Success
    UserRole.USER -> Primary.copy(alpha = 0.10f) to Primary
}
