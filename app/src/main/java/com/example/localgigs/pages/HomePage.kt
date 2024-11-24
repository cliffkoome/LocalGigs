package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    isProfessional: Boolean
) {
    // Mock data for demonstration
    val authState = authViewModel.authState.observeAsState()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    var firstname by remember { mutableStateOf("") }

    // Fetch user's first name from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: "User"
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(navController.context, "Error loading user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    val recentJobs = listOf(
        mapOf("title" to "Fixing Plumbing", "status" to "Completed"),
        mapOf("title" to "Electrical Repair", "status" to "Completed"),
        mapOf("title" to "Wall Painting", "status" to "Completed"),
        mapOf("title" to "Carpentry Work", "status" to "Completed"),
        mapOf("title" to "Gardening", "status" to "Completed")
    )
    val upcomingJobs = listOf(
        mapOf("title" to "Roof Repair", "scheduledAt" to "Tomorrow, 10 AM"),
        mapOf("title" to "Floor Installation", "scheduledAt" to "Monday, 2 PM")
    )
    val totalEarnings = 120000.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Actions at the Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.navigate("search") }) {
                Text("Search Jobs")
            }
            TextButton(onClick = { authViewModel.signout() }) {
                Text("Sign Out", color = Color.Red)
            }
        }

        // Welcome Header
        Text(
            text = "Welcome, $firstname!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your Professional Dashboard",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Earnings Overview
        Text(
            text = "Total Earnings: KES $totalEarnings",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Jobs Section
        Text(text = "Recent Jobs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)) {
            items(recentJobs) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = job["title"] as String,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Status: ${job["status"] as String}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Jobs Section
        Text(text = "Upcoming Jobs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)) {
            items(upcomingJobs) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = job["title"] as String,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scheduled At: ${job["scheduledAt"] as String}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}
