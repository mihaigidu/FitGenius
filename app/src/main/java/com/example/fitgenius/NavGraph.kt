package com.example.fitgenius

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitgenius.screens.HomeScreen
import com.example.fitgenius.screens.LoginScreen
import com.example.fitgenius.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = "login") {
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onUserRegistered = { profile ->
                    currentUserProfile = profile
                }
            )
        }
        composable("home") {
            HomeScreen(navController, currentUserProfile)
        }
    }
}