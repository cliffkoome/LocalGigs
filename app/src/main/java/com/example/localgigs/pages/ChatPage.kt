package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.repository.MessageRepository
import com.example.localgigs.model.Message
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChatPage(
    navController: NavController,
    conversationId: String,
    userId: String,
    messageRepository: MessageRepository
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for launching suspend functions

    // Fetch messages for the conversation
    LaunchedEffect(conversationId) {
        messageRepository.getMessages(conversationId) { fetchedMessages ->
            messages = fetchedMessages
        }
    }

    // Local context for Toast usage
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Chat",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display messages
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            messages.forEach { message ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text(
                        text = "${message.senderName}:",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        color = if (message.senderId == userId) Color.Blue else Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.content,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Message input field
        BasicTextField(
            value = newMessage,
            onValueChange = { newMessage = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newMessage.text.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            messageRepository.sendMessage(conversationId, newMessage.text, userId)
                            newMessage = TextFieldValue("") // Clear input field
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to send message: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Message cannot be empty", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Send", fontSize = 18.sp)
        }
    }
}
