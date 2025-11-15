package com.example.fitgenius.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userProfile: UserProfile,
    onGenerateNewPlan: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showGeneratePlanDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(userProfile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(userProfile.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuItem("Cuenta") { navController.navigate("account_settings") }
            ProfileMenuItem("Preferencias") { navController.navigate("profile_form/true") }
            ProfileMenuItem("Generar nuevo plan") { showGeneratePlanDialog = true }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { showLogoutDialog = true }) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showDeleteDialog = true }) {
                Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Cerrar sesión",
            message = "¿Estás seguro de que quieres cerrar sesión?",
            onConfirm = { /* TODO: Handle logout */ showLogoutDialog = false },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Eliminar cuenta",
            message = "¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.",
            onConfirm = { /* TODO: Handle delete account */ showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showGeneratePlanDialog) {
        ConfirmationDialog(
            title = "Generar nuevo plan",
            message = "Se generará un nuevo plan de entrenamiento y dieta. El plan actual se reemplazará.",
            onConfirm = {
                onGenerateNewPlan()
                showGeneratePlanDialog = false
                navController.popBackStack()
            },
            onDismiss = { showGeneratePlanDialog = false }
        )
    }
}

@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("No")
            }
        }
    )
}