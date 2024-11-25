package com.example.localgigs.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SearchPage(modifier: Modifier = Modifier, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var jobList by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch jobs from Firestore
    LaunchedEffect(Unit) {
        db.collection("jobs")
            .get()
            .addOnSuccessListener { snapshot ->
                jobList = snapshot.documents.map { document ->
                    val jobData = document.data ?: emptyMap<String, Any>()
                    jobData + mapOf("jobId" to document.id) // Add jobId to each job
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("Search Jobs") },
            shape = RoundedCornerShape(8.dp)
        )

        // Job List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(jobList.filter { job ->
                // Filter jobs based on title search query and exclude jobs with 'assigned' status
                val title = job["title"] as? String ?: ""
                val status = job["status"] as? String ?: ""
                title.contains(searchQuery, ignoreCase = true) && status != "assigned" && status != "completed"
            }) { job ->
                val jobId = job["jobId"] as? String ?: ""
                val title = job["title"] as? String ?: "No Title"
                val location = job["location"] as? String ?: "Unknown"
                val pay = job["pay"] as? Double ?: 0.0
                val description = job["description"] as? String ?: "No Description"
                val professionalEmail = job["professionalEmail"] as? String ?: ""  // Assume this field is in Firestore

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            // Pass the job details along with professionalEmail to the JobViewPage
                            navController.navigate("jobView/$jobId/$title/$location/$pay/$description/$professionalEmail")
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Location: $location",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Pay: KES ${pay.toString()}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

