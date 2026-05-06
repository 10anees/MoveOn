package com.example.moveon.data.repository

import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.InventoryPendingSyncDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.InventoryPendingSyncEntity
import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toEntity
import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import com.example.moveon.domain.repository.InventoryRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val boxDao: BoxDao,
    private val itemDao: ItemDao,
    private val pendingSyncDao: InventoryPendingSyncDao,
    private val firestore: FirebaseFirestore
) : InventoryRepository {

    private val syncMutex = Mutex()

    override suspend fun addNewBox(
        box: Box,
        ownerUserId: String,
        colorHex: String,
        pendingCloudUpload: Boolean
    ) {
        boxDao.insertBox(box.toEntity(ownerUserId, colorHex, pendingCloudUpload))
    }

    override suspend fun markBoxSyncedToCloud(boxUuid: String) {
        boxDao.markCloudSynced(boxUuid)
    }

    override suspend fun syncInventoryWithCloud(userId: String): Result<Unit> =
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                runCatching {
                    syncInventoryWithCloudInternal(userId)
                }
            }
        }

    override suspend fun enqueuePendingBoxDelete(userId: String, boxUuid: String) {
        pendingSyncDao.insert(
            InventoryPendingSyncEntity(
                id = UUID.randomUUID().toString(),
                user_id = userId,
                op = "DELETE",
                box_uuid = boxUuid,
                box_id = null,
                category = null,
                label = null,
                color_hex = null,
                packed_value = null,
                created_at_utc = System.currentTimeMillis()
            )
        )
    }

    override suspend fun enqueuePendingBoxPatch(
        userId: String,
        boxUuid: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    ) {
        pendingSyncDao.insert(
            InventoryPendingSyncEntity(
                id = UUID.randomUUID().toString(),
                user_id = userId,
                op = "PATCH",
                box_uuid = boxUuid,
                box_id = boxId,
                category = category,
                label = label,
                color_hex = colorHex,
                packed_value = null,
                created_at_utc = System.currentTimeMillis()
            )
        )
    }

    override suspend fun enqueuePendingBoxPacked(
        userId: String,
        boxUuid: String,
        packed: Boolean
    ) {
        pendingSyncDao.insert(
            InventoryPendingSyncEntity(
                id = UUID.randomUUID().toString(),
                user_id = userId,
                op = "PACKED",
                box_uuid = boxUuid,
                box_id = null,
                category = null,
                label = null,
                color_hex = null,
                packed_value = if (packed) 1 else 0,
                created_at_utc = System.currentTimeMillis()
            )
        )
    }

    override fun getBoxesForMoveForOwner(bookingId: Int, ownerUserId: String): Flow<List<Box>> {
        return boxDao.getBoxesForBookingForOwner(bookingId, ownerUserId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun getTotalBoxesCountForOwner(ownerUserId: String): Int =
        boxDao.getTotalBoxesCountForOwner(ownerUserId)

    private suspend fun syncInventoryWithCloudInternal(userId: String) {
        boxDao.claimBoxesWithEmptyOwner(bookingId = 0, ownerUserId = userId)

        pendingSyncDao.getPendingDeletes(userId).forEach { op ->
            val result = deleteBoxFromCloud(boxUuid = op.box_uuid, userId = userId)
            if (result.isSuccess) {
                pendingSyncDao.deleteById(op.id)
            }
        }

        val snap = firestore.collection("users")
            .document(userId)
            .collection("boxes")
            .get()
            .await()

        val pendingPatchBoxes =
            pendingSyncDao.getPendingPatches(userId).map { it.box_uuid }.toSet()
        val pendingPackedBoxes =
            pendingSyncDao.getPendingPacked(userId).map { it.box_uuid }.toSet()
        val skipPullOverwrite = pendingPatchBoxes + pendingPackedBoxes

        snap.documents.forEach { doc ->
            val uuid = doc.id
            if (pendingSyncDao.countPendingDelete(userId, uuid) > 0) return@forEach
            if (uuid in skipPullOverwrite) return@forEach

            doc.toOwnedBoxEntityOrNull(primaryOwnerUid = userId)?.let { entity ->
                boxDao.insertBox(
                    entity.copy(
                        pending_cloud_upload = false,
                        owner_user_id = userId
                    )
                )
            }
        }

        boxDao.getBoxesPendingUpload(userId).forEach { entity ->
            val box = entity.toDomainModel()
            val result = addNewBoxToCloud(box = box, userId = userId, colorHex = entity.color_hex)
            if (result.isSuccess) {
                boxDao.markCloudSynced(box.boxUuid)
            }
        }

        pendingSyncDao.getPendingPatches(userId).forEach { op ->
            val boxId = op.box_id ?: return@forEach
            val category = op.category ?: return@forEach
            val label = op.label ?: return@forEach
            val color = op.color_hex ?: BoxEntity.DEFAULT_BOX_COLOR_HEX
            val patchResult = updateBoxInfoInCloud(
                boxUuid = op.box_uuid,
                userId = userId,
                boxId = boxId,
                category = category,
                label = label,
                colorHex = color
            )
            if (patchResult.isSuccess) {
                pendingSyncDao.deleteById(op.id)
            }
        }

        pendingSyncDao.getPendingPacked(userId).forEach { op ->
            val packed = op.packed_value == 1
            val res = updateBoxPackedStatusInCloud(
                boxUuid = op.box_uuid,
                userId = userId,
                isPacked = packed
            )
            if (res.isSuccess) {
                pendingSyncDao.deleteById(op.id)
            }
        }
    }

    override suspend fun addNewBoxToCloud(box: Box, userId: String, colorHex: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val payload = mapOf(
            "box_uuid" to box.boxUuid,
            "box_id" to box.boxId,
            "user_id" to userId,
            "booking_id" to box.bookingId,
            "vehicle_id" to box.vehicleId,
            "category" to box.category,
            "label" to box.label,
            "volume" to box.volume,
            "packed" to box.packed,
            "color_hex" to colorHex,
            "created_at" to now
        )

        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(box.boxUuid)
                .set(payload)
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "inventory_updated_at" to now,
                        "inventory_boxes" to mapOf(box.boxUuid to payload)
                    ),
                    SetOptions.merge()
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} merge inventory_boxes: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to save box to Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun updateBoxPackedStatus(boxUuid: String, isPacked: Boolean) {
        boxDao.updatePackedStatus(boxUuid, isPacked)
    }

    override suspend fun updateBoxPackedStatusInCloud(
        boxUuid: String,
        userId: String,
        isPacked: Boolean
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .set(
                    mapOf(
                        "packed" to isPacked,
                        "updated_at" to now
                    ),
                    SetOptions.merge()
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes packed update: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}.packed" to isPacked,
                        "inventory_boxes.${boxUuid}.updated_at" to now,
                        "inventory_updated_at" to now
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes packed update: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to update packed status in Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun updateBoxInfo(
        boxUuid: String,
        boxId: String,
        category: String,
        label: String
    ) {
        boxDao.updateBoxInfo(
            boxUuid = boxUuid,
            boxId = boxId,
            category = category,
            label = label
        )
    }

    override suspend fun updateBoxInfoInCloud(
        boxUuid: String,
        userId: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val payload = mapOf(
            "box_id" to boxId,
            "category" to category,
            "label" to label,
            "color_hex" to colorHex,
            "updated_at" to now
        )
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .set(payload, SetOptions.merge())
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes info update: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}.box_id" to boxId,
                        "inventory_boxes.${boxUuid}.category" to category,
                        "inventory_boxes.${boxUuid}.label" to label,
                        "inventory_boxes.${boxUuid}.color_hex" to colorHex,
                        "inventory_boxes.${boxUuid}.updated_at" to now,
                        "inventory_updated_at" to now
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes info update: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to update box info in Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun deleteBox(boxUuid: String) {
        boxDao.deleteBoxByUuid(boxUuid)
    }

    override suspend fun deleteBoxFromCloud(boxUuid: String, userId: String): Result<Unit> {
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .delete()
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes delete: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}" to FieldValue.delete(),
                        "inventory_updated_at" to System.currentTimeMillis()
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes delete: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to delete box from Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override fun getBoxesForMove(bookingId: Int): Flow<List<Box>> {
        return boxDao.getBoxesForBooking(bookingId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getItemsInBox(boxId: String): Flow<List<Item>> {
        return itemDao.getItemInBox(boxId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getTotalItemsCount(): Flow<Int> {
        return itemDao.getTotalItemsCount()
    }

    override fun getTotalFragileItemsCount(): Flow<Int> {
        return itemDao.getTotalFragileItemsCount()
    }

    override fun getItemCountsByBox(): Flow<Map<String, Int>> {
        return itemDao.getItemCountsByBox().map { counts ->
            counts.associate { it.boxId to it.itemCount }
        }
    }

    override suspend fun addItemToInventory(item: Item) {
        itemDao.insertItem(item.toEntity())
    }

    override suspend fun updateItemInInventory(item: Item) {
        itemDao.updateItem(item.toEntity())
    }

    override suspend fun deleteItemFromInventory(item: Item) {
        itemDao.deleteItem(item.toEntity())
    }

    override suspend fun getTotalBoxesCount(): Int {
        return boxDao.getTotalBoxesCount()
    }

    override suspend fun hasPendingInventoryCloudWork(userId: String): Boolean {
        if (boxDao.getBoxesPendingUpload(userId).isNotEmpty()) return true
        if (pendingSyncDao.getPendingDeletes(userId).isNotEmpty()) return true
        if (pendingSyncDao.getPendingPatches(userId).isNotEmpty()) return true
        if (pendingSyncDao.getPendingPacked(userId).isNotEmpty()) return true
        return false
    }

    /**
     * Firestore payloads may use ints or doubles for numeric fields depending on SDK version / writers.
     */
    private fun Number.toCleanInt(): Int = this.toDouble().toInt()

    private fun DocumentSnapshot.toOwnedBoxEntityOrNull(primaryOwnerUid: String): BoxEntity? {
        val boxUuid = id
        val boxIdStr = getString("box_id") ?: return null
        val bookingIdRaw = get("booking_id")
        val bookingId = when (bookingIdRaw) {
            is Number -> bookingIdRaw.toCleanInt()
            is String -> bookingIdRaw.toIntOrNull() ?: 0
            else -> 0
        }

        val category = getString("category").orEmpty()
        val label = getString("label").orEmpty()
        val packed = getBoolean("packed") ?: false
        val colorHex = getString("color_hex") ?: BoxEntity.DEFAULT_BOX_COLOR_HEX

        val volumeRaw = get("volume")
        val volume = when (volumeRaw) {
            is Number -> volumeRaw.toDouble()
            else -> volumeRaw?.toString()?.toDoubleOrNull() ?: 0.0
        }

        val vehicleRaw = get("vehicle_id")
        val vehicleId: Int? = when (vehicleRaw) {
            null -> null
            is Number -> vehicleRaw.toCleanInt()
            else -> null
        }

        val ownerUid = getString("user_id") ?: primaryOwnerUid

        return BoxEntity(
            box_uuid = boxUuid,
            box_id = boxIdStr,
            booking_id = bookingId,
            vehicle_id = vehicleId,
            category = category,
            label = label,
            volume = volume,
            packed = packed,
            owner_user_id = ownerUid,
            color_hex = colorHex,
            pending_cloud_upload = false
        )
    }
}
