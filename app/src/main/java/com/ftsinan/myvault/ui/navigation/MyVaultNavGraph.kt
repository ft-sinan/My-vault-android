package com.ftsinan.myvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ftsinan.myvault.ui.screens.addedit.AddEditDocumentScreen
import com.ftsinan.myvault.ui.screens.detail.DocumentDetailScreen
import com.ftsinan.myvault.ui.screens.home.HomeScreen
import com.ftsinan.myvault.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{documentId}"
    const val DETAIL = "detail/{documentId}"
    const val SETTINGS = "settings"

    fun edit(documentId: Long) = "edit/$documentId"
    fun detail(documentId: Long) = "detail/$documentId"

    const val ARG_DOCUMENT_ID = "documentId"
}

/**
 * The unlocked part of the app. Lock/Setup-PIN are handled above this
 * graph by MainActivity, so a locked vault can never be navigated into.
 */
@Composable
fun MyVaultNavGraph(onLock: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddDocument = { navController.navigate(Routes.ADD) },
                onDocumentClick = { id -> navController.navigate(Routes.detail(id)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onLock = onLock
            )
        }

        composable(Routes.ADD) {
            AddEditDocumentScreen(
                documentId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument(Routes.ARG_DOCUMENT_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments
                ?.getLong(Routes.ARG_DOCUMENT_ID) ?: 0L
            AddEditDocumentScreen(
                documentId = documentId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_DOCUMENT_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments
                ?.getLong(Routes.ARG_DOCUMENT_ID) ?: 0L
            DocumentDetailScreen(
                documentId = documentId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLock = onLock
            )
        }
    }
}
