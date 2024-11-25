package com.example.localgigs.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import com.example.localgigs.model.Job

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
    var postedJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var ongoingJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
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

    // Fetch posted jobs and ongoing jobs from Firestore
    LaunchedEffect(user?.email) {
        val userEmail = user?.email
        if (userEmail != null) {
            fetchJobs(db, userEmail, "postedBy") { fetchedJobs ->
                postedJobs = fetchedJobs
            }
            fetchJobs(db, userEmail, "status", "assigned") { fetchedJobs ->
                ongoingJobs = fetchedJobs
            }
        }
    }

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

        // Posted Jobs Section
        Text(
            text = "Your Posted Jobs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(postedJobs) { job ->
                JobCard(job, navController)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ongoing Jobs Section
        Text(
            text = "Ongoing Jobs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ongoingJobs) { job ->
                OngoingJobCard(job, navController) // Use OngoingJobCard here
            }
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}

@Composable
fun JobCard(job: Job, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                navController.navigate("ApplicantsPage/${job.jobId}")
            },
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Category: ${job.category}")
            Text(text = "Location: ${job.location}")
            Text(text = "Pay: KES ${job.pay}") // Updated from Budget to Pay
            Text(text = "Job Type: ${job.jobType}")
            Text(text = "Skills: ${job.skills}")
            Text(text = "Description: ${job.description}")
        }
    }
}
// Updated Ongoing Job Card to be clickable
@Composable
fun OngoingJobCard(job: Job, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(
                    "ManageJobsPage/${
                        Uri.encode(job.jobId)
                    }/${
                        Uri.encode(job.title)
                    }/${
                        Uri.encode(job.description)
                    }/${
                        job.pay
                    }/${
                        Uri.encode(job.location)
                    }"
                )
            }
        ,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = job.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = job.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

fun fetchJobs(
    db: FirebaseFirestore,
    userEmail: String,
    filterField: String,
    filterValue: String? = null,
    onResult: (List<Job>) -> Unit
) {
    val query = when (filterField) {
        "status" -> db.collection("jobs")
            .whereEqualTo("status", filterValue)  // For ongoing jobs, filter by "status"
            .whereEqualTo("postedBy", userEmail)  // Only show jobs where postedBy matches current user email
        else -> db.collection("jobs").whereEqualTo(filterField, userEmail)
    }

    query.get()
        .addOnSuccessListener { result: QuerySnapshot ->
            val fetchedJobs = result.documents.mapNotNull { document ->
                val job = document.toObject<Job>()
                job?.copy(jobId = document.id) // Adding jobId
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
