package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.AuthViewModel
import com.example.localgigs.AuthState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun ProfilePage(modifier: Modifier = Modifier, authViewModel: AuthViewModel, navController: NavController) {
    val authState = authViewModel.authState.observeAsState()
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }  // Add skills state variable

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: ""
                        lastname = document.getString("lastname") ?: ""
                        email = document.getString("email") ?: ""
                        jobTitle = document.getString("jobTitle") ?: ""
                        skills = document.getString("skills") ?: ""  // Retrieve skills
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(navController.context, "Error loading user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFFFFFFF)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Page",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            label = { Text("Job Title") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Skills field
        OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Skills") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Update user details including skills in Firestore
                if (userId != null) {
                    val userMap = mapOf(
                        "firstname" to firstname,
                        "lastname" to lastname,
                        "email" to email,
                        "jobTitle" to jobTitle,
                        "skills" to skills  // Include skills in the update
                    )

                    db.collection("users").document(userId).update(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(navController.context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(navController.context, "Error updating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Changes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Handle password change
                if (password.isNotEmpty()) {
                    user?.updatePassword(password)
                        ?.addOnSuccessListener {
                            Toast.makeText(navController.context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener { exception ->
                            Toast.makeText(navController.context, "Error updating password: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Change Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            // Handle logout
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text(text = "Logout", color = Color.Black)
        }
    }
}
