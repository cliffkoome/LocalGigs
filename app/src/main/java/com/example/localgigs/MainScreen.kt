package com.example.localgigs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.compose.rememberNavController
import com.example.localgigs.pages.HomePage
import com.example.localgigs.pages.MessagesPage
import com.example.localgigs.pages.NotificationPage
import com.example.localgigs.pages.ProfilePage
import com.example.localgigs.pages.SearchPage

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Determine if the user is a professional
    val isProfessional = authViewModel.authState.value == AuthState.Authenticated && checkUserRole(authViewModel)

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
                navItemList.forEachIndexed{ index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index ,
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
        MyAppNavigation(
            modifier = Modifier.padding(innerPadding),
            authViewModel = authViewModel
        )
        // Pass isProfessional to ContentScreen
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            navController = navController,
            authViewModel = authViewModel,
            isProfessional = isProfessional
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavController,
    authViewModel: AuthViewModel,
    isProfessional: Boolean
) {
    when(selectedIndex){
        0 -> HomePage(
            modifier = modifier,
            navController = navController,
            authViewModel = authViewModel,
            isProfessional = isProfessional
        )
        1 -> SearchPage()
        2 -> MessagesPage()
        3 -> ProfilePage()
    }
}