package com.example.moveon.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boxes",
    indices = [Index(value = ["box_id"], unique = true)]
)
data class BoxEntity(
    @PrimaryKey
    val box_uuid: String,
    val box_id: String,
    val booking_id: Int,
    val vehicle_id: Int?,
    val category: String,
    val label: String,
    val volume: Double,
    val packed: Boolean,
    /** Firebase Auth uid of the inventory owner */
    val owner_user_id: String = "",
    /** UI / cloud color for this box */
    val color_hex: String = DEFAULT_BOX_COLOR_HEX,
    /** True when the box exists locally but needs to be uploaded to Firestore */
    val pending_cloud_upload: Boolean = false
) {
    companion object {
        const val DEFAULT_BOX_COLOR_HEX = "#1565C0"
    }
}