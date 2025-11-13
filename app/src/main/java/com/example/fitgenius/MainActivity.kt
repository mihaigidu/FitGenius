package com.example.fitgenius

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.fitgenius.ui.theme.FitGeniusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitGeniusTheme {
                Surface {
                    FitGeniusApp()
                }
            }
        }
    }
}

@Composable
fun FitGeniusApp() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
    }
