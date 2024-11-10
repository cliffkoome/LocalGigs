package com.example.localgigs.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    isProfessional: Boolean
) {
    val authState = authViewModel.authState.observeAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        Text(text = "Welcome to LocalGigs!", fontSize = 24.sp)
        Text(
            text = if (isProfessional) "Explore Gigs Near You" else "Find Reliable Professionals",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile and Sign Out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.navigate("profile") }) {
                Text("View Profile")
            }
            TextButton(onClick = { authViewModel.signout() }) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Job Listings or Available Gigs
        Text(
            text = if (isProfessional) "Available Gigs" else "Nearby Professionals",
            fontSize = 18.sp
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Mock data, replace with actual data from backend
            val listings = if (isProfessional) listOf("Gig 1", "Gig 2", "Gig 3") else listOf("Pro 1", "Pro 2", "Pro 3")

            items(listings) { listing ->
                Text(text = listing, modifier = Modifier.padding(8.dp))
            }
        }
    }

    // Redirect to Login if unauthenticated
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}
