package com.example.moveon.ui.features.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

/**
 * Reusable detail-dialog header — gradient avatar + name/subtitle, mirroring the
 * "ItemInfoDialog" + "MoveOnProfileHeaderCard" hybrid used elsewhere in the app
 * so admin overlays feel native.
 */
@Composable
internal fun AdminDialogHeader(
    title: String,
    subtitle: String,
    initials: String,
    avatarIcon: ImageVector? = null,
    accent: Color = Primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarIcon != null) {
                Icon(
                    imageVector = avatarIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column {
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
        }
    }
}

@Composable
internal fun AdminFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = LightTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            supportingText = if (error == null) null else {
                { Text(error, color = ErrorDeep) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightSurfaceVariant,
                unfocusedContainerColor = LightSurfaceVariant,
                disabledContainerColor = LightSurfaceVariant,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = ErrorDeep,
                focusedTextColor = LightTextPrimary,
                unfocusedTextColor = LightTextPrimary,
                disabledTextColor = LightTextPrimary.copy(alpha = 0.7f),
                errorTextColor = LightTextPrimary,
                focusedLabelColor = LightTextSecondary,
                unfocusedLabelColor = LightTextSecondary,
                disabledLabelColor = LightTextSecondary.copy(alpha = 0.7f),
                focusedPlaceholderColor = LightTextSecondary,
                unfocusedPlaceholderColor = LightTextSecondary,
                cursorColor = Primary,
                errorSupportingTextColor = ErrorDeep
            )
        )
    }
}

@Composable
internal fun AdminInfoRow(
    label: String,
    value: String,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightSurfaceVariant, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, LightBorder), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = LightTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LightTextSecondary
            )
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

