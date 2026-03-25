// Jetpack Navigation Template with Type-Safe Arguments
// Pattern: Sealed navigation routes, nested graphs

package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

// Sealed Navigation Routes (Type-Safe)
@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class Details(val itemId: String) : Route

    @Serializable
    data class Settings(val section: String = "general") : Route

    // Nested graph routes
    @Serializable
    sealed interface Auth : Route {
        @Serializable
        data object Login : Auth

        @Serializable
        data object Register : Auth

        @Serializable
        data object ForgotPassword : Auth
    }
}

// Main Navigation Host
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable<Route.Home> {
            HomeScreen(
                onNavigateToDetails = { itemId ->
                    navController.navigate(Route.Details(itemId))
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                }
            )
        }

        // Profile Screen
        composable<Route.Profile> {
            ProfileScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToSettings = { section ->
                    navController.navigate(Route.Settings(section))
                }
            )
        }

        // Details Screen with Arguments
        composable<Route.Details> { backStackEntry ->
            val route: Route.Details = backStackEntry.toRoute()
            DetailsScreen(
                itemId = route.itemId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Settings Screen with Optional Arguments
        composable<Route.Settings> { backStackEntry ->
            val route: Route.Settings = backStackEntry.toRoute()
            SettingsScreen(
                initialSection = route.section,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Auth Flow (Nested Graph)
        authNavGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(Route.Home) {
                    // Clear auth flow from back stack
                    popUpTo<Route.Auth.Login> { inclusive = true }
                }
            }
        )
    }
}

// Auth Navigation Graph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation<Route.Auth>(
        startDestination = Route.Auth.Login
    ) {
        composable<Route.Auth.Login> {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(Route.Auth.Register)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Route.Auth.ForgotPassword)
                }
            )
        }

        composable<Route.Auth.Register> {
            RegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable<Route.Auth.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

// Navigation Extensions
fun NavHostController.navigateToAuth() {
    navigate(Route.Auth.Login)
}

fun NavHostController.navigateAndClearBackStack(route: Route) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}

// Placeholder Screens (replace with actual implementations)
@Composable
private fun HomeScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // Implementation
}

@Composable
private fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (String) -> Unit
) {
    // Implementation
}

@Composable
private fun DetailsScreen(
    itemId: String,
    onNavigateBack: () -> Unit
) {
    // Implementation
}

@Composable
private fun SettingsScreen(
    initialSection: String,
    onNavigateBack: () -> Unit
) {
    // Implementation
}

@Composable
private fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    // Implementation
}

@Composable
private fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Implementation
}

@Composable
private fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    // Implementation
}
