package com.example.localgigs

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.localgigs.pages.ClientHomePage
import com.example.localgigs.pages.ClientSearchPage
import com.example.localgigs.pages.HomePage
import com.example.localgigs.pages.MessagesPage
import com.example.localgigs.pages.ProfilePage
import com.example.localgigs.pages.SearchPage
import com.example.localgigs.repository.MessageRepository

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    navController: NavController,
    isProfessional: Boolean,
    userId: String
) {
    // Check if user is authenticated before showing MainScreen content
    val isAuthenticated = authViewModel.authState.value is AuthState.Authenticated
    if (!isAuthenticated) {
        // Log for debugging
        Log.d("MainScreen", "User not authenticated, navigating to login.")
        navController.navigate("login") {
            popUpTo("login") { inclusive = true } // Clear navigation stack
        }
        return
    }

    // Log for debugging
    Log.d("MainScreen", "User authenticated, isProfessional: $isProfessional")

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Search", Icons.Default.Search),
        NavItem("Messages", Icons.Default.MailOutline),
        NavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = "Icon")
                        },
                        label = {
                            Text(text = navItem.label)
                        })
                }
            }
        }
    ) { innerPadding ->
        // Log for debugging
        Log.d("MainScreen", "Selected index: $selectedIndex")
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            navController = navController,
            authViewModel = authViewModel,
            isProfessional = isProfessional,
            userId = userId
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavController,
    authViewModel: AuthViewModel,
    isProfessional: Boolean,
    userId: String
) {
    if (isProfessional) {
        when (selectedIndex) {
            0 -> HomePage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                isProfessional = isProfessional
            )
            1 -> SearchPage(navController = navController)
            2 -> MessagesPage(
                navController = navController,
                userId = userId,
                messageRepository = MessageRepository()
            )
            3 -> ProfilePage(modifier, authViewModel, navController)
        }
    } else {
        when (selectedIndex) {
            0 -> ClientHomePage(
                navController = navController,
                authViewModel = authViewModel
            )
            1 -> ClientSearchPage()
            2 -> MessagesPage(
                navController = navController,
                userId = userId,
                messageRepository = MessageRepository()
            )
            3 -> ProfilePage(modifier, authViewModel, navController)
        }
    }
}
