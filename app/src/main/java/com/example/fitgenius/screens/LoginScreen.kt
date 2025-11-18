package com.example.fitgenius.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.ui.theme.PetrolGreen
import com.example.fitgenius.ui.theme.BrightBlue
import com.example.fitgenius.ui.theme.PureWhite

@Composable
fun LoginScreen(navController: NavController) {

    val gradient = Brush.verticalGradient(
        colors = listOf(
            PetrolGreen.copy(alpha = 0.90f),
            BrightBlue.copy(alpha = 0.70f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(32.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "FitGenius",
                style = MaterialTheme.typography.displayMedium,
                color = PureWhite,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tu entrenador personal con IA",
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(120.dp))

            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite)
            ) {
                Text(
                    "Comenzar ahora",
                    color = PetrolGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("home") }) {
                Text(
                    "Ya tengo una cuenta",
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
