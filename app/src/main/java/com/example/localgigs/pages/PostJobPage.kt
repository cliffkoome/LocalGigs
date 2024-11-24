package com.example.localgigs.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PostJobPage() {
    val db = FirebaseFirestore.getInstance()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var skillsRequired by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    Column {
        TextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") })
        TextField(value = description, onValueChange = { description = it }, label = { Text("Job Description") })
        TextField(value = skillsRequired, onValueChange = { skillsRequired = it }, label = { Text("Skills Required") })
        TextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget") })

        Button(onClick = {
            val job = hashMapOf(
                "title" to title,
                "description" to description,
                "skillsRequired" to skillsRequired.split(","),
                "budget" to budget.toDouble(),
                "clientId" to FirebaseAuth.getInstance().currentUser?.uid,
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("jobs").add(job)
        }) {
            Text("Post Job")
        }
    }
}
