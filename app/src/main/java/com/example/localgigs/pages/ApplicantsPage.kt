package com.example.localgigs.pages

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.example.localgigs.model.JobApplication
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ApplicantsPage(
    jobId: String,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    var applicants by remember { mutableStateOf<List<JobApplication>>(emptyList()) }
    var jobTitle by remember { mutableStateOf("") }
    val context = LocalContext.current

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

        // List of applicants
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(applicants) { applicant ->
                ApplicantCard(
                    applicant = applicant,
                    jobId = jobId,
                    db = db,
                    context = context,
                    onApprove = { approvedApplicant ->
                        approveApplicant(db, jobId, approvedApplicant, context)
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
    context: Context,
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
                IconButton(onClick = { onApprove(applicant) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Approve",
                        tint = Color.Green
                    )
                }

                IconButton(onClick = { /* Handle Decline Action */ }) {
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
        .document(jobId)
        .get()
        .addOnSuccessListener { document ->
            val jobTitle = document.getString("title") ?: ""
            val jobStatus = document.getString("status") ?: ""

            if (jobStatus == "assigned" || jobStatus == "completed") {
                onResult(jobTitle, emptyList())
                return@addOnSuccessListener
            }

            val applicants = mutableListOf<JobApplication>()
            val applicantsMap = document.get("applicants") as? Map<String, String>

            applicantsMap?.forEach { (key, email) ->
                applicants.add(JobApplication(name = key, email = email))
            }

            onResult(jobTitle, applicants)
        }
        .addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}

fun approveApplicant(
    db: FirebaseFirestore,
    jobId: String,
    applicant: JobApplication,
    context: Context
) {
    val currentTime = System.currentTimeMillis()
    val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(currentTime))
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    db.collection("users").whereEqualTo("email", applicant.email)
        .get()
        .addOnSuccessListener { querySnapshot ->
            val userDocument = querySnapshot.documents.firstOrNull()
            val applicantUid = userDocument?.getString("uid") ?: ""

            if (applicantUid.isEmpty()) {
                Toast.makeText(context, "Applicant UID not found!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            db.collection("jobs").document(jobId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val jobTitle = document.getString("title") ?: ""

                        val upcomingJob = hashMapOf(
                            "Title" to jobTitle,
                            "Scheduled" to formattedTime,
                            "AssignedTo" to applicantUid,
                            "PostedBy" to currentUserEmail
                        )

                        db.collection("upcomingjobs")
                            .add(upcomingJob)
                            .addOnSuccessListener {
                                db.collection("jobs").document(jobId)
                                    .update("status", "assigned", "AssignedTo", applicantUid)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Applicant Approved!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error updating job: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Error adding job: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Job not found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error fetching job: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Error fetching applicant UID: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

// Data class for Job Application
data class JobApplication(
    val name: String = "",
    val email: String = "",
    val experience: Int = 0,
    val skills: String = ""
)
