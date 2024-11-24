package com.example.localgigs.pages

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
                jobList = snapshot.documents.map { it.data ?: emptyMap() }
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
                val title = job["title"] as? String ?: ""
                title.contains(searchQuery, ignoreCase = true)
            }) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = job["title"] as? String ?: "No Title",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Location: ${(job["location"] as? String) ?: "Unknown"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Pay: KES ${(job["pay"] as? Double)?.toString() ?: "Negotiable"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
