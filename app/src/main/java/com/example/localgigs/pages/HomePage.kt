package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    isProfessional: Boolean
) {
    val authState = authViewModel.authState.observeAsState()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    // State to hold user's first name
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header with User's First Name
        Text(
            text = "Welcome, $firstname!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isProfessional) "Explore Gigs Near You" else "Find Reliable Professionals",
            fontSize = 20.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile and Sign Out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { authViewModel.signout() }) {
                Text("Sign Out", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Redirect to Login if unauthenticated
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }
}
