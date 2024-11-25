package com.example.localgigs.model


data class JobApplication(
    val name: String = "",
    val email: String = "",
    val uid: String = "", // Added UID field
    val experience: Int = 0,
    val skills: String = ""
)
