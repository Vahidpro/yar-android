package io.github.vahidpro.yar.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.vahidpro.yar.R
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarScreenBar
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.hokm.ui.HokmRulesScreen
import io.github.vahidpro.yar.hokm.ui.HokmScoreScreen
import io.github.vahidpro.yar.hokm.ui.HokmViewModel
import io.github.vahidpro.yar.settings.AppPreferences
import io.github.vahidpro.yar.settings.AppearanceViewModel

private enum class MainTab(val route: String, @param:StringRes val label: Int, val icon: YarSymbol) {
    Tools("tools", R.string.tab_tools, YarSymbol.Cards),
    Games("games", R.string.tab_games, YarSymbol.History),
    Settings("settings", R.string.tab_settings, YarSymbol.Settings),
}

private const val currentGame = "hokm/current"
private const val savedGame = "hokm/saved"
private const val hokmRules = "hokm/rules"

@Composable
fun YarApp(hokm: HokmViewModel, appearance: AppearanceViewModel, preferences: AppPreferences) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentTab = MainTab.entries.firstOrNull { it.route == backStackEntry?.destination?.route }
    val games by hokm.games.collectAsStateWithLifecycle()
    val themeSaveFailed by appearance.saveFailed.collectAsStateWithLifecycle()
    val snackbars = remember { SnackbarHostState() }
    val themeSaveError = stringResource(R.string.theme_save_error)
    LaunchedEffect(themeSaveFailed) {
        if (themeSaveFailed) {
            snackbars.showSnackbar(themeSaveError)
            appearance.clearSaveError()
        }
    }

    fun selectTab(tab: MainTab) {
        nav.navigate(tab.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openGame(id: Long?) {
        nav.navigate(if (id == null) currentGame else "$savedGame/$id") { launchSingleTop = true }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbars) },
        bottomBar = {
            if (currentTab != null) {
                YarNavigation(currentTab = currentTab, onSelect = ::selectTab)
            }
        },
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            NavHost(
                navController = nav,
                startDestination = MainTab.Tools.route,
                modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            ) {
                composable(MainTab.Tools.route) {
                    ToolsScreen(
                        state = games,
                        onOpenHokm = { openGame(null) },
                        onOpenRules = { nav.navigate(hokmRules) },
                        onOpenGame = ::openGame,
                        onOpenHistory = { selectTab(MainTab.Games) },
                    )
                }
                composable(MainTab.Games.route) {
                    HistoryScreen(
                        state = games,
                        onRetry = hokm::reload,
                        onOpenGame = ::openGame,
                        onNewGame = { nav.navigate("$currentGame?setup=true") },
                    )
                }
                composable(MainTab.Settings.route) {
                    Column {
                        YarScreenBar(stringResource(R.string.tab_settings))
                        AppearanceScreen(
                            preferences = preferences,
                            onSelectMode = appearance::select,
                            onSelectPalette = appearance::selectPalette,
                            onSelectLanguage = appearance::selectLanguage,
                        )
                    }
                }
                composable(
                    route = "$currentGame?setup={setup}",
                    arguments = listOf(navArgument("setup") { type = NavType.BoolType; defaultValue = false }),
                ) { entry ->
                    HokmScoreScreen(
                        viewModel = hokm,
                        gameId = null,
                        openNewGame = entry.arguments?.getBoolean("setup") == true,
                        onBack = { nav.popBackStack() },
                        onActivated = {},
                    )
                }
                composable(
                    route = "$savedGame/{gameId}",
                    arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
                ) { entry ->
                    HokmScoreScreen(
                        viewModel = hokm,
                        gameId = entry.arguments?.getLong("gameId"),
                        onBack = { nav.popBackStack() },
                        onActivated = {
                            nav.navigate(currentGame) {
                                popUpTo(entry.destination.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(hokmRules) { HokmRulesScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}

@Composable
private fun YarNavigation(currentTab: MainTab, onSelect: (MainTab) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == currentTab,
                    onClick = { onSelect(tab) },
                    icon = { YarIcon(tab.icon) },
                    label = { Text(stringResource(tab.label), style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
