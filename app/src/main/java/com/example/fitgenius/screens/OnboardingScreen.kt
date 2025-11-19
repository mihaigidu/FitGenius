package com.example.fitgenius.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitgenius.data.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    onOnboardingComplete: (UserProfile) -> Unit
) {
    val pagerState = rememberPagerState { 3 } // 3 páginas
    val coroutineScope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf(UserProfile()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Crea tu Perfil") }) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    Button(onClick = { 
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } 
                    }) {
                        Text("Anterior")
                    }
                }
                Button(onClick = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onOnboardingComplete(userProfile)
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                }) {
                    Text(if (pagerState.currentPage < 2) "Siguiente" else "Finalizar y Crear Plan")
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = false // Solo se navega con los botones
        ) {
            when (it) {
                0 -> GenderPage(userProfile) { newProfile -> userProfile = newProfile }
                1 -> PhysicalDataPage(userProfile) { newProfile -> userProfile = newProfile }
                2 -> PreferencesPage(userProfile) { newProfile -> userProfile = newProfile }
            }
        }
    }
}

@Composable
fun GenderPage(userProfile: UserProfile, onProfileChanged: (UserProfile) -> Unit) {
    // Implementación de la página de género
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selecciona tu género", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        DropdownMenuField(options = listOf("Hombre", "Mujer", "Otro"), selectedOption = userProfile.gender, onSelect = { onProfileChanged(userProfile.copy(gender = it)) }, label = "Género")
    }
}

@Composable
fun PhysicalDataPage(userProfile: UserProfile, onProfileChanged: (UserProfile) -> Unit) {
    // Implementación de la página de datos físicos
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cuéntanos sobre ti", style = MaterialTheme.typography.headlineSmall)
        // Campos para altura, peso, edad, etc.
    }
}

@Composable
fun PreferencesPage(userProfile: UserProfile, onProfileChanged: (UserProfile) -> Unit) {
    // Implementación de la página de preferencias
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tus preferencias", style = MaterialTheme.typography.headlineSmall)
        // Campos para alergias, ejercicios favoritos, etc.
    }
}
