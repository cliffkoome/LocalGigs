package com.example.localgigs.model


data class JobApplication(
    val name: String = "",
    val email: String = "",
    val uid: String = "", // Add this field to store applicant's UID
    val experience: Int = 0,
    val skills: String = ""
)
