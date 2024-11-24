package com.example.localgigs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localgigs.model.ChatViewModel
import com.example.localgigs.pages.ChatScreen
import com.example.localgigs.pages.HomePage
import com.example.localgigs.pages.LoginPage
import com.example.localgigs.pages.SignupPage
import com.example.localgigs.pages.UsersListPage
import com.example.localgigs.repository.MessageRepository

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Retrieve current user's ID
    val currentUserId = authViewModel.currentUserId

    // Determine if user is authenticated and has a professional role
    val isAuthenticated = authViewModel.authState.value == AuthState.Authenticated
    val isProfessional = isAuthenticated && checkUserRole(authViewModel)

    // Get the singleton instance of MessageRepository
    val messageRepository = MessageRepository.getInstance()

    // Conditional navigation based on authentication state
    NavHost(navController = navController, startDestination = if (isAuthenticated) "home" else "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            MainScreen(
                authViewModel = authViewModel,
                modifier = modifier,
                navController = navController,
                isProfessional = isProfessional,
                userId = currentUserId ?: ""
            )
        }
        composable("users") {
            // Fetch the list of users (replace with actual data fetching logic)
            val users = listOf("User1", "User2", "User3") // Replace with actual user fetching logic

            UsersListPage(
                navController = navController,
                users = users,
                currentUserId = currentUserId ?: "",
                messageRepository = messageRepository
            )
        }

        // Route for ChatScreen
        composable("chatScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val conversationId = "conversation_with_$userId" // Assuming conversationId is based on userId
            ChatScreen(
                userId = userId,
                conversationId = conversationId,
                chatViewModel = ChatViewModel(messageRepository = messageRepository)
            )
        }
    }
}

// Mock function to determine user role (replace with actual implementation)
fun checkUserRole(authViewModel: AuthViewModel): Boolean {
    return true // Replace this logic with actual role checking
}
