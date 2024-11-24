package com.example.localgigs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class MessageRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Send a message to Firestore
    fun sendMessage(conversationId: String, message: String) {
        val senderId = auth.currentUser?.uid
        val timestamp = System.currentTimeMillis()

        val messageData = hashMapOf(
            "senderId" to senderId,
            "message" to message,
            "timestamp" to timestamp
        )

        // Store the message in Firestore under the conversation
        db.collection("messages").document(conversationId).collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                // Handle success (e.g., notify UI)
            }
            .addOnFailureListener { exception ->
                // Handle failure (e.g., show error to user)
            }
    }

    // Get messages for a conversation
    fun getMessages(conversationId: String, callback: (List<Message>) -> Unit) {
        // Get messages ordered by timestamp
        db.collection("messages").document(conversationId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null || snapshot == null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val messages = snapshot.documents.map { doc ->
                    Message(
                        senderId = doc.getString("senderId") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                callback(messages) // Update UI with messages
            }
    }
}

data class Message(
    val senderId: String,
    val message: String,
    val timestamp: Long
)