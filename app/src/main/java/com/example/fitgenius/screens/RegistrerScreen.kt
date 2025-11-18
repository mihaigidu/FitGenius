package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.ui.theme.PetrolGreen
import com.example.fitgenius.ui.theme.PureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    onUserRegistered: (UserProfile) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crea tu Cuenta",
                        fontWeight = FontWeight.Bold,
                        color = PetrolGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PetrolGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val profile = UserProfile(name = name, email = email)
                    onUserRegistered(profile)
                    navController.navigate("profile_form/false")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetrolGreen),
                shape = MaterialTheme.shapes.large,
                enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
            ) {
                Text("CONTINUAR", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                "Vamos a comenzar con tus datos básicos",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = PetrolGreen,
                    fontWeight = FontWeight.SemiBold
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mín. 6 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
