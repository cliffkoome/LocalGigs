package com.example.localgigs.model

data class User(
    val jobTitle: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val userType: String = "",
    val name: String = "" // This can be used for the full name if needed
)
