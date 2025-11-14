package com.example.fitgenius

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.screens.HomeScreen
import com.example.fitgenius.screens.LoginScreen
import com.example.fitgenius.screens.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    currentUserProfile: UserProfile?,
    onUserRegistered: (UserProfile) -> Unit,
    startDestination: String = "login"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onUserRegistered = onUserRegistered
            )
        }
        composable("home") {
            HomeScreen(navController, currentUserProfile)
        }
    }
}