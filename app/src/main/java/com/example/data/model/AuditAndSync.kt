package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val userEmail: String,
    val userName: String,
    val details: String,
    val status: String = "SUCCESS"
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val operation: String, // CREATE, UPDATE, DELETE, VERIFY, LOCK
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = true
)
