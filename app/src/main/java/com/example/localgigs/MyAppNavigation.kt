package com.example.localgigs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localgigs.pages.HomePage
import com.example.localgigs.pages.LoginPage
import com.example.localgigs.pages.SignupPage

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    // Here you could check the user’s role, for example:
    // Let's assume a temporary boolean that determines if the user is a professional
    val isProfessional = authViewModel.authState.value == AuthState.Authenticated && checkUserRole(authViewModel)

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier = modifier, navController = navController, authViewModel = authViewModel, isProfessional = isProfessional)
        }
    }
}

// Mock function to determine user role (replace this with actual implementation)
fun checkUserRole(authViewModel: AuthViewModel): Boolean {
    // Logic to check if the user is a professional or client (e.g., based on user profile data)
    return true // or false based on actual role
}
