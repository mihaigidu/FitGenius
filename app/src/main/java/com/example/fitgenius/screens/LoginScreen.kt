package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido a FitGenius", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") }
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") }
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(onClick = {
            // Por ahora, redirige al registro para capturar el perfil
            navController.navigate("register")
        }) {
            Text("Iniciar sesión")
        }
        
        Spacer(Modifier.height(8.dp))
        
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}