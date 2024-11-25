package com.example.localgigs.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.localgigs.AuthState
import com.example.localgigs.AuthViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation


@Composable
fun SignupPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Client") } // Default to "Client"
    var jobTitle by remember { mutableStateOf("") } // Added Job Title
    var isValid by remember { mutableStateOf(true) } // Basic validation state

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                // Retrieve the userType from AuthState
                val userType = (authState.value as AuthState.Authenticated).userType
                // Navigate to the correct home screen based on userType
                if (userType == "Client") {
                    navController.navigate("clientHome") // Navigate to client home screen
                } else {
                    navController.navigate("home") // Navigate to professional home screen
                }
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
                ).show()
            }
            else -> Unit
        }
    }


    // Basic validation to enable the "Create account" button
    isValid = email.isNotBlank() && password.length >= 6 && firstname.isNotBlank() && lastname.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Added padding to avoid elements from touching screen edges
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "SignUp", fontSize = 32.sp, style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // First Name
        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text(text = "First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text(text = "Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Job Title
        OutlinedTextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            label = { Text(text = "Job Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = password.length < 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Type Radio Buttons
        Text(text = "Select user type:", fontSize = 18.sp)

        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Client Option
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = userType == "Client",
                    onClick = { userType = "Client" }
                )
                Text(text = "Client", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Professional Option
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = userType == "Professional",
                    onClick = { userType = "Professional" }
                )
                Text(text = "Professional", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create Account Button
        Button(
            onClick = { authViewModel.signup(email, password, firstname, lastname, userType, jobTitle) },
            enabled = isValid && authState.value != AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Create account")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Login redirect text
        TextButton(onClick = { navController.navigate("login") }) {
            Text(text = "Already have an account? Login")
        }

        // Show Loading indicator if the state is loading
        if (authState.value == AuthState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
