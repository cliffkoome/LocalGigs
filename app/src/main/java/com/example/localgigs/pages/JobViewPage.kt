package com.example.localgigs.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun JobViewPage(
    navController: NavController,
    jobId: String,
    jobTitle: String,
    jobLocation: String,
    jobPay: String,
    jobDescription: String,
    professionalEmail: String
) {
    var applicationSuccess by remember { mutableStateOf(false) }  // Track application success
    var applicationMessage by remember { mutableStateOf("") }  // Message to show (e.g., already applied or success)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Job details section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = jobTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Location: $jobLocation",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Pay: KES $jobPay",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp)
            Text(
                text = "Job Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = jobDescription,
                fontSize = 14.sp
            )
        }

        // Apply Job Button
        Button(
            onClick = {
                applyForJob(jobId, navController, { message ->
                    applicationMessage = message
                    applicationSuccess = message == "Successfully applied"
                }) // Handle job application
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(text = "Apply Job", fontSize = 16.sp)
        }

        // Show Snackbar after successful job application or if already applied
        if (applicationMessage.isNotEmpty()) {
            LaunchedEffect(applicationMessage) {
                // Show appropriate message after application attempt
                delay(2000) // Wait for 2 seconds before navigating or showing message
                if (applicationSuccess) {
                    navController.navigate("home") // Navigate to home if successful
                }
            }

            // Display a message, like a Snackbar or Toast
            Text(
                text = applicationMessage,
                color = if (applicationSuccess) Color.Green else Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

// Function to handle job application logic
private fun applyForJob(
    jobId: String,
    navController: NavController,
    onApplicationResult: (String) -> Unit // Callback to handle the result (success or already applied)
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val professionalEmail = currentUser?.email // Get the email of the currently authenticated user

    if (professionalEmail.isNullOrEmpty()) {
        Log.e("JobViewPage", "Error: No user is logged in or email is empty")
        return
    }

    Log.d("JobViewPage", "Applying for job with email: $professionalEmail")

    // Fetch the job document using the jobId
    db.collection("jobs").document(jobId).get().addOnSuccessListener { document ->
        if (document.exists()) {
            // Fetch the current applicants map, or initialize an empty map if it doesn't exist
            val applicants = document.get("applicants") as? Map<String, String> ?: mutableMapOf()

            // Check if the user has already applied
            if (applicants.containsValue(professionalEmail)) {
                onApplicationResult("You have already applied for this job.")  // Notify user if already applied
            } else {
                // Find the next available applicant index (applicant1, applicant2, etc.)
                val nextApplicantIndex = applicants.size + 1

                // Determine the field name for the new applicant
                val applicantField = "applicant$nextApplicantIndex"

                // Add the new applicant to the applicants map
                (applicants as MutableMap)[applicantField] = professionalEmail

                // Update the Firestore document with the new applicants data
                db.collection("jobs").document(jobId)
                    .update("applicants", applicants)
                    .addOnSuccessListener {
                        Log.d("JobViewPage", "Successfully applied for job with email: $professionalEmail")
                        // Notify user of successful application
                        onApplicationResult("Successfully applied for the job!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("JobViewPage", "Error applying for job: ${e.localizedMessage}")
                        onApplicationResult("Error applying for the job. Please try again.")  // Error message
                    }
            }
        } else {
            Log.e("JobViewPage", "Job not found")
            onApplicationResult("Job not found.")  // Job not found error
        }
    }
}
