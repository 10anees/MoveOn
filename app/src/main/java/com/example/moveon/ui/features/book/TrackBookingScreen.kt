package com.example.moveon.ui.features.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.repository.LogisticsRepository
import com.example.moveon.ui.components.DualMarkerMapPreview
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TrackBookingViewModel @Inject constructor(
    private val repo: LogisticsRepository
) : ViewModel() {
    var state by mutableStateOf(TrackBookingState(isLoading = true))
        private set

    fun load(bookingId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            repo.getBookingById(bookingId)
                .onSuccess { booking ->
                    state = TrackBookingState(isLoading = false, booking = booking)
                }
                .onFailure { e ->
                    state = TrackBookingState(
                        isLoading = false,
                        error = e.message ?: "Unable to load booking."
                    )
                }
        }
    }
}

data class TrackBookingState(
    val isLoading: Boolean = false,
    val booking: Booking? = null,
    val error: String? = null
)

@Composable
fun TrackBookingScreen(
    bookingId: String,
    onBack: () -> Unit,
    viewModel: TrackBookingViewModel = hiltViewModel()
) {
    LaunchedEffect(bookingId) { viewModel.load(bookingId) }
    val state = viewModel.state

    Scaffold(containerColor = LightBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MoveDetailsHeader(
                bookingId = state.booking?.id ?: bookingId,
                onBack = onBack
            )

            when {
                state.isLoading -> MoveDetailsLoading()
                state.error != null -> MoveDetailsError(state.error)
                state.booking != null -> MoveDetailsBody(state.booking)
                else -> MoveDetailsError("Booking not found.")
            }
        }
    }
}

@Composable
private fun MoveDetailsHeader(bookingId: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = LightTextPrimary
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Move Details",
                style = MaterialTheme.typography.headlineSmall,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Booking #${bookingId.takeLast(8).ifBlank { "—" }}",
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MoveDetailsLoading() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Primary
            )
            Text(
                text = "Loading move details...",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary
            )
        }
    }
}

@Composable
private fun MoveDetailsError(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = ErrorDeep,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MoveDetailsBody(booking: Booking) {
    MoveSummaryCard(booking)

    if (booking.pickupLat != 0.0 && booking.dropOffLat != 0.0) {
        DualMarkerMapPreview(
            pickupLat = booking.pickupLat,
            pickupLng = booking.pickupLng,
            dropOffLat = booking.dropOffLat,
            dropOffLng = booking.dropOffLng
        )
    }

    MoveAddressesCard(booking)
    MoveTripInfoCard(booking)
}

@Composable
private fun MoveSummaryCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = formatScheduledDate(booking.scheduledTime),
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = LightTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = formatScheduledTime(booking.scheduledTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        }
                    }
                }
                StatusBadge(booking.status)
            }

            if (booking.rating > 0f) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", booking.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveAddressesCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Route",
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            AddressRow(
                title = "Pickup",
                address = booking.pickupAddress.ifBlank { "—" },
                filled = true
            )
            AddressRow(
                title = "Drop-off",
                address = booking.dropOffAddress.ifBlank { "—" },
                filled = false
            )
        }
    }
}

@Composable
private fun MoveTripInfoCard(booking: Booking) {
    val distanceKm = if (booking.pickupLat != 0.0 && booking.dropOffLat != 0.0) {
        LocationUtils.calculateDistanceKm(
            LatLng(booking.pickupLat, booking.pickupLng),
            LatLng(booking.dropOffLat, booking.dropOffLng)
        ).takeIf { it > 0.0 }
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Trip Information",
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            InfoRow(
                label = "Total Distance",
                value = distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "—"
            )
            InfoRow(
                label = "Total Fare",
                value = formatPkr(booking.totalFare)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (background, foreground, label) = when (status) {
        BookingStatus.COMPLETED -> Triple(Color(0xFF2E7D32), Color.White, "Completed")
        BookingStatus.CONFIRMED -> Triple(Primary, Color.White, "Confirmed")
        BookingStatus.SEARCHING -> Triple(Color(0xFFFFA000), Color.White, "Searching")
        BookingStatus.ACTIVE -> Triple(Primary, Color.White, "Active")
        else -> Triple(
            Color(0xFF757575),
            Color.White,
            status.name.lowercase().replaceFirstChar { it.uppercase() }
        )
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = foreground,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AddressRow(title: String, address: String, filled: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        val dotModifier = Modifier
            .padding(top = 6.dp)
            .size(10.dp)
            .let { base ->
                if (filled) {
                    base.background(Primary, CircleShape)
                } else {
                    base
                        .background(LightSurface, CircleShape)
                        .border(BorderStroke(1.5.dp, Primary), CircleShape)
                }
            }
        Box(modifier = dotModifier)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Straighten,
                contentDescription = null,
                tint = LightTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

private fun formatScheduledDate(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "Date unavailable"
    return runCatching {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(timestampMillis))
    }.getOrDefault("—")
}

private fun formatScheduledTime(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "—"
    return runCatching {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis))
    }.getOrDefault("—")
}

private fun formatPkr(value: Double): String {
    if (value <= 0.0) return "—"
    return "PKR ${DecimalFormat("#,###").format(value)}"
}
