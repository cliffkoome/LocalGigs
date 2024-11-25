package com.example.localgigs.model

data class User(
    val id: String = "", // Firestore document ID
    val jobTitle: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val userType: String = ""
)
