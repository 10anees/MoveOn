package com.example.moveon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.InventoryPendingSyncDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.local.dao.UserSessionDao
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.InventoryPendingSyncEntity
import com.example.moveon.data.local.entities.ItemEntity
import com.example.moveon.data.local.entities.UserSessionEntity

@Database(
    entities = [
        BoxEntity::class,
        ItemEntity::class,
        UserSessionEntity::class,
        InventoryPendingSyncEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MoveOnDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun itemDao(): ItemDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun inventoryPendingSyncDao(): InventoryPendingSyncDao

    companion object{
        const val DATABASE_NAME = "moveon_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_sessions` (
                        `user_id` TEXT NOT NULL,
                        `first_name` TEXT NOT NULL,
                        `last_name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone_number` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `last_login_time` INTEGER,
                        `last_synced_at` INTEGER NOT NULL,
                        PRIMARY KEY(`user_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("PRAGMA foreign_keys=OFF")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `boxes_new` (
                        `box_uuid` TEXT NOT NULL,
                        `box_id` TEXT NOT NULL,
                        `booking_id` INTEGER NOT NULL,
                        `vehicle_id` INTEGER,
                        `category` TEXT NOT NULL,
                        `label` TEXT NOT NULL,
                        `volume` REAL NOT NULL,
                        `packed` INTEGER NOT NULL,
                        PRIMARY KEY(`box_uuid`)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO `boxes_new` (
                        `box_uuid`,
                        `box_id`,
                        `booking_id`,
                        `vehicle_id`,
                        `category`,
                        `label`,
                        `volume`,
                        `packed`
                    )
                    SELECT
                        `box_id` AS `box_uuid`,
                        `box_id`,
                        `booking_id`,
                        `vehicle_id`,
                        `category`,
                        `label`,
                        `volume`,
                        0 AS `packed`
                    FROM `boxes`
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE `boxes`")
                database.execSQL("ALTER TABLE `boxes_new` RENAME TO `boxes`")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_boxes_box_id` ON `boxes` (`box_id`)"
                )

                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `items` ADD COLUMN `quantity` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE boxes ADD COLUMN owner_user_id TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE boxes ADD COLUMN color_hex TEXT NOT NULL DEFAULT '#1565C0'"
                )
                database.execSQL(
                    "ALTER TABLE boxes ADD COLUMN pending_cloud_upload INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventory_sync_queue` (
                        `id` TEXT NOT NULL,
                        `user_id` TEXT NOT NULL,
                        `op` TEXT NOT NULL,
                        `box_uuid` TEXT NOT NULL,
                        `box_id` TEXT,
                        `category` TEXT,
                        `label` TEXT,
                        `color_hex` TEXT,
                        `packed_value` INTEGER,
                        `created_at_utc` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_inventory_sync_queue_user_id`
                    ON `inventory_sync_queue` (`user_id`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_inventory_sync_queue_user_box`
                    ON `inventory_sync_queue` (`user_id`, `box_uuid`)
                    """.trimIndent()
                )
            }
        }
    }
}