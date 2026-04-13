package id.rancak.app.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.rancak.app.presentation.ui.auth.LoginScreen
import id.rancak.app.presentation.ui.cart.CartScreen
import id.rancak.app.presentation.ui.payment.PaymentScreen
import id.rancak.app.presentation.ui.pos.PosScreen
import id.rancak.app.presentation.ui.shift.ShiftScreen
import kotlinx.coroutines.launch

private data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RancakNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()

    val drawerItems = remember {
        listOf(
            DrawerItem("Kasir", Icons.Default.PointOfSale, Screen.Pos),
            DrawerItem("Shift", Icons.Default.AccessTime, Screen.Shift),
            DrawerItem("Riwayat", Icons.Default.Receipt, Screen.SalesHistory),
        )
    }

    val showDrawer = remember(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route
        route != null && !route.contains("Login") && !route.contains("TenantPicker")
    }

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                    Spacer(Modifier.height(24.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Rancak POS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Point of Sale",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))

                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = false,
                            onClick = {
                                navController.navigate(item.screen) {
                                    launchSingleTop = true
                                }
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        label = { Text("Keluar") },
                        selected = false,
                        onClick = {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        ) {
            NavigationContent(navController, onMenuClick = {
                scope.launch {
                    drawerState.open()
                }
            })
        }
    } else {
        NavigationContent(navController, onMenuClick = {})
    }
}

@Composable
private fun NavigationContent(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Pos) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Pos> {
            PosScreen(
                onCartClick = { navController.navigate(Screen.Cart) },
                onMenuClick = onMenuClick
            )
        }

        composable<Screen.Cart> {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Screen.Payment) }
            )
        }

        composable<Screen.Payment> {
            PaymentScreen(
                onBack = { navController.popBackStack() },
                onPaymentComplete = {
                    navController.navigate(Screen.Pos) {
                        popUpTo(Screen.Pos) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Shift> {
            ShiftScreen(onBack = { navController.popBackStack() })
        }
    }
}
