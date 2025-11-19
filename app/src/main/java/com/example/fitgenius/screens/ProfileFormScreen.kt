package com.example.fitgenius.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
    val totalSteps = 4

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
                    3 -> GoalStep(profileState = profileState, onProfileChange = { profileState = it })
                    4 -> PreferencesStep(profileState = profileState, onProfileChange = { profileState = it })
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GenderStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    FormSection("Información Básica") {
        Text(
            text = "Cuéntanos sobre ti para personalizar tu experiencia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text("Género", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val genders = listOf("Hombre", "Mujer")
        genders.forEach { gender ->
            GenderButton(
                text = gender,
                isSelected = profileState.gender == gender,
                onClick = { onProfileChange(profileState.copy(gender = gender)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


@Composable
fun PersonalDataStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    FormSection("Datos Físicos") {
        Text(
            text = "Esta información nos ayuda a crear rutinas más precisas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Edad (años)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = profileState.age.toString().takeIf { it != "0" } ?: "",
            onValueChange = { onProfileChange(profileState.copy(age = it.toIntOrNull() ?: 0)) },
            placeholder = { Text("Ej: 25") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Peso (kg)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = profileState.weight.toString().takeIf { it != "0.0" } ?: "",
            onValueChange = { onProfileChange(profileState.copy(weight = it.toDoubleOrNull() ?: 0.0)) },
            placeholder = { Text("Ej: 70") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Altura (cm)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = profileState.height.toString().takeIf { it != "0.0" } ?: "",
            onValueChange = { onProfileChange(profileState.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            placeholder = { Text("Ej: 170") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (profileState.gender == "Mujer") {
            DropdownMenuField(
                options = listOf("Menstruación", "Folicular", "Ovulación", "Lútea"),
                selectedOption = profileState.menstrualPhase ?: "Menstruación",
                onSelect = { onProfileChange(profileState.copy(menstrualPhase = it)) },
                label = "Fase Menstrual"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Estos datos son opcionales pero nos ayudan a crear mejores recomendaciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalStep(profileState: UserProfile, onProfileChange: (UserProfile) -> Unit) {
    val goals = listOf(
        "Perder Peso" to "Quemar grasa y reducir medidas",
        "Ganar Masa Muscular" to "Aumentar músculo y fuerza",
        "Mantener Forma" to "Tonificar y mantenerse activo",
        "Mejorar Resistencia" to "Aumentar capacidad cardiovascular"
    )

    FormSection("Tu Objetivo") {
        Text(
            text = "¿Qué quieres lograr con FitAI Coach?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        goals.forEach { (title, subtitle) ->
            GoalButton(
                title = title,
                subtitle = subtitle,
                isSelected = profileState.goal == title,
                onClick = { onProfileChange(profileState.copy(goal = title)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GoalButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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