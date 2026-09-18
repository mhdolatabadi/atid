package ir.mhdolatabadi.atid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.ui.dashboard.DashboardScreen
import ir.mhdolatabadi.atid.ui.home.HomeScreen
import ir.mhdolatabadi.atid.ui.notifications.PrayerTimesScreen
import ir.mhdolatabadi.atid.ui.texts.TextDetailScreen
import ir.mhdolatabadi.atid.ui.texts.TextsScreen

private sealed class AtidDestination(val route: String, val labelRes: Int, val iconRes: Int) {
    object Home : AtidDestination("home", R.string.title_home, R.drawable.ic_tab_qada)
    object Dashboard : AtidDestination("dashboard", R.string.title_dashboard, R.drawable.ic_tab_calendar)
    object PrayerTimes : AtidDestination("prayer_times", R.string.title_notifications, R.drawable.ic_tab_prayer_times)
    object Texts : AtidDestination("texts", R.string.title_texts, R.drawable.ic_tab_texts)
}

private val bottomBarDestinations = listOf(
    AtidDestination.Home,
    AtidDestination.Dashboard,
    AtidDestination.PrayerTimes,
    AtidDestination.Texts
)

@Composable
fun AtidApp() {
    val navController = rememberNavController()

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = navController,
            startDestination = AtidDestination.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(AtidDestination.Home.route) { HomeScreen() }
            composable(AtidDestination.Dashboard.route) { DashboardScreen() }
            composable(AtidDestination.PrayerTimes.route) { PrayerTimesScreen() }
            composable(AtidDestination.Texts.route) {
                TextsScreen(onTextClick = { id -> navController.navigate("texts/$id") })
            }
            composable(
                route = "texts/{textId}",
                arguments = listOf(navArgument("textId") { })
            ) { backStackEntry ->
                val textId = backStackEntry.arguments?.getString("textId").orEmpty()
                TextDetailScreen(textId = textId, onBack = { navController.popBackStack() })
            }
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            bottomBarDestinations.forEach { destination ->
                val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(destination.labelRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
