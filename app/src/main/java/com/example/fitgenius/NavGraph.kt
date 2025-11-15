package com.example.fitgenius

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitgenius.data.AIResponse
import com.example.fitgenius.data.UserProfile
import com.example.fitgenius.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    currentUserProfile: UserProfile?,
    aiResponse: AIResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onUserRegistered: (UserProfile) -> Unit,
    onProfileComplete: (UserProfile, Boolean, Uri?) -> Unit,
    onGenerateNewPlan: () -> Unit,
    startDestination: String = "login"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onUserRegistered = {
                    onUserRegistered(it)
                    navController.navigate("profile_form/false")
                }
            )
        }
        composable(
            route = "profile_form/{isEditing}",
            arguments = listOf(navArgument("isEditing") { type = NavType.BoolType })
        ) {
            if (currentUserProfile != null) {
                val isEditing = it.arguments?.getBoolean("isEditing") ?: false
                ProfileFormScreen(
                    navController = navController,
                    userProfile = currentUserProfile,
                    onProfileComplete = { profile -> onProfileComplete(profile, isEditing, null) },
                    isEditing = isEditing
                )
            }
        }
        composable("home") {
            HomeScreen(navController, currentUserProfile, aiResponse, isLoading, errorMessage)
        }
        composable("profile") {
            if (currentUserProfile != null) {
                ProfileScreen(navController, currentUserProfile, onGenerateNewPlan)
            }
        }
        composable("account_settings") {
            if (currentUserProfile != null) {
                AccountSettingsScreen(navController, currentUserProfile, onProfileComplete)
            }
        }
    }
}