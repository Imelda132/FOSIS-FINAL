package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.PermitEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleLogEntity

@Database(
    entities = [
        FosisItemEntity::class,
        PermitEntity::class,
        UserEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class,
        ChatMessageEntity::class,
        VehicleLogEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class FosisDatabase : RoomDatabase() {
    abstract fun fosisDao(): FosisDao

    companion object {
        @Volatile
        private var INSTANCE: FosisDatabase? = null

        fun getDatabase(context: Context): FosisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FosisDatabase::class.java,
                    "fosis_master_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}