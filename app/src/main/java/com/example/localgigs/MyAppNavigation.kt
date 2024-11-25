package com.example.localgigs

import com.example.localgigs.pages.ClientSearchPage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localgigs.model.ChatViewModel
import com.example.localgigs.pages.ChatScreen
import com.example.localgigs.pages.ClientHomePage
import com.example.localgigs.pages.JobViewPage
import com.example.localgigs.pages.LoginPage
import com.example.localgigs.pages.PostJobPage
import com.example.localgigs.pages.ProfessionalDetailsPage
import com.example.localgigs.pages.SignupPage
import com.example.localgigs.pages.UsersListPage
import com.example.localgigs.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Retrieve current user's ID
    val currentUserId = authViewModel.currentUserId

    // Determine if user is authenticated and has a professional role
    val isAuthenticated = authViewModel.authState.value is AuthState.Authenticated
    val userTypeState by authViewModel.userType.observeAsState(initial = null)

    val startDestination = if (isAuthenticated) {
        if (userTypeState == "Client") "client home" else "home"
    } else {
        "login"
    }

    // Get the singleton instance of MessageRepository
    val messageRepository = MessageRepository.getInstance()

    // Conditional navigation based on authentication state
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("client home") {
            ClientHomePage(
                navController = navController,
                authViewModel = authViewModel)
        }
        composable("home") {
            MainScreen(
                authViewModel = authViewModel,
                modifier = modifier,
                navController = navController,
                userTypeState  == "Professional",
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

        composable("PostJob") {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            PostJobPage(
                navController = navController,
                userEmail = userEmail
            )
        }

        composable("ClientSearch") {
            ClientSearchPage(navController = navController)
        }

        composable(
            "jobView/{title}/{location}/{pay}/{description}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
                navArgument("pay") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "No Title"
            val location = backStackEntry.arguments?.getString("location") ?: "Unknown"
            val pay = backStackEntry.arguments?.getString("pay") ?: "Negotiable"
            val description = backStackEntry.arguments?.getString("description") ?: "Unknown"
            JobViewPage(navController, title, location, pay, description)
        }
        composable("professional_details_page/{userEmail}") { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""
            ProfessionalDetailsPage(userEmail = userEmail)
        }
    }
}
