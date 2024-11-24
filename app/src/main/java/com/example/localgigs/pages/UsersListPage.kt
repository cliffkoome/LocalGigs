package com.example.localgigs.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.localgigs.repository.MessageRepository
import kotlinx.coroutines.launch

@Composable
fun UsersListPage(
    navController: NavController,
    users: List<String>, // Replace with actual user data model
    currentUserId: String,
    messageRepository: MessageRepository
) {
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var conversationId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Launch the coroutine to get or create conversation when a user is selected
    LaunchedEffect(selectedUserId) {
        selectedUserId?.let {
            coroutineScope.launch {
                // Call the suspend function to get or create a conversation
                conversationId = messageRepository.getOrCreateConversation(currentUserId, it)
                conversationId?.let { id ->
                    // Navigate to the chat page with the conversation ID
                    navController.navigate("chat/$id")
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Users List", modifier = Modifier.padding(bottom = 16.dp))

        // List of users (you can replace this with actual user data)
        users.forEach { userId ->
            Button(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                onClick = {
                    selectedUserId = userId
                    // Navigate to the ChatScreen with the selected user
                    navController.navigate("chatScreen/${userId}")
                }
            ) {
                Text(text = "Chat with User $userId") // Display the user information here
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optionally, add a back button or other navigation UI here
        TextButton(onClick = { navController.popBackStack() }) {
            Text(text = "Back")
        }
    }
}
