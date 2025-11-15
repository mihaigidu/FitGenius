package com.example.fitgenius.screens

import androidx.compose.foundation.clickable
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
import com.example.fitgenius.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormScreen(
    navController: NavController,
    userProfile: UserProfile,
    onProfileComplete: (UserProfile) -> Unit,
    isEditing: Boolean = false
) {
    var profileState by remember(userProfile) { mutableStateOf(userProfile) }

    val exerciseOptions = listOf("Correr", "Nadar", "Ciclismo", "Levantamiento de pesas", "Yoga", "Pilates", "CrossFit")

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (isEditing) "Editar Perfil" else "Paso 2 de 2: Completa tu Perfil")
            })
        },
        bottomBar = {
            Button(
                onClick = {
                    onProfileComplete(profileState)
                    if (isEditing) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(
                    text = if (isEditing) "Guardar Cambios" else "Crear Perfil y Generar Plan",
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section: Biometrics
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos Biométricos", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = profileState.age.toString().takeIf { it != "0" } ?: "", onValueChange = { profileState = profileState.copy(age = it.toIntOrNull() ?: 0) }, label = { Text("Edad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = profileState.weight.toString().takeIf { it != "0.0" } ?: "", onValueChange = { profileState = profileState.copy(weight = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Peso (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = profileState.height.toString().takeIf { it != "0.0" } ?: "", onValueChange = { profileState = profileState.copy(height = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Altura (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                }
            }

            // Section: Goals & Lifestyle
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estilo de Vida y Objetivos", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    DropdownMenuField(options = listOf("Hombre", "Mujer"), selectedOption = profileState.gender, onSelect = { profileState = profileState.copy(gender = it) }, label = "Género")
                    if (profileState.gender == "Mujer") {
                        Spacer(Modifier.height(8.dp))
                        DropdownMenuField(options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"), selectedOption = profileState.menstrualPhase ?: "Menstruación", onSelect = { profileState = profileState.copy(menstrualPhase = it) }, label = "Fase Menstrual")
                    }
                    Spacer(Modifier.height(8.dp))
                    DropdownMenuField(options = listOf("Pérdida de peso", "Ganar músculo", "Mantenimiento", "Tonificación"), selectedOption = profileState.goal, onSelect = { profileState = profileState.copy(goal = it) }, label = "Objetivo Principal")
                    Spacer(Modifier.height(8.dp))
                    DropdownMenuField(options = listOf("Sedentario", "Moderado", "Activo", "Muy activo"), selectedOption = profileState.activityLevel, onSelect = { profileState = profileState.copy(activityLevel = it) }, label = "Nivel de Actividad")
                }
            }

            // Section: Training Preferences
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preferencias de Entrenamiento", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    DropdownMenuField(options = listOf("3", "4", "5", "6"), selectedOption = profileState.trainingDays.toString(), onSelect = { profileState = profileState.copy(trainingDays = it.toIntOrNull() ?: 3) }, label = "Días de entrenamiento por semana")
                    Spacer(Modifier.height(8.dp))
                    DropdownMenuField(options = listOf("Casa", "Gimnasio"), selectedOption = profileState.trainingLocation, onSelect = { profileState = profileState.copy(trainingLocation = it) }, label = "Lugar de entrenamiento")

                    Text("Ejercicios Favoritos (opcional)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                    exerciseOptions.forEach { exercise ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                val currentFavorites = profileState.favoriteExercises.toMutableList()
                                if (currentFavorites.contains(exercise)) {
                                    currentFavorites.remove(exercise)
                                } else {
                                    currentFavorites.add(exercise)
                                }
                                profileState = profileState.copy(favoriteExercises = currentFavorites)
                            }.padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = profileState.favoriteExercises.contains(exercise), onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(exercise)
                        }
                    }
                }
            }

            // Section: Diet Preferences
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preferencias de Dieta", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = profileState.allergies, onValueChange = { profileState = profileState.copy(allergies = it) }, label = { Text("Alergias (separadas por comas)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = profileState.foodPreferences, onValueChange = { profileState = profileState.copy(foodPreferences = it) }, label = { Text("Otras preferencias (vegano, etc.)") }, modifier = Modifier.fillMaxWidth())
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