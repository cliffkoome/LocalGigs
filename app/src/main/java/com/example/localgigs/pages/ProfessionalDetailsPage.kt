package com.example.localgigs.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localgigs.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfessionalDetailsPage(userEmail: String) {
    val user = remember { mutableStateOf<User?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Fetch the user details based on userEmail
    LaunchedEffect(userEmail) {
        try {
            val db = FirebaseFirestore.getInstance()
            val querySnapshot = db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                errorMessage.value = "User not found."
            } else {
                val fetchedUser = querySnapshot.documents.first().toObject(User::class.java)
                user.value = fetchedUser
            }
        } catch (e: Exception) {
            errorMessage.value = "Error fetching user details: ${e.localizedMessage}"
        } finally {
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = "Professional Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Loading Indicator
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Error Message if Any
            errorMessage.value?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
            }

            // User Data Section
            user.value?.let { user ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Job Title: ${user.jobTitle}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Name: ${user.firstname} ${user.lastname}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Email: ${user.email}",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
