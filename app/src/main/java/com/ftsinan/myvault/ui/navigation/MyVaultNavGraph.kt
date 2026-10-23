package com.ftsinan.myvault.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.ui.screens.addedit.AddEditDocumentScreen
import com.ftsinan.myvault.ui.screens.addedit.ImportSource
import com.ftsinan.myvault.ui.screens.detail.DocumentDetailScreen
import com.ftsinan.myvault.ui.screens.documents.DocumentsScreen
import com.ftsinan.myvault.ui.screens.home.HomeScreen
import com.ftsinan.myvault.ui.screens.profile.FamilyScreen
import com.ftsinan.myvault.ui.screens.profile.MemberEditScreen
import com.ftsinan.myvault.ui.screens.profile.ProfileDetailScreen
import com.ftsinan.myvault.ui.screens.settings.ManageMembersScreen
import com.ftsinan.myvault.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val DOCUMENTS = "documents?group={group}"
    const val DOCUMENTS_BASE = "documents"
    const val FAMILY = "family"
    const val SETTINGS = "settings"

    const val ADD = "add?source={source}"
    const val EDIT = "edit/{documentId}"
    const val DETAIL = "detail/{documentId}"
    const val PROFILE = "profile/{memberId}"
    const val MEMBER_ADD = "member/add"
    const val MEMBER_EDIT = "member/edit/{memberId}"
    const val MANAGE_MEMBERS = "members/manage"

    const val ARG_DOCUMENT_ID = "documentId"
    const val ARG_MEMBER_ID = "memberId"
    const val ARG_SOURCE = "source"
    const val ARG_GROUP = "group"

    fun add(source: ImportSource = ImportSource.NONE) = "add?source=${source.name}"
    fun edit(documentId: Long) = "edit/$documentId"
    fun detail(documentId: Long) = "detail/$documentId"
    fun profile(memberId: Long) = "profile/$memberId"
    fun memberEdit(memberId: Long) = "member/edit/$memberId"

    fun documents(group: AssignedGroup? = null): String =
        if (group == null) DOCUMENTS_BASE else "$DOCUMENTS_BASE?group=${group.name}"
}

/**
 * The unlocked part of the app. Lock/setup gates are handled by MainActivity,
 * so a locked vault can never be navigated into.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVaultNavGraph(
    onLock: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val navController = rememberNavController()
    var showAddSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute != null && currentRoute in setOf(
        Routes.HOME,
        Routes.DOCUMENTS,
        Routes.FAMILY,
        Routes.SETTINGS
    )

    fun navigateTopLevel(route: String, restoreSavedState: Boolean = true) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = restoreSavedState
        }
    }

    fun isSelected(routePattern: String): Boolean =
        currentDestination?.hierarchy?.any { it.route == routePattern } == true

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    stringResource(R.string.add_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.add_sheet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Card(
                    modifier = Modifier.clickable {
                        showAddSheet = false
                        navController.navigate(Routes.add(ImportSource.SCAN))
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.add_scan)) },
                        supportingContent = { Text(stringResource(R.string.add_scan_subtitle)) },
                        leadingContent = {
                            Icon(Icons.Filled.DocumentScanner, contentDescription = null)
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.clickable {
                        showAddSheet = false
                        navController.navigate(Routes.add(ImportSource.FILE))
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.add_file)) },
                        supportingContent = { Text(stringResource(R.string.add_file_subtitle)) },
                        leadingContent = {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    Scaffold(
        // Every destination provides its own top bar. Zero insets here stop
        // Scaffold from applying the status-bar height a second time above
        // those top bars, which made every header look oversized.
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider()
                    NavigationBar {
                        NavigationBarItem(
                            selected = isSelected(Routes.HOME),
                            onClick = { navigateTopLevel(Routes.HOME) },
                            icon = {
                                Icon(
                                    Icons.Filled.Home,
                                    contentDescription = stringResource(R.string.tab_home)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_home)) }
                        )
                        NavigationBarItem(
                            selected = isSelected(Routes.DOCUMENTS),
                            onClick = { navigateTopLevel(Routes.documents()) },
                            icon = {
                                Icon(
                                    Icons.Filled.Description,
                                    contentDescription = stringResource(R.string.tab_docs)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_docs)) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { showAddSheet = true },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.tab_add),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.tab_add)) }
                        )
                        NavigationBarItem(
                            selected = isSelected(Routes.FAMILY),
                            onClick = { navigateTopLevel(Routes.FAMILY) },
                            icon = {
                                Icon(
                                    Icons.Filled.Groups,
                                    contentDescription = stringResource(R.string.tab_family)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_family)) }
                        )
                        NavigationBarItem(
                            selected = isSelected(Routes.SETTINGS),
                            onClick = { navigateTopLevel(Routes.SETTINGS) },
                            icon = {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = stringResource(R.string.tab_settings)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_settings)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAddDocument = { navController.navigate(Routes.add(it)) },
                    onDocumentClick = { navController.navigate(Routes.detail(it)) },
                    onProfileClick = { navController.navigate(Routes.profile(it)) },
                    onFamilyClick = { navigateTopLevel(Routes.FAMILY) },
                    onLock = onLock
                )
            }

            composable(
                route = Routes.DOCUMENTS,
                arguments = listOf(
                    navArgument(Routes.ARG_GROUP) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                )
            ) { entry ->
                val initialGroup = entry.arguments?.getString(Routes.ARG_GROUP)
                    ?.let { runCatching { AssignedGroup.valueOf(it) }.getOrNull() }
                DocumentsScreen(
                    initialGroup = initialGroup,
                    onAddDocument = { navController.navigate(Routes.add(it)) },
                    onDocumentClick = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.FAMILY) {
                FamilyScreen(
                    onProfileClick = { navController.navigate(Routes.profile(it)) },
                    onManageMembers = { navController.navigate(Routes.MANAGE_MEMBERS) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navigateTopLevel(Routes.HOME) },
                    onLock = onLock,
                    onManageMembers = { navController.navigate(Routes.MANAGE_MEMBERS) },
                    onLanguageSelected = onLanguageSelected
                )
            }

            composable(Routes.MANAGE_MEMBERS) {
                ManageMembersScreen(
                    onBack = { navController.popBackStack() },
                    onAddMember = { navController.navigate(Routes.MEMBER_ADD) },
                    onEditMember = { navController.navigate(Routes.memberEdit(it)) }
                )
            }

            composable(Routes.MEMBER_ADD) {
                MemberEditScreen(
                    memberId = null,
                    ownerSetup = false,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.MEMBER_EDIT,
                arguments = listOf(
                    navArgument(Routes.ARG_MEMBER_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val memberId = entry.arguments?.getLong(Routes.ARG_MEMBER_ID) ?: 0L
                MemberEditScreen(
                    memberId = memberId,
                    ownerSetup = false,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.PROFILE,
                arguments = listOf(
                    navArgument(Routes.ARG_MEMBER_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val memberId = entry.arguments?.getLong(Routes.ARG_MEMBER_ID) ?: 0L
                ProfileDetailScreen(
                    memberId = memberId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.memberEdit(it)) },
                    onDocumentClick = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(
                route = Routes.ADD,
                arguments = listOf(
                    navArgument(Routes.ARG_SOURCE) {
                        type = NavType.StringType
                        defaultValue = ImportSource.NONE.name
                    }
                )
            ) { entry ->
                val source = runCatching {
                    ImportSource.valueOf(
                        entry.arguments?.getString(Routes.ARG_SOURCE)
                            ?: ImportSource.NONE.name
                    )
                }.getOrDefault(ImportSource.NONE)
                AddEditDocumentScreen(
                    documentId = null,
                    initialSource = source,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT,
                arguments = listOf(
                    navArgument(Routes.ARG_DOCUMENT_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val documentId = entry.arguments?.getLong(Routes.ARG_DOCUMENT_ID) ?: 0L
                AddEditDocumentScreen(
                    documentId = documentId,
                    initialSource = ImportSource.NONE,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_DOCUMENT_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val documentId = entry.arguments?.getLong(Routes.ARG_DOCUMENT_ID) ?: 0L
                DocumentDetailScreen(
                    documentId = documentId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(it)) }
                )
            }
        }
    }
}
