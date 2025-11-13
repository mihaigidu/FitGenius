package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.UserProfile

@Composable
fun RegisterScreen(navController: NavController, onUserRegistered: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Hombre") }
    var menstrualPhase by remember { mutableStateOf("Menstruación") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Mantenimiento") }
    var activityLevel by remember { mutableStateOf("Moderado") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro FitGenius", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Altura (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(16.dp))

        Text("Género:", style = MaterialTheme.typography.titleSmall)
        DropdownMenuExample(
            options = listOf("Hombre", "Mujer"),
            selectedOption = gender,
            onSelect = { gender = it }
        )

        if (gender == "Mujer") {
            Spacer(Modifier.height(8.dp))
            Text("Fase menstrual:", style = MaterialTheme.typography.titleSmall)
            DropdownMenuExample(
                options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"),
                selectedOption = menstrualPhase,
                onSelect = { menstrualPhase = it }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Objetivo:", style = MaterialTheme.typography.titleSmall)
        DropdownMenuExample(
            options = listOf("Pérdida de peso", "Ganar músculo", "Mantenimiento", "Tonificación"),
            selectedOption = goal,
            onSelect = { goal = it }
        )

        Spacer(Modifier.height(8.dp))
        Text("Nivel de actividad:", style = MaterialTheme.typography.titleSmall)
        DropdownMenuExample(
            options = listOf("Sedentario", "Moderado", "Activo", "Muy activo"),
            selectedOption = activityLevel,
            onSelect = { activityLevel = it }
        )

        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            val userProfile = UserProfile(
                name = name,
                email = email,
                gender = gender,
                menstrualPhase = if (gender == "Mujer") menstrualPhase else null,
                age = age.toIntOrNull(),
                weight = weight.toDoubleOrNull(),
                height = height.toDoubleOrNull(),
                goal = goal,
                activityLevel = activityLevel
            )
            onUserRegistered(userProfile)
            navController.navigate("home")
        }) {
            Text("Registrarse")
        }
    }
}

@Composable
fun DropdownMenuExample(options: List<String>, selectedOption: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}