package com.example.fitgenius.screens

import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormScreen(
    navController: NavController,
    userProfile: UserProfile,
    onProfileComplete: (UserProfile, Boolean, Uri?) -> Unit,
    isEditing: Boolean
) {
    var profileState by remember { mutableStateOf(userProfile) }
    val exerciseOptions = listOf("Correr", "Nadar", "Ciclismo", "Pesas", "Yoga", "Pilates", "CrossFit")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Perfil" else "Tu Perfil (Paso 2)") },
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
                    onProfileComplete(profileState, isEditing, null)
                    if (isEditing) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (isEditing) "GUARDAR CAMBIOS" else "GENERAR MI PLAN",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Datos Biométricos
            FormSection(title = "Datos Biométricos") {
                OutlinedTextField(value = profileState.age.toString().takeIf { it != "0" } ?: "", onValueChange = { profileState = profileState.copy(age = it.toIntOrNull() ?: 0) }, label = { Text("Edad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = profileState.weight.toString().takeIf { it != "0.0" } ?: "", onValueChange = { profileState = profileState.copy(weight = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Peso (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = profileState.height.toString().takeIf { it != "0.0" } ?: "", onValueChange = { profileState = profileState.copy(height = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Altura (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                DropdownMenuField(options = listOf("Hombre", "Mujer"), selectedOption = profileState.gender, onSelect = { profileState = profileState.copy(gender = it) }, label = "Género")
                if (profileState.gender == "Mujer") {
                    DropdownMenuField(options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"), selectedOption = profileState.menstrualPhase ?: "Menstruación", onSelect = { profileState = profileState.copy(menstrualPhase = it) }, label = "Fase Menstrual")
                }
            }

            // Objetivos y Estilo de Vida
            FormSection(title = "Objetivos y Estilo de Vida") {
                DropdownMenuField(options = listOf("Pérdida de peso", "Ganar músculo", "Mantenimiento", "Tonificación"), selectedOption = profileState.goal, onSelect = { profileState = profileState.copy(goal = it) }, label = "Mi objetivo principal")
                DropdownMenuField(options = listOf("Sedentario", "Moderado", "Activo", "Muy activo"), selectedOption = profileState.activityLevel, onSelect = { profileState = profileState.copy(activityLevel = it) }, label = "Mi nivel de actividad")
                DropdownMenuField(options = (3..7).map { "$it días" }, selectedOption = "${profileState.trainingDays} días", onSelect = { profileState = profileState.copy(trainingDays = it.removeSuffix(" días").toIntOrNull() ?: 3) }, label = "Días de entreno semanales")
                DropdownMenuField(options = listOf("En casa", "Gimnasio"), selectedOption = profileState.trainingLocation, onSelect = { profileState = profileState.copy(trainingLocation = it) }, label = "¿Dónde entrenas?")
            }

            // Preferencias
            FormSection(title = "Preferencias") {
                MultiSelectChipGroup(title = "Ejercicios que te gustan", options = exerciseOptions, selectedOptions = profileState.favoriteExercises) { newSelection ->
                    profileState = profileState.copy(favoriteExercises = newSelection)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = profileState.allergies, onValueChange = { profileState = profileState.copy(allergies = it) }, label = { Text("Alergias o intolerancias") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ej: Lactosa, gluten, nueces...") })
                OutlinedTextField(value = profileState.foodPreferences, onValueChange = { profileState = profileState.copy(foodPreferences = it) }, label = { Text("Preferencias de comida") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ej: Vegano, vegetariano, sin carne roja...") })
            }
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectChipGroup(title: String, options: List<String>, selectedOptions: List<String>, onSelectionChanged: (List<String>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = {
                        val newSelection = if (selectedOptions.contains(option)) {
                            selectedOptions.filterNot { it == option }
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}
