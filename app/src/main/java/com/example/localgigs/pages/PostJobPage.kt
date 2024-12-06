package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobPage(navController: NavController, userEmail: String) {
    // State variables for form fields
    var jobTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var pay by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }

    // Firestore instance
    val db = FirebaseFirestore.getInstance()

    // Handle form submission
    fun postJob() {
        if (jobTitle.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty() && pay.isNotEmpty() && location.isNotEmpty() && jobType.isNotEmpty() && skills.isNotEmpty()) {
            // Create jobId using email and job title (email.jobtitle format)
            val jobId = "$userEmail.$jobTitle"

            // Create a job object with jobId and postedBy field
            val jobData = hashMapOf(
                "jobId" to jobId,  // Add the jobId field
                "title" to jobTitle,
                "description" to description,
                "category" to category,
                "pay" to pay.toDouble(),
                "location" to location,
                "jobType" to jobType,
                "skills" to skills,
                "postedBy" to userEmail  // Add the postedBy field
            )

            // Add the job data to Firestore
            db.collection("jobs")
                .add(jobData)
                .addOnSuccessListener {
                    Toast.makeText(navController.context, "Job posted successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate back to the home page or jobs list
                    navController.popBackStack()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(navController.context, "Error posting job: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(navController.context, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Post a Job",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Job Title Input
        TextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            label = { Text("Job Title") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Description Input
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            maxLines = 5
        )

        // Category Input
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Pay Input
        TextField(
            value = pay,
            onValueChange = { pay = it },
            label = { Text("Pay (KES)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Location Input
        TextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Job Type TextBox
        TextField(
            value = jobType,
            onValueChange = { jobType = it },
            label = { Text("Job Type (Full-time, Part-time, Freelance)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Skills Required Input
        TextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Skills Required (comma separated)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Submit Button
        Button(
            onClick = { postJob() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Text(text = "Post Job", color = Color.White)
        }
    }
}
