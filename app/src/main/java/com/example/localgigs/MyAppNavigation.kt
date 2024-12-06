package com.example.localgigs

import android.net.Uri
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
import com.example.localgigs.pages.ApplicantsPage
import com.example.localgigs.pages.ChatScreen
import com.example.localgigs.pages.ClientHomePage
import com.example.localgigs.pages.JobViewPage
import com.example.localgigs.pages.LoginPage
import com.example.localgigs.pages.ManageJobsPage
import com.example.localgigs.pages.PostJobPage
import com.example.localgigs.pages.ProfessionalDetailsPage
import com.example.localgigs.pages.SearchPage
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
        "home"
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
        composable("search") { 
            SearchPage(navController = navController)
        }
//        composable("client home") {
//            ClientHomePage(
//                navController = navController,
//                authViewModel = authViewModel)
//        }
        composable("home") {
            MainScreen(
                authViewModel = authViewModel,
                modifier = modifier,
                navController = navController,
                userTypeState  == "Professional",
                userId = currentUserId ?: ""
            )
        }
        composable(
            route = "ManageJobsPage/{jobId}/{jobTitle}/{jobDescription}/{jobPay}/{jobLocation}"
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")?.let { Uri.decode(it) } ?: ""
            val jobTitle = backStackEntry.arguments?.getString("jobTitle")?.let { Uri.decode(it) } ?: ""
            val jobDescription = backStackEntry.arguments?.getString("jobDescription")?.let { Uri.decode(it) } ?: ""
            val jobPay = backStackEntry.arguments?.getString("jobPay")?.toDoubleOrNull() ?: 0.0
            val jobLocation = backStackEntry.arguments?.getString("jobLocation")?.let { Uri.decode(it) } ?: ""

            ManageJobsPage(
                navController = navController,
                jobId = jobId,
                jobTitle = jobTitle,
                jobDescription = jobDescription,
                jobPay = jobPay,
                jobLocation = jobLocation
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
            "jobView/{jobId}/{title}/{location}/{pay}/{description}/{professionalEmail}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
                navArgument("pay") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("professionalEmail") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Get the jobId and other job details from the backStackEntry
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "No Title"
            val location = backStackEntry.arguments?.getString("location") ?: "Unknown"
            val pay = backStackEntry.arguments?.getString("pay") ?: "Negotiable"
            val description = backStackEntry.arguments?.getString("description") ?: "Unknown"
            val professionalEmail = backStackEntry.arguments?.getString("professionalEmail") ?: ""


            // Pass jobId to JobViewPage along with other details
            JobViewPage(navController, jobId, title, location, pay, description, professionalEmail)
        }

        composable("professional_details_page/{userEmail}") { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""
            ProfessionalDetailsPage(userEmail = userEmail)
        }

        composable("ApplicantsPage/{jobId}") { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            ApplicantsPage(jobId = jobId, navController = navController)
        }

    }
}
