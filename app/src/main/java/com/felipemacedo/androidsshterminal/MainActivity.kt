package com.felipemacedo.androidsshterminal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.felipemacedo.androidsshterminal.ui.screens.connection.ConnectionScreen
import com.felipemacedo.androidsshterminal.ui.screens.hostlist.HostListScreen
import com.felipemacedo.androidsshterminal.ui.screens.terminal.TerminalScreen
import com.felipemacedo.androidsshterminal.ui.theme.AndroidSSHTerminalTheme

/**
 * Main activity for the Android SSH Terminal app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidSSHTerminalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "host_list"
                    ) {
                        // Host list screen
                        composable("host_list") {
                            HostListScreen(
                                onNavigateToConnection = { hostId ->
                                    if (hostId != null) {
                                        navController.navigate("connection/$hostId")
                                    } else {
                                        navController.navigate("connection")
                                    }
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId")
                                }
                            )
                        }

                        // Connection screen (new connection)
                        composable("connection") {
                            ConnectionScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId") {
                                        // Pop connection screen from back stack
                                        popUpTo("host_list")
                                    }
                                }
                            )
                        }

                        // Connection screen (edit existing connection)
                        composable(
                            route = "connection/{hostId}",
                            arguments = listOf(navArgument("hostId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")
                            ConnectionScreen(
                                hostId = hostId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId") {
                                        // Pop connection screen from back stack
                                        popUpTo("host_list")
                                    }
                                }
                            )
                        }

                        // Terminal screen
                        composable(
                            route = "terminal/{sessionId}",
                            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                            TerminalScreen(
                                sessionId = sessionId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
