package com.ftsinan.myvault.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
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
import com.ftsinan.myvault.ui.components.springPress
import com.ftsinan.myvault.ui.screens.addedit.AddEditDocumentScreen
import com.ftsinan.myvault.ui.theme.LocalAnimationsEnabled
import com.ftsinan.myvault.ui.theme.LocalGlassBottomBar
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
    val glassBottomBar = LocalGlassBottomBar.current
    val animationsEnabled = LocalAnimationsEnabled.current
    var showAddSheet by remember { mutableStateOf(false) }
    val dockShownState = rememberSaveable { mutableStateOf(true) }
    val dockConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f) {
                    dockShownState.value = false
                } else if (available.y > 10f) {
                    dockShownState.value = true
                }
                return Offset.Zero
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute != null && currentRoute in setOf(
        Routes.HOME,
        Routes.DOCUMENTS,
        Routes.FAMILY,
        Routes.SETTINGS
    )

    LaunchedEffect(currentRoute) {
        if (showBottomBar) dockShownState.value = true
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(dockConnection),
            enterTransition = {
                if (!animationsEnabled) {
                    EnterTransition.None
                } else {
                    val fromTop = routeRank(initialState.destination.route) < 10
                    val toTop = routeRank(targetState.destination.route) < 10
                    if (fromTop && toTop) {
                        fadeIn(tween(260)) + scaleIn(
                            initialScale = 0.985f,
                            animationSpec = tween(260)
                        )
                    } else {
                        fadeIn(tween(240)) + slideInHorizontally(tween(320)) { it / 5 } +
                            scaleIn(initialScale = 0.99f, animationSpec = tween(320))
                    }
                }
            },
            exitTransition = {
                if (!animationsEnabled) {
                    ExitTransition.None
                } else {
                    val fromTop = routeRank(initialState.destination.route) < 10
                    val toTop = routeRank(targetState.destination.route) < 10
                    if (fromTop && toTop) {
                        fadeOut(tween(180)) + scaleOut(
                            targetScale = 0.99f,
                            animationSpec = tween(180)
                        )
                    } else {
                        fadeOut(tween(180)) + slideOutHorizontally(tween(240)) { -it / 8 }
                    }
                }
            },
            popEnterTransition = {
                if (!animationsEnabled) {
                    EnterTransition.None
                } else {
                    val fromTop = routeRank(initialState.destination.route) < 10
                    val toTop = routeRank(targetState.destination.route) < 10
                    if (fromTop && toTop) {
                        fadeIn(tween(240)) + scaleIn(
                            initialScale = 0.985f,
                            animationSpec = tween(240)
                        )
                    } else {
                        fadeIn(tween(220)) + slideInHorizontally(tween(300)) { -it / 5 }
                    }
                }
            },
            popExitTransition = {
                if (!animationsEnabled) {
                    ExitTransition.None
                } else {
                    val fromTop = routeRank(initialState.destination.route) < 10
                    val toTop = routeRank(targetState.destination.route) < 10
                    if (fromTop && toTop) {
                        fadeOut(tween(180)) + scaleOut(
                            targetScale = 0.99f,
                            animationSpec = tween(180)
                        )
                    } else {
                        fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { it / 5 }
                    }
                }
            }
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

        if (showBottomBar) {
            VaultFloatingDock(
                visible = dockShownState.value,
                glass = glassBottomBar,
                homeSelected = isSelected(Routes.HOME),
                documentsSelected = isSelected(Routes.DOCUMENTS),
                familySelected = isSelected(Routes.FAMILY),
                settingsSelected = isSelected(Routes.SETTINGS),
                onHome = { navigateTopLevel(Routes.HOME) },
                onDocuments = { navigateTopLevel(Routes.documents()) },
                onAdd = { showAddSheet = true },
                onFamily = { navigateTopLevel(Routes.FAMILY) },
                onSettings = { navigateTopLevel(Routes.SETTINGS) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


private fun routeRank(route: String?): Int = when (route) {
    Routes.HOME -> 0
    Routes.DOCUMENTS -> 1
    Routes.FAMILY -> 2
    Routes.SETTINGS -> 3
    else -> 10
}

/** Orion-style floating dock with an expanding selected label and scroll-away motion. */
@Composable
private fun VaultFloatingDock(
    visible: Boolean,
    glass: Boolean,
    homeSelected: Boolean,
    documentsSelected: Boolean,
    familySelected: Boolean,
    settingsSelected: Boolean,
    onHome: () -> Unit,
    onDocuments: () -> Unit,
    onAdd: () -> Unit,
    onFamily: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val translation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 0f else 150f,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.82f, stiffness = 300f)
        } else {
            snap()
        },
        label = "dockTranslation"
    )
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animationsEnabled) tween(180) else snap(),
        label = "dockAlpha"
    )
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .graphicsLayer {
                translationY = translation.dp.toPx()
                this.alpha = alpha
            }
            .shadow(14.dp, CircleShape)
            .clip(CircleShape)
            .background(
                if (glass) {
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
            .padding(6.dp)
    ) {
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp)) {
            DockItem(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.tab_home),
                selected = homeSelected,
                onClick = onHome
            )
            DockItem(
                icon = Icons.Filled.Description,
                label = stringResource(R.string.tab_docs),
                selected = documentsSelected,
                onClick = onDocuments
            )
            DockAddItem(onClick = onAdd)
            DockItem(
                icon = Icons.Filled.Groups,
                label = stringResource(R.string.tab_family),
                selected = familySelected,
                onClick = onFamily
            )
            DockItem(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.tab_settings),
                selected = settingsSelected,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = if (animationsEnabled) tween(220) else snap(),
        label = "dockItemContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = if (animationsEnabled) tween(220) else snap(),
        label = "dockItemContent"
    )
    val iconScale = remember { Animatable(1f) }
    LaunchedEffect(selected, animationsEnabled) {
        if (selected && animationsEnabled) {
            iconScale.snapTo(0.68f)
            iconScale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 460f))
        } else {
            iconScale.snapTo(1f)
        }
    }
    Box(
        modifier = Modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(container)
            .springPress(CircleShape, role = Role.Tab, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
            )
            AnimatedVisibility(
                visible = selected,
                enter = if (animationsEnabled) {
                    expandHorizontally(spring(dampingRatio = 0.76f, stiffness = 380f)) +
                        fadeIn(tween(160))
                } else {
                    EnterTransition.None
                },
                exit = if (animationsEnabled) {
                    shrinkHorizontally(tween(140)) + fadeOut(tween(100))
                } else {
                    ExitTransition.None
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 88.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockAddItem(onClick: () -> Unit) {
    val label = stringResource(R.string.tab_add)
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 52.dp)
            .springPress(CircleShape, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
