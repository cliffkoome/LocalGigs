package com.example.localgigs.repository

import com.example.localgigs.model.Conversation
import com.example.localgigs.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MessageRepository {

    private val db = FirebaseFirestore.getInstance()

    // Get conversations for the user
    suspend fun getConversations(userId: String, callback: (List<Conversation>) -> Unit) {
        try {
            val result = db.collection("conversations")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            val conversations = result.documents.mapNotNull { doc ->
                val participants = doc.get("participants") as List<*>
                val lastMessage = doc.getString("lastMessage") ?: "No messages yet"
                val otherUserId = participants.first { it != userId } as String
                val otherUserName = getUserName(otherUserId)

                Conversation(
                    id = doc.id,
                    otherUserName = otherUserName,
                    lastMessage = lastMessage
                )
            }
            callback(conversations)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(emptyList())
        }
    }

    // Get messages for a specific conversation
    suspend fun getMessages(conversationId: String, callback: (List<Message>) -> Unit) {
        try {
            val result = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()

            val messages = result.documents.map { doc ->
                Message(
                    senderId = doc.getString("senderId") ?: "Unknown",
                    text = doc.getString("content") ?: "No content",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
            callback(messages)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(emptyList())
        }
    }

    // Send a message in a conversation
    suspend fun sendMessage(conversationId: String, messageText: String, senderId: String) {
        try {
            val newMessage = hashMapOf(
                "senderId" to senderId,
                "content" to messageText,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(newMessage)
                .await()

            // Update the last message in the conversation
            db.collection("conversations")
                .document(conversationId)
                .update("lastMessage", messageText)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get or create a conversation between two users
    suspend fun getOrCreateConversation(userId: String, otherUserId: String): String? {
        return try {
            // Query for existing conversations involving the current user
            val conversations = db.collection("conversations")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            // Check if a conversation with the other user exists
            val existingConversation = conversations.documents.find { doc ->
                val participants = doc.get("participants") as List<*>
                participants.contains(otherUserId)
            }

            if (existingConversation != null) {
                // Return the existing conversation ID
                existingConversation.id
            } else {
                // Create a new conversation if one doesn't exist
                val newConversation = hashMapOf(
                    "participants" to listOf(userId, otherUserId),
                    "lastMessage" to ""
                )
                val newDoc = db.collection("conversations").add(newConversation).await()
                newDoc.id // Return the new conversation ID
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetch user name (mocked for now)
    private fun getUserName(userId: String): String {
        // Mock implementation - replace with actual Firestore query for users
        return "User $userId"
    }

    companion object {
        // Singleton instance
        @Volatile
        private var INSTANCE: MessageRepository? = null

        // Provide access to the singleton instance
        fun getInstance(): MessageRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MessageRepository().also { INSTANCE = it }
            }
        }
    }
}
