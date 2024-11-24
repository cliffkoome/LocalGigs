package com.example.localgigs.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun JobViewPage(
    navController: NavController,
    jobTitle: String,
    jobLocation: String,
    jobPay: String,
    jobDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = jobTitle,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Location: $jobLocation",
                fontSize = 16.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Pay: KES $jobPay",
                fontSize = 16.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = androidx.compose.ui.graphics.Color.LightGray, thickness = 1.dp)
            Text(
                text = "Job Description",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = jobDescription,
                fontSize = 14.sp
            )
        }

        // Apply Job Button
        Button(
            onClick = { /* Add functionality here */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(text = "Apply Job", fontSize = 16.sp)
        }
    }
}
