package com.example.localgigs.pages

import Job
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ClientHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val authState = authViewModel.authState.observeAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var firstname by remember { mutableStateOf("") } // Example for the client's first name

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

    // Fetch jobs from Firestore filtered by postedBy field
    LaunchedEffect(user?.email) {
        val userEmail = user?.email
        if (userEmail != null) {
            fetchJobs(db, userEmail) { fetchedJobs ->
                jobs = fetchedJobs
            }
        }
    }

    // Job data class
    data class Job(
        val title: String = "",
        val description: String = "",
        val category: String = "",
        val budget: Double = 0.0,
        val location: String = "",
        val jobType: String = "",
        val skills: String = ""
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick Actions at the Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { authViewModel.signout() }) {
                Text("Sign Out", color = Color.Red)
            }
            // Button to Post a Job
            Button(
                onClick = {
                    navController.navigate("PostJob")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Post a Job", color = Color.White)
            }
        }

        // Welcome Header
        Text(
            text = "Welcome, $firstname!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your Client Dashboard",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Jobs list Section
        Text(
            text = "Your Posted Jobs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Displaying list of posted jobs
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(jobs) { job ->
                JobCard(job)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}

@Composable
fun JobCard(job: Job) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Category: ${job.category}")
            Text(text = "Location: ${job.location}")
            Text(text = "Budget: KES ${job.budget}")
            Text(text = "Job Type: ${job.jobType}")
            Text(text = "Skills: ${job.skills}")
            Text(text = "Description: ${job.description}")
        }
    }
}

fun fetchJobs(db: FirebaseFirestore, userEmail: String, onResult: (List<Job>) -> Unit) {
    db.collection("jobs")
        .whereEqualTo("postedBy", userEmail) // Filter jobs by 'postedBy' field
        .get()
        .addOnSuccessListener { result: QuerySnapshot ->
            val fetchedJobs = result.documents.mapNotNull { document ->
                document.toObject<Job>()
            }
            onResult(fetchedJobs)
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                null, "Error fetching jobs: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
}
