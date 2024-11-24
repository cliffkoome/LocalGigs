package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.localgigs.repository.MessageRepository
import com.example.localgigs.model.Conversation

@Composable
fun MessagesPage(
    navController: NavController,
    userId: String,
    messageRepository: MessageRepository
) {
    val conversations = remember { mutableStateListOf<Conversation>() }

    LaunchedEffect(userId) {
        messageRepository.getConversations(userId) { fetchedConversations ->
            conversations.clear()
            conversations.addAll(fetchedConversations)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Messages",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(conversations) { conversation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                navController.navigate("chat/${conversation.id}")
                            }
                    ) {
                        Text(
                            text = conversation.otherUserName,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = conversation.lastMessage,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Floating Action Button with improved padding and alignment
        FloatingActionButton(
            onClick = { navController.navigate("users") },
            modifier = Modifier
                .padding(bottom = 150.dp, end = 16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Compose Message")
        }
    }
}
