package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    LOCATION,
    FORM_TICKET,
    FORM_PERMIT
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderEmail: String,
    val senderName: String,
    val senderRole: String,
    val recipientEmail: String = "ALL_TIM_OPERATIONS",
    val messageText: String,
    val messageType: MessageType = MessageType.TEXT,
    
    // Location Payload
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    val locationAddress: String = "",
    
    // Form Payload
    val attachedFormId: Long? = null,
    val attachedFormTitle: String = "",
    val attachedFormType: String = "",
    val attachedFormStatus: String = "",
    
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true,
    val isOfflinePending: Boolean = false
)
