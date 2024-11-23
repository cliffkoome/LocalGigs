package com.example.localgigs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.example.localgigs.ui.theme.LocalGigsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            LocalGigsTheme {
                MainScreen(
                    authViewModel = authViewModel
                )
            }
        }
    }
}
