package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crea tu Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val userProfile = UserProfile(
                        id = "", // You might want to generate a unique ID here
                        name = name,
                        email = email,
                        gender = gender,
                        menstrualPhase = if (gender == "Mujer") menstrualPhase else null,
                        age = age.toIntOrNull() ?: 0,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        height = height.toDoubleOrNull() ?: 0.0,
                        goal = goal,
                        activityLevel = activityLevel
                    )
                    onUserRegistered(userProfile)
                    navController.navigate("home") { popUpTo("login") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Crear Perfil y Generar Plan", color = MaterialTheme.colorScheme.onTertiary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Section: Basic Info
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información Básica", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                }
            }

            // Section: Biometrics
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos Biométricos", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Edad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Peso (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Altura (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                }
            }

            // Section: Goals & Lifestyle
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estilo de Vida y Objetivos", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    DropdownMenuField(options = listOf("Hombre", "Mujer"), selectedOption = gender, onSelect = { gender = it }, label = "Género")

                    if (gender == "Mujer") {
                        Spacer(Modifier.height(8.dp))
                        DropdownMenuField(options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"), selectedOption = menstrualPhase, onSelect = { menstrualPhase = it }, label = "Fase Menstrual")
                    }
                    Spacer(Modifier.height(8.dp))
                    DropdownMenuField(options = listOf("Pérdida de peso", "Ganar músculo", "Mantenimiento", "Tonificación"), selectedOption = goal, onSelect = { goal = it }, label = "Objetivo Principal")
                    Spacer(Modifier.height(8.dp))
                    DropdownMenuField(options = listOf("Sedentario", "Moderado", "Activo", "Muy activo"), selectedOption = activityLevel, onSelect = { activityLevel = it }, label = "Nivel de Actividad")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(options: List<String>, selectedOption: String, onSelect: (String) -> Unit, label: String) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
