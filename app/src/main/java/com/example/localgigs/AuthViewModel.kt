package com.example.localgigs

import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val _userType = MutableLiveData<String?>()
    val userType: MutableLiveData<String?> = _userType

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated(userType = "client") // Default to "client"
            fetchUserType()
        }
    }

    private fun fetchUserType() {
        val userId = currentUserId
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val type = document.getString("userType")
                        _userType.value = type ?: "client" // Default to client if not found
                        // Update the AuthState with the fetched userType
                        _authState.value = AuthState.Authenticated(userType = _userType.value ?: "client")
                    } else {
                        _userType.value = "client" // Default to client if no user type is found
                        _authState.value = AuthState.Authenticated(userType = "client")
                    }
                }
                .addOnFailureListener {
                    _userType.value = "client" // Default to client in case of an error
                    _authState.value = AuthState.Authenticated(userType = "client")
                }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated(userType = "client") // Default userType
                    fetchUserType() // Fetch the user type after successful login
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(email: String, password: String, firstname: String, lastname: String, userType: String, jobTitle: String) {
        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        // Check password length
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        _authState.value = AuthState.Loading

        // Create a new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Create a map to store user details including the userType
                    val userMap = mapOf(
                        "firstname" to firstname,
                        "lastname" to lastname,
                        "email" to email,
                        "uid" to (userId ?: ""),
                        "jobTitle" to jobTitle,
                        "userType" to userType // Storing the user type
                    )

                    // Store user details in Firestore
                    userId?.let {
                        firestore.collection("users").document(it).set(userMap)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated(userType = userType) // Pass userType here
                            }
                            .addOnFailureListener { exception ->
                                _authState.value = AuthState.Error("Failed to save user data: ${exception.message}")
                            }
                    } ?: run {
                        _authState.value = AuthState.Error("User ID is null")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _userType.value = null // Reset user type on logout
    }
}

sealed class AuthState {
    data class Authenticated(val userType: String) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
