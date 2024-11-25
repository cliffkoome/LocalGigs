package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageJobsPage(
    navController: NavController,
    jobId: String,
    jobTitle: String,
    jobDescription: String,
    jobPay: Double,
    jobLocation: String
) {
    val db = FirebaseFirestore.getInstance()
    var professionalName by remember { mutableStateOf("") }
    var professionalTitle by remember { mutableStateOf("") }
    var professionalEmail by remember { mutableStateOf("") }

    // Fetch professional details from Firestore
    LaunchedEffect(jobId) {
        db.collection("jobs").document(jobId).get()
            .addOnSuccessListener { document ->
                val assignedProfessionalId = document.getString("AssignedTo")
                if (assignedProfessionalId != null) {
                    db.collection("users").document(assignedProfessionalId).get()
                        .addOnSuccessListener { userDocument ->
                            val firstname = userDocument.getString("firstname") ?: "Unknown"
                            val lastname = userDocument.getString("lastname") ?: "Unknown"
                            val jobTitle = userDocument.getString("jobTitle") ?: "Unknown"
                            professionalEmail = userDocument.getString("email") ?: "Unknown"
                            professionalName = "$firstname $lastname"
                            professionalTitle = jobTitle
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                navController.context,
                                "Failed to fetch professional details.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manage Job",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Job Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Title: $jobTitle")
        Text(text = "Description: $jobDescription")
        Text(text = "Pay: $jobPay")
        Text(text = "Location: $jobLocation")
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Professional Assigned",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Name: $professionalName")
        Text(text = "Title: $professionalTitle")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Back", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                // Update job status to "completed"
                db.collection("jobs").document(jobId)
                    .update("status", "completed")
                    .addOnSuccessListener {
                        // Remove the job from the "upcomingjobs" collection
                        db.collection("upcomingjobs")
                            .whereEqualTo("Title", jobTitle)
                            .whereEqualTo("AssignedTo", professionalEmail)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                for (document in querySnapshot) {
                                    db.collection("upcomingjobs").document(document.id).delete()
                                }
                                // Add a record to the "recentjobs" collection
                                val recentJob = hashMapOf(
                                    "Status" to "Completed",
                                    "Title" to jobTitle,
                                    "Pay" to jobPay,
                                    "completedBy" to professionalEmail
                                )
                                db.collection("recentjobs").add(recentJob)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            navController.context,
                                            "Job marked as completed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            navController.context,
                                            "Error adding to recent jobs: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    navController.context,
                                    "Error removing from upcoming jobs: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            navController.context,
                            "Error updating job status: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(text = "Completed", color = Color.White)
        }
    }
}
