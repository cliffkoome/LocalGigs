package com.example.localgigs.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var ongoingJobsAssigned by remember { mutableStateOf<List<Job>>(emptyList()) }
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

    // Fetch posted jobs and ongoing jobs (assigned) from Firestore
    LaunchedEffect(user?.email) {
        val userEmail = user?.email
        if (userEmail != null) {
            fetchJobs(db, userEmail, "postedBy") { fetchedJobs ->
                postedJobs = fetchedJobs
            }
            fetchJobs(db, userEmail, "status", "assigned") { fetchedJobs ->
                ongoingJobsAssigned = fetchedJobs
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Header
        Text(
            text = "Welcome, $firstname!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Your Client Dashboard",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Posted Jobs Section
        Text(
            text = "Your Posted Jobs",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(0xFF007BFF)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(postedJobs) { job ->
                val cardColor = when (job.status) {
                    "completed" -> Color(0xFFA5D6A7) // Green
                    else -> Color.LightGray
                }
                JobCard(job, navController, cardColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ongoing Jobs (Posted by User)
        Text(
            text = "Ongoing Jobs (Posted by You)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(0xFF007BFF)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ongoingJobsAssigned) { job ->
                JobCard(job, navController, Color(0xFFFFE0B2)) // Light Orange
            }
        }

    }
    FloatingActionButton(
        onClick = {
            navController.navigate("PostJob")
        },
        modifier = Modifier
            .padding(bottom = 150.dp, end = 16.dp)
            .align(Alignment.BottomEnd)

    ) {
        Icon(Icons.Default.Add, contentDescription = "Compose Message")
    }}
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}

@Composable
fun JobCard(job: Job, navController: NavController, cardColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable {
                if (job.status == "assigned") {
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
                } else {
                    navController.navigate("ApplicantsPage/${job.jobId}")
                }
            },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = job.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${job.category}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Location: ${job.location}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Pay: KES ${job.pay}", fontSize = 14.sp, color = Color(0xFF007BFF))
            Text(text = "Job Type: ${job.jobType}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Skills: ${job.skills}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Status: ${job.status}", fontSize = 14.sp, color = Color(0xFF007BFF))
        }
    }
}

// Helper function to fetch jobs from Firestore
fun fetchJobs(
    db: FirebaseFirestore,
    userEmail: String,
    filterField: String,
    filterValue: String? = null,
    onResult: (List<Job>) -> Unit
) {
    val query = when (filterField) {
        "status" -> db.collection("jobs")
            .whereEqualTo("status", filterValue)  // For assigned jobs
            .whereEqualTo("postedBy", userEmail)  // Filter for user's jobs
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
