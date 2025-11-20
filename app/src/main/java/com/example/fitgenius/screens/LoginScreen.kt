package com.example.fitgenius.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.MutedGreen

val AuthLightGreen = Color(0xFFF2FAF6)

enum class AuthMode { 
    Login, 
    Register 
} 

@Composable
fun AuthScreen(navController: NavController, onUserRegistered: (UserProfile) -> Unit) {
    var currentScreen by remember { mutableStateOf(AuthMode.Login) }

    Scaffold(containerColor = AuthLightGreen) {
        padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(Icons.Default.FitnessCenter, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("FitGenius", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tu entrenador personal con IA", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            AuthModeSelector(currentScreen) { currentScreen = it }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentScreen == AuthMode.Login) {
                LoginContent(navController)
            } else {
                RegisterContent(navController, onUserRegistered)
            }

            Spacer(modifier = Modifier.weight(1f))
            Text("Powered by AI • Diseñado para tu éxito", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AuthModeSelector(currentScreen: AuthMode, onScreenChange: (AuthMode) -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MutedGreen)) {
        Row(modifier = Modifier.padding(4.dp)) {
            Button(
                onClick = { onScreenChange(AuthMode.Login) },
                shape = RoundedCornerShape(20.dp),
                colors = if (currentScreen == AuthMode.Login) ButtonDefaults.buttonColors(containerColor = Color.White) else ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Iniciar Sesión", color = if (currentScreen == AuthMode.Login) MaterialTheme.colorScheme.primary else Color.Gray)
            }
            Button(
                onClick = { onScreenChange(AuthMode.Register) },
                shape = RoundedCornerShape(20.dp),
                colors = if (currentScreen == AuthMode.Register) ButtonDefaults.buttonColors(containerColor = Color.White) else ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Registrarse", color = if (currentScreen == AuthMode.Register) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }
    }
}

@Composable
fun LoginContent(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Bienvenido de nuevo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Ingresa tus credenciales para continuar", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Text("Email", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                placeholder = { Text("tu@email.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Contraseña", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Iniciar Sesión")
            }
        }
    }
}

@Composable
fun RegisterContent(navController: NavController, onUserRegistered: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Crear cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Completa el formulario para comenzar tu transformación", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Text("Nombre", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("Tu nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Email", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                placeholder = { Text("tu@email.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Contraseña", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { 
                val userProfile = UserProfile(name = name, email = email)
                onUserRegistered(userProfile)
                navController.navigate("profile_form/false") 
            }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Crear Cuenta")
            }
        }
    }
}
