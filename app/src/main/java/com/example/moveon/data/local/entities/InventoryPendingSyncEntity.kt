package com.example.moveon.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_sync_queue",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "box_uuid"])
    ]
)
data class InventoryPendingSyncEntity(
    @PrimaryKey val id: String,
    val user_id: String,
    /** DELETE, PATCH, PACKED */
    val op: String,
    val box_uuid: String,
    val box_id: String?,
    val category: String?,
    val label: String?,
    val color_hex: String?,
    val packed_value: Int?,
    val created_at_utc: Long
)
