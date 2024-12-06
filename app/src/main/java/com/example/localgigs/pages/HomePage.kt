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
    val authState = authViewModel.authState.observeAsState()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val userEmail = user?.email

    var firstname by remember { mutableStateOf("User") }
    var recentJobs by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var upcomingJobs by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var totalEarnings by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            isLoading = true
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: "User"
                    }
                }
                .addOnFailureListener { Log.e("HomePage", "Error loading user data") }

            db.collection("recentjobs")
                .whereEqualTo("completedBy", userEmail)
                .get()
                .addOnSuccessListener { result ->
                    recentJobs = result.map { it.data }
                    totalEarnings = recentJobs.sumOf { (it["Pay"] as? Double) ?: 0.0 }
                }
                .addOnFailureListener { Log.e("HomePage", "Error fetching recent jobs") }

            db.collection("upcomingjobs")
                .whereEqualTo("AssignedTo", userId)
                .get()
                .addOnSuccessListener { result ->
                    upcomingJobs = result.map { it.data }
                }
                .addOnFailureListener { Log.e("HomePage", "Error fetching upcoming jobs") }

            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
//        TopBar(navController, authViewModel)
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text("Welcome, $firstname!", style = MaterialTheme.typography.headlineMedium)
        Text("Your Professional Dashboard", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Earnings Overview
        EarningsOverview(totalEarnings)

        Spacer(modifier = Modifier.height(24.dp))

        // Job Sections
        JobSection("Recent Jobs", recentJobs, Color(0xFFE3F2FD))
        Spacer(modifier = Modifier.height(16.dp))
        JobSection("Upcoming Jobs", upcomingJobs, Color(0xFFFBE9E7))

        if (isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}

@Composable
fun TopBar(navController: NavController, authViewModel: AuthViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { authViewModel.signout() }) {
            Text("Sign Out", color = Color.Red)
        }
    }
}

@Composable
fun EarningsOverview(totalEarnings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Earnings", style = MaterialTheme.typography.bodyLarge)
            Text(
                "KES ${"%.2f".format(totalEarnings)}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun JobSection(title: String, jobs: List<Map<String, Any>>, cardColor: Color) {
    Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
    Spacer(modifier = Modifier.height(8.dp))

    if (jobs.isNotEmpty()) {
        LazyColumn {
            items(jobs) { job ->
                JobCard(job, cardColor)
            }
        }
    } else {
        Text("No $title found", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun JobCard(job: Map<String, Any>, cardColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(job["Title"] as? String ?: "Unknown", fontWeight = FontWeight.Bold)
            Text("Status: ${job["Status"] ?: "Unknown"}", color = Color.Gray)
        }
    }
}
