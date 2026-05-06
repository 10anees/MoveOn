package com.example.moveon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moveon.data.local.entities.InventoryPendingSyncEntity

@Dao
interface InventoryPendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: InventoryPendingSyncEntity)

    @Query(
        """
        SELECT * FROM inventory_sync_queue
        WHERE user_id = :userId AND op = 'DELETE'
        ORDER BY created_at_utc ASC
        """
    )
    suspend fun getPendingDeletes(userId: String): List<InventoryPendingSyncEntity>

    @Query(
        """
        SELECT * FROM inventory_sync_queue
        WHERE user_id = :userId AND op = 'PATCH'
        ORDER BY created_at_utc ASC
        """
    )
    suspend fun getPendingPatches(userId: String): List<InventoryPendingSyncEntity>

    @Query(
        """
        SELECT * FROM inventory_sync_queue
        WHERE user_id = :userId AND op = 'PACKED'
        ORDER BY created_at_utc ASC
        """
    )
    suspend fun getPendingPacked(userId: String): List<InventoryPendingSyncEntity>

    @Query("DELETE FROM inventory_sync_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT COUNT(*) FROM inventory_sync_queue
        WHERE user_id = :userId AND box_uuid = :boxUuid AND op = 'DELETE'
        """
    )
    suspend fun countPendingDelete(userId: String, boxUuid: String): Int

    @Query(
        """
        DELETE FROM inventory_sync_queue
        WHERE user_id = :userId AND box_uuid = :boxUuid
        """
    )
    suspend fun clearQueuedOpsForBox(userId: String, boxUuid: String)
}
