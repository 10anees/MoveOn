package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import kotlinx.coroutines.flow.Flow

const val DEFAULT_INVENTORY_BOX_COLOR_HEX = "#1565C0"

interface InventoryRepository {
    suspend fun addNewBox(
        box: Box,
        ownerUserId: String,
        colorHex: String = DEFAULT_INVENTORY_BOX_COLOR_HEX,
        pendingCloudUpload: Boolean = false
    )

    suspend fun addNewBoxToCloud(box: Box, userId: String, colorHex: String): Result<Unit>

    suspend fun markBoxSyncedToCloud(boxUuid: String)

    suspend fun syncInventoryWithCloud(userId: String): Result<Unit>

    suspend fun enqueuePendingBoxDelete(userId: String, boxUuid: String)

    suspend fun enqueuePendingBoxPatch(
        userId: String,
        boxUuid: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    )

    suspend fun enqueuePendingBoxPacked(userId: String, boxUuid: String, packed: Boolean)

    fun getBoxesForMoveForOwner(bookingId: Int, ownerUserId: String): Flow<List<Box>>

    suspend fun getTotalBoxesCountForOwner(ownerUserId: String): Int
    suspend fun updateBoxPackedStatus(boxUuid: String, isPacked: Boolean)
    suspend fun updateBoxPackedStatusInCloud(
        boxUuid: String,
        userId: String,
        isPacked: Boolean
    ): Result<Unit>
    suspend fun updateBoxInfo(
        boxUuid: String,
        boxId: String,
        category: String,
        label: String
    )
    suspend fun updateBoxInfoInCloud(
        boxUuid: String,
        userId: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    ): Result<Unit>
    suspend fun deleteBox(boxUuid: String)
    suspend fun deleteBoxFromCloud(boxUuid: String, userId: String): Result<Unit>
    fun getBoxesForMove(bookingId: Int): Flow<List<Box>>
    fun getItemsInBox(boxId: String): Flow<List<Item>>
    fun getTotalItemsCount(): Flow<Int>
    fun getTotalFragileItemsCount(): Flow<Int>
    fun getItemCountsByBox(): Flow<Map<String, Int>>
    suspend fun addItemToInventory(item: Item)
    suspend fun updateItemInInventory(item: Item)
    suspend fun deleteItemFromInventory(item: Item)
    suspend fun getTotalBoxesCount(): Int

    suspend fun hasPendingInventoryCloudWork(userId: String): Boolean
}