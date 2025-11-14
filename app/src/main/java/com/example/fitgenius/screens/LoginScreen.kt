package com.example.fitgenius.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.ui.theme.DarkGreen
import com.example.fitgenius.ui.theme.Teal

@Composable
fun LoginScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkGreen, Teal)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "FitGenius",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                "Tu entrenador personal de IA",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(128.dp))

            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Comenzar Ahora (Registrarse)", color = MaterialTheme.colorScheme.onTertiary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Lógica de inicio de sesión aquí */ navController.navigate("home") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Ya tengo una cuenta (Demo)", color = Color.White)
            }
        }
    }
}
