package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.localgigs.model.JobApplication
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.example.localgigs.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ApplicantsPage(
    jobId: String, // Job ID passed to the page
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    var applicants by remember { mutableStateOf<List<JobApplication>>(emptyList()) } // Storing JobApplication objects
    var jobTitle by remember { mutableStateOf("") } // Storing the job title

    // Fetch applicants and job title based on jobId
    LaunchedEffect(jobId) {
        fetchApplicants(db, jobId) { fetchedTitle, fetchedApplicants ->
            jobTitle = fetchedTitle
            applicants = fetchedApplicants
        }
    }

    // UI for applicants page
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title of the page
        Text(
            text = "Applicants for \"$jobTitle\"",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of applicants (showing only email addresses)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(applicants) { applicant ->
                ApplicantCard(
                    applicant = applicant,
                    jobId = jobId,
                    db = db,
                    onApprove = { approvedApplicant ->
                        approveApplicant(db, jobId, approvedApplicant)
                    }
                )
            }
        }
    }
}



@Composable
fun ApplicantCard(
    applicant: JobApplication,
    jobId: String,
    db: FirebaseFirestore,
    onApprove: (JobApplication) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${applicant.name}", fontWeight = FontWeight.Bold)
            Text(text = "Email: ${applicant.email}")
            Text(text = "Experience: ${applicant.experience} years")
            Text(text = "Skills: ${applicant.skills}")

            // Approve and Decline buttons
            Row(modifier = Modifier.padding(top = 8.dp)) {
                // Approve button (green check)
                IconButton(onClick = { onApprove(applicant) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Approve",
                        tint = Color.Green
                    )
                }

                // Decline button (red X)
                IconButton(onClick = { /* Handle Decline Action (optional) */ }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Decline",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}



fun fetchApplicants(
    db: FirebaseFirestore,
    jobId: String,
    onResult: (String, List<JobApplication>) -> Unit
) {
    db.collection("jobs")
        .document(jobId) // Use the jobId to get the specific job document
        .get()
        .addOnSuccessListener { document ->
            val jobTitle = document.getString("title") ?: "" // Fetch job title
            val jobStatus = document.getString("status") ?: "" // Fetch job status

            // Check if job status is "assigned"
            if (jobStatus == "assigned") {
                onResult(jobTitle, emptyList()) // Return empty list if job is already assigned
                return@addOnSuccessListener
            }

            val applicants = mutableListOf<JobApplication>()
            val applicantsMap = document.get("applicants") as? Map<String, Any>

            applicantsMap?.forEach { (key, value) ->
                if (value is String) {
                    applicants.add(JobApplication(name = key, email = value)) // Creating a JobApplication object with email
                }
            }

            onResult(jobTitle, applicants) // Return title and applicants list
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                null, "Error fetching applicants: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
}


fun approveApplicant(
    db: FirebaseFirestore,
    jobId: String,
    applicant: JobApplication
) {
    val currentTime = System.currentTimeMillis() // Get current timestamp
    val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(currentTime)) // Format the time as a string

    // Fetch job title from Firestore
    db.collection("jobs").document(jobId).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val jobTitle = document.getString("title") ?: "" // Fetch job title

                // Add the job to the upcoming jobs collection
                val upcomingJob = hashMapOf(
                    "Title" to jobTitle, // Use fetched job title
                    "Scheduled" to formattedTime,
                    "AssignedTo" to applicant.email // Store the email of the approved applicant
                )

                db.collection("upcomingjobs")
                    .add(upcomingJob)
                    .addOnSuccessListener {
                        // Update the status field to 'assigned' in the job document
                        db.collection("jobs").document(jobId)
                            .update("status", "assigned")
                            .addOnSuccessListener {
                                Toast.makeText(null, "Applicant Approved and Job Status Updated!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(null, "Error updating job status: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(null, "Error approving applicant: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(null, "Job not found!", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(null, "Error fetching job: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}






// Data class for Job Application (Applicant)
data class JobApplication(
    val name: String = "",
    val email: String = "",
    val experience: Int = 0,
    val skills: String = ""
)
