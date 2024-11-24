package com.example.localgigs.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localgigs.model.Message
import com.example.localgigs.model.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("RememberReturnType")
@Composable
fun ChatScreen(
    userId: String,  // No need to redefine this
    conversationId: String,
    chatViewModel: ChatViewModel = viewModel()
) {
    // Observe messages from ViewModel using collectAsStateWithLifecycle
    val messages = chatViewModel.messages.collectAsStateWithLifecycle().value
    val messageText = remember { mutableStateOf("") }

    // Load messages when the conversation ID changes
    LaunchedEffect(conversationId) {
        chatViewModel.getMessages(conversationId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // List of messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            reverseLayout = true // Latest messages at the bottom
        ) {
            items(messages) { message ->
                MessageItem(message = message, currentUserId = userId)
            }
        }

        // Input field and send button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.value.isNotBlank()) {
                            chatViewModel.sendMessage(conversationId, messageText.value, userId)
                            messageText.value = "" // Clear input field
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp)
            )

            IconButton(
                onClick = {
                    if (messageText.value.isNotBlank()) {
                        chatViewModel.sendMessage(conversationId, messageText.value, userId)
                        messageText.value = "" // Clear input field
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isSentByCurrentUser = message.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.text,
            fontSize = 16.sp,
            color = if (isSentByCurrentUser) Color.White else Color.Black,
            modifier = Modifier
                .background(
                    if (isSentByCurrentUser) Color.Blue else Color.LightGray,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}