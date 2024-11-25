package com.example.localgigs.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ClientSearchPage(navController: NavController) {
    // Create mutable states for users, search query, and loading state
    val users = remember { mutableStateListOf<User>() }
    val isLoading = remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch users from Firestore on launch
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users")
                .whereEqualTo("userType", "Professional")
                .get()
                .await()

            users.clear()
            snapshot.documents.forEach { document ->
                val user = document.toObject(User::class.java)
                user?.let { users.add(it) }
            }

            // Set loading state to false after data fetch
            isLoading.value = false
        } catch (e: Exception) {
            Log.e("ClientSearchPage", "Error fetching users: ${e.localizedMessage}")
            isLoading.value = false
        }
    }

    // Display loading indicator or user data
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("Search Professionals by Job Title") },
            shape = RoundedCornerShape(8.dp)
        )

        // Show loading indicator while data is being fetched
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Filtered list of users based on search query in jobTitle
            LazyColumn {
                val filteredUsers = users.filter { user ->
                    user.jobTitle.contains(searchQuery, ignoreCase = true)
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        Text("No professionals found.", modifier = Modifier.fillMaxWidth().padding(16.dp))
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserCard(user = user, navController = navController)
                    }
                }
            }
        }
    }
}


@Composable
fun UserCard(user: User, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                // Navigate to the professional details page and pass the user's email
                navController.navigate("professional_details_page/${user.email}")
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Display JobTitle as the main content
            Text(
                text = user.jobTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Display Firstname + Lastname
            Text(
                text = "${user.firstname} ${user.lastname}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}
