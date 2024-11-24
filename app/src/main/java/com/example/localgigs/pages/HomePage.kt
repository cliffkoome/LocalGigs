package com.example.localgigs.pages

import android.util.Log
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
    // Fetching the user's data
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

    // Fetch recent jobs and upcoming jobs from Firestore
    var recentjobs by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var upcomingjobs by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("recentjobs").get()
                .addOnSuccessListener { result ->
                    val jobs = result.map { document ->
                        mapOf(
                            "Title" to (document.getString("Title") ?: ""),
                            "Status" to (document.getString("Status") ?: "")
                        )
                    }
                    Log.d("HomePage", "Fetched Recent Jobs: $jobs")
                    recentjobs = jobs
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(navController.context, "Error fetching recent jobs: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            db.collection("upcomingjobs").get()
                .addOnSuccessListener { result ->
                    val jobs = result.map { document ->
                        mapOf(
                            "Title" to (document.getString("Title") ?: ""),
                            "scheduledAt" to (document.getString("scheduledAt") ?: "")
                        )
                    }
                    Log.d("HomePage", "Fetched Upcoming Jobs: $jobs")
                    upcomingjobs = jobs
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(navController.context, "Error fetching upcoming jobs: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // UI Layout
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

        // Earnings Overview (optional)
        Text(
            text = "Total Earnings: KES 120000", // Replace with actual total earnings
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Jobs Section
        Text(text = "Recent Jobs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Display Recent Jobs
        if (recentjobs.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                items(recentjobs) { job ->
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
                                text = job["Title"] ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Status: ${job["Status"] ?: ""}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            Text("No recent jobs found")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Jobs Section
        Text(text = "Upcoming Jobs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Display Upcoming Jobs
        if (upcomingjobs.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                items(upcomingjobs) { job ->
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
                                text = job["Title"] ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Scheduled At: ${job["scheduledAt"] ?: ""}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            Text("No upcoming jobs found")
        }
    }

    // Handle unauthenticated state
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}
