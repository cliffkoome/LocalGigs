package com.example.localgigs.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String,
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0L // Optional: To sort messages by time
)
