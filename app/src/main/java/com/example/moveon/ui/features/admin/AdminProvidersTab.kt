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
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.moveon.domain.model.Provider
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
import java.util.Locale

@Composable
fun AdminProvidersTab(
    state: AdminDashboardUiState,
    onSave: (Provider) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogProvider by remember { mutableStateOf<Provider?>(null) }
    var dialogEditable by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<Provider?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AdminSectionHeader(
                title = "Providers",
                subtitle = "${state.providers.size} businesses • Manage transport providers"
            )
        }

        if (state.providersError != null) {
            item { Text(state.providersError, color = ErrorDeep) }
        }

        if (state.isLoadingProviders && state.providers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Loading providers...",
                        modifier = Modifier.padding(16.dp),
                        color = LightTextSecondary
                    )
                }
            }
        } else if (state.providers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No providers yet.",
                        modifier = Modifier.padding(16.dp),
                        color = LightTextSecondary
                    )
                }
            }
        } else {
            items(state.providers) { provider ->
                val (chipBg, chipFg) = verifiedChipColors(provider.isVerified)
                AdminEntityCard(
                    initials = providerInitials(provider),
                    title = provider.establishmentName.ifBlank { "Unnamed business" },
                    subtitle = formatProviderSubtitle(provider),
                    chipText = if (provider.isVerified) "Verified" else "Unverified",
                    chipBackground = chipBg,
                    chipForeground = chipFg,
                    avatarTint = Accent,
                    avatarIcon = Icons.Outlined.LocalShipping,
                    onCardClick = {
                        dialogProvider = provider
                        dialogEditable = false
                    },
                    onView = {
                        dialogProvider = provider
                        dialogEditable = false
                    },
                    onEdit = {
                        dialogProvider = provider
                        dialogEditable = true
                    },
                    onDelete = { deleteCandidate = provider }
                )
            }
        }
    }

    dialogProvider?.let { selected ->
        AdminProviderDetailsDialog(
            provider = selected,
            startInEditMode = dialogEditable,
            onDismiss = { dialogProvider = null },
            onSave = {
                onSave(it)
                dialogProvider = null
            }
        )
    }

    deleteCandidate?.let { candidate ->
        ConfirmDeleteProviderDialog(
            provider = candidate,
            onConfirm = {
                onDelete(candidate.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }
}

@Composable
private fun AdminProviderDetailsDialog(
    provider: Provider,
    startInEditMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (Provider) -> Unit
) {
    var editing by remember(provider.id) { mutableStateOf(startInEditMode) }
    var name by remember(provider.id) { mutableStateOf(provider.establishmentName) }
    var lat by remember(provider.id) {
        mutableStateOf(if (provider.businessLat == 0.0) "" else provider.businessLat.toString())
    }
    var lng by remember(provider.id) {
        mutableStateOf(if (provider.businessLng == 0.0) "" else provider.businessLng.toString())
    }
    var rating by remember(provider.id) {
        mutableStateOf(String.format(Locale.US, "%.1f", provider.rating))
    }

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
                    title = provider.establishmentName.ifBlank { "Unnamed business" },
                    subtitle = if (provider.isVerified) "Verified provider" else "Unverified provider",
                    initials = providerInitials(provider),
                    avatarIcon = Icons.Outlined.LocalShipping,
                    accent = Accent
                )

                if (editing) {
                    AdminFormField(
                        label = "Business name",
                        value = name,
                        onValueChange = { name = it }
                    )
                    AdminFormField(
                        label = "Business latitude",
                        value = lat,
                        onValueChange = { lat = it }
                    )
                    AdminFormField(
                        label = "Business longitude",
                        value = lng,
                        onValueChange = { lng = it }
                    )
                    AdminFormField(
                        label = "Rating (0–5)",
                        value = rating,
                        onValueChange = { rating = it }
                    )
                } else {
                    AdminInfoRow(
                        label = "Business name",
                        value = provider.establishmentName,
                        leadingIcon = Icons.Outlined.LocalShipping
                    )
                    AdminInfoRow(
                        label = "Phone",
                        value = provider.phoneNumber.orEmpty(),
                        leadingIcon = Icons.Outlined.Phone
                    )
                    AdminInfoRow(
                        label = "Location",
                        value = formatLatLng(provider.businessLat, provider.businessLng),
                        leadingIcon = Icons.Outlined.LocationOn
                    )
                    AdminInfoRow(
                        label = "Rating",
                        value = String.format(Locale.US, "%.1f / 5", provider.rating),
                        leadingIcon = Icons.Outlined.Star
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
                                    provider.copy(
                                        establishmentName = name.trim(),
                                        businessLat = lat.toDoubleOrNull() ?: provider.businessLat,
                                        businessLng = lng.toDoubleOrNull() ?: provider.businessLng,
                                        rating = rating.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: provider.rating
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
private fun ConfirmDeleteProviderDialog(
    provider: Provider,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete provider?",
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "This will permanently remove ${provider.establishmentName.ifBlank { "this provider" }}. The user account associated with this provider will also be removed. This cannot be undone.",
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

private fun providerInitials(provider: Provider): String {
    val source = provider.establishmentName.ifBlank { provider.id }
    val parts = source.split(" ", limit = 2).filter { it.isNotBlank() }
    val initials = parts.mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("")
    return initials.ifBlank { "P" }
}

private fun formatProviderSubtitle(provider: Provider): String {
    val phone = provider.phoneNumber.orEmpty().ifBlank { "No phone" }
    val location = formatLatLng(provider.businessLat, provider.businessLng)
    return if (location == "—") phone else "$phone • $location"
}

private fun formatLatLng(lat: Double, lng: Double): String {
    if (lat == 0.0 && lng == 0.0) return "—"
    return String.format(Locale.US, "%.4f, %.4f", lat, lng)
}

private fun verifiedChipColors(isVerified: Boolean): Pair<Color, Color> {
    return if (isVerified) Success.copy(alpha = 0.16f) to Success
    else Accent.copy(alpha = 0.14f) to Accent
}
