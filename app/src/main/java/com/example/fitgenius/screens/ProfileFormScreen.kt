package com.example.fitgenius.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 3

    val progress by animateFloatAsState(targetValue = (currentStep -1).toFloat() / totalSteps.toFloat(), label = "progress")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Perfil" else "Tu Perfil (Paso $currentStep de $totalSteps)") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "ANTERIOR")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            onProfileComplete(profileState, isEditing, null)
                            if (isEditing) navController.popBackStack() else navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = if (currentStep < totalSteps) "SIGUIENTE" else if (isEditing) "GUARDAR CAMBIOS" else "GENERAR MI PLAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.background)))
                .padding(paddingValues)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                when (currentStep) {
                    1 -> GenderStep(profileState = profileState, onProfileChange = { profileState = it })
                    2 -> PersonalDataStep(profileState = profileState, onProfileChange = { profileState = it })
                    3 -> PreferencesStep(profileState = profileState, onProfileChange = { profileState = it })
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GenderStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    FormSection("Tu Género") {
        DropdownMenuField(
            options = listOf("Hombre", "Mujer"),
            selectedOption = profileState.gender,
            onSelect = { onProfileChange(profileState.copy(gender = it)) },
            label = "Género"
        )
    }
}

@Composable
fun PersonalDataStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    FormSection("Tus Medidas") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = profileState.age.toString().takeIf { it != "0" } ?: "",
                onValueChange = { onProfileChange(profileState.copy(age = it.toIntOrNull() ?: 0)) },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = profileState.weight.toString().takeIf { it != "0.0" } ?: "",
                onValueChange = { onProfileChange(profileState.copy(weight = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = profileState.height.toString().takeIf { it != "0.0" } ?: "",
            onValueChange = { onProfileChange(profileState.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            label = { Text("Altura (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        if (profileState.gender == "Mujer") {
            DropdownMenuField(
                options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"),
                selectedOption = profileState.menstrualPhase ?: "Menstruación",
                onSelect = { onProfileChange(profileState.copy(menstrualPhase = it)) },
                label = "Fase Menstrual"
            )
        }
    }
}

@Composable
fun PreferencesStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    val exerciseOptions = listOf("Correr", "Nadar", "Ciclismo", "Pesas", "Yoga", "Pilates", "CrossFit")

    FormSection("Tus Preferencias") {
        DropdownMenuField(
            options = listOf("En casa", "Gimnasio"),
            selectedOption = profileState.trainingLocation,
            onSelect = { onProfileChange(profileState.copy(trainingLocation = it)) },
            label = "Lugar de entrenamiento"
        )
        OutlinedTextField(
            value = profileState.allergies,
            onValueChange = { onProfileChange(profileState.copy(allergies = it)) },
            label = { Text("Alergias o intolerancias") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Lactosa, gluten, nueces...") }
        )
        OutlinedTextField(
            value = profileState.foodPreferences,
            onValueChange = { onProfileChange(profileState.copy(foodPreferences = it)) },
            label = { Text("Preferencias de comida") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Vegano, sin carne roja...") }
        )
        MultiSelectChipGroup(
            title = "Ejercicios que te gustan (opcional)",
            options = exerciseOptions,
            selectedOptions = profileState.favoriteExercises
        ) { newSelection ->
            onProfileChange(profileState.copy(favoriteExercises = newSelection))
        }
    }
}


@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(options: List<String>, selectedOption: String, onSelect: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectChipGroup(title: String, options: List<String>, selectedOptions: List<String>, onSelectionChanged: (List<String>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.take(4).forEach { option -> // Muestra los primeros 4 para una línea limpia
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = {
                        val newSelection = if (selectedOptions.contains(option)) selectedOptions.filterNot { it == option } else selectedOptions + option
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}