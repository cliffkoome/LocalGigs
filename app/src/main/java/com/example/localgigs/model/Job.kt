package com.example.localgigs.model

data class Job(
    val jobId: String = "",
    val title: String = "",
    val category: String = "",
    val location: String = "",
    val pay: Double = 0.0,
    val jobType: String = "",
    val skills: String = "",
    val description: String = "",
    val postedBy: String = "",
    val status: String = "" // Add the status field
)
