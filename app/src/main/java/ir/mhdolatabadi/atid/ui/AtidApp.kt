package ir.mhdolatabadi.atid.ui

import android.app.Activity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.ui.components.SkyBackground
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.components.pressScale
import ir.mhdolatabadi.atid.ui.dashboard.DashboardScreen
import ir.mhdolatabadi.atid.ui.home.HomeScreen
import ir.mhdolatabadi.atid.ui.notifications.PrayerTimesScreen
import ir.mhdolatabadi.atid.ui.texts.TextDetailScreen
import ir.mhdolatabadi.atid.ui.texts.TextsScreen
import ir.mhdolatabadi.atid.ui.theme.Atid

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

/** Space the floating tab bar takes, so screens can keep their last item clear of it. */
val BottomBarClearance = 104.dp

@Composable
fun AtidApp() {
    val navController = rememberNavController()
    val colors = Atid.colors

    // The sky runs behind the system bars, so their icons must contrast with it.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !colors.isDark
                isAppearanceLightNavigationBars = !colors.isDark
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SkyBackground()

        NavHost(
            navController = navController,
            startDestination = AtidDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            enterTransition = { fadeIn(tween(320)) + slideInVertically(tween(420)) { it / 24 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(320)) },
            popExitTransition = { fadeOut(tween(160)) }
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
        val selectedIndex = bottomBarDestinations.indexOfFirst { destination ->
            currentRoute?.hierarchy?.any { it.route == destination.route } == true ||
                (destination == AtidDestination.Texts && currentRoute?.route?.startsWith("texts/") == true)
        }.coerceAtLeast(0)

        FloatingTabBar(
            selectedIndex = selectedIndex,
            onSelect = { destination ->
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun FloatingTabBar(
    selectedIndex: Int,
    onSelect: (AtidDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Atid.colors
    val barShape = RoundedCornerShape(24.dp)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .glass(barShape, strong = true)
            .padding(6.dp)
    ) {
        val tabWidth = maxWidth / bottomBarDestinations.size
        // offset() follows layout direction, so in RTL the pill moves leftwards from the first tab.
        val indicatorOffset by animateDpAsState(tabWidth * selectedIndex, tween(420), label = "tabIndicator")
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .background(colors.accentSoft, RoundedCornerShape(18.dp))
        )
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
            bottomBarDestinations.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                val interaction = remember { MutableInteractionSource() }
                val lift by animateDpAsState(if (selected) (-2).dp else 0.dp, tween(320), label = "tabLift")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pressScale(interaction)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(destination) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val tint = if (selected) colors.accent else colors.muted
                    Icon(
                        painter = painterResource(destination.iconRes),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.offset(y = lift)
                    )
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = tint
                    )
                }
            }
        }
    }
}
