import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localgigs.model.ChatViewModel
import com.example.localgigs.pages.ChatScreen
import com.example.localgigs.pages.HomePage
import com.example.localgigs.pages.LoginPage
import com.example.localgigs.pages.SearchPage
import com.example.localgigs.pages.SignupPage
import com.example.localgigs.pages.UsersListPage
import com.example.localgigs.pages.ClientHomePage
import com.example.localgigs.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel
import com.example.localgigs.pages.PostJobPage
import kotlinx.coroutines.tasks.await

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Retrieve current user's ID
    val currentUserId = authViewModel.currentUserId

    // Determine if user is authenticated
    val isAuthenticated = authViewModel.authState.value == AuthState.Authenticated

    // Get the singleton instance of MessageRepository
    val messageRepository = MessageRepository.getInstance()

    // State to hold the user role (Professional or Client)
    var userRole by remember { mutableStateOf<String?>(null) }

    // Conditional navigation based on authentication state
    NavHost(navController = navController, startDestination = if (isAuthenticated) "checkRole" else "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }

        // Route to check user role after authentication
        composable("checkRole") {
            // Fetch the user role asynchronously
            LaunchedEffect(currentUserId) {
                currentUserId?.let {
                    val role = getUserRole(it)
                    userRole = role
                    if (role == "Professional") {
                        navController.navigate("professionalHome") // Navigate to professional's homepage
                    } else {
                        navController.navigate("clientHome") // Navigate to client's homepage
                    }
                }
            }
        }

        // Homepages
        composable("professionalHome") {
            HomePage(
                navController = navController,
                authViewModel = authViewModel,
                isProfessional = true // Pass true for professionals
            )
        }
        composable("clientHome") {
            ClientHomePage(
                navController = navController,
                authViewModel = authViewModel
            ) // Client homepage
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

        composable("search") {
            SearchPage(navController = navController)
        }

        composable("PostJob") {
            PostJobPage(navController = navController)
        }
    }
}

// Function to check the user's role from Firestore (returns a String role)
suspend fun getUserRole(userId: String): String {
    return try {
        val document = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
        document.getString("userType") ?: "Client" // Default to "Client" if not found
    } catch (e: Exception) {
        "Client" // Default to "Client" in case of failure
    }
}
