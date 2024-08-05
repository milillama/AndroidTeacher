import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainView(userSettings: UserSettings, authenticationViewModel: AuthenticationViewModel = viewModel()) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            CustomTabBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(userSettings) }
            composable("new") { NewScreen(userSettings) }
            composable("requests") { RequestsScreen(userSettings) }
            composable("profile") { ProfileScreen(userSettings) }
            composable("settings") { SettingsScreen(userSettings) }
        }
    }
}

@Composable
fun CustomTabBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("New", Icons.Default.NoteAdd, "new"),
        BottomNavItem("Requests", Icons.Default.Inbox, "requests"),
        BottomNavItem("Profile", Icons.Default.Person, "profile"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )

    BottomNavigation(
        elevation = 8.dp
    ) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestination) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun HomeScreen(userSettings: UserSettings) {
    // Implement your HomeScreen content
    Box(modifier = Modifier.padding(16.dp)) {
        Text(text = "Home Screen")
    }
}

@Composable
fun NewScreen(userSettings: UserSettings) {
    // Implement your NewScreen content
    Box(modifier = Modifier.padding(16.dp)) {
        Text(text = "New Screen")
    }
}

@Composable
fun RequestsScreen(userSettings: UserSettings) {
    // Implement your RequestsScreen content
    Box(modifier = Modifier.padding(16.dp)) {
        Text(text = "Requests Screen")
    }
}

@Composable
fun ProfileScreen(userSettings: UserSettings) {
    // Implement your ProfileScreen content
    Box(modifier = Modifier.padding(16.dp)) {
        Text(text = "Profile Screen")
    }
}

@Composable
fun SettingsScreen(userSettings: UserSettings) {
    // Implement your SettingsScreen content
    Box(modifier = Modifier.padding(16.dp)) {
        Text(text = "Settings Screen")
    }
}
