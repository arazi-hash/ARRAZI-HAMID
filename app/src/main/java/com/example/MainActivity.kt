package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FounderScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProtocolScreen
import com.example.ui.screens.SurvivalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnchorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AnchorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                
                // Observe ViewModel live flows
                val isLocked by viewModel.isLocked.collectAsState()
                val isSealed by viewModel.isSealed.collectAsState()
                val isOnboarded by viewModel.isOnboarded.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        
                        // ABSOLUTE DISCIPLINE LOCKDOWN INTERCEPTOR:
                        // If locked, the Compose Tree strictly restricts rendering to ONLY show the Survival state.
                        if (isLocked) {
                            SurvivalScreen(
                                viewModel = viewModel,
                                onNavigateToDashboard = {
                                    // Lock verified & released by VM coordinates query
                                }
                            )
                        } else if (!isSealed) {
                            // Stage 1: Founder configuration
                            FounderScreen(
                                viewModel = viewModel,
                                onNavigateToOnboarding = {
                                    // VM will also update SharedPreferences state
                                },
                                snackbarHostState = snackbarHostState
                            )
                        } else if (!isOnboarded) {
                            // Stage 2: Client onboarding setup
                            OnboardingScreen(
                                viewModel = viewModel,
                                onFinishOnboarding = {
                                    viewModel.completeOnboarding()
                                }
                            )
                        } else {
                            // Stage 3: Standard GCC Dashboard Navigation Hub
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = "dashboard",
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable("dashboard") {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToProtocol = { navController.navigate("protocol") },
                                        onNavigateToSurvival = {
                                            // Fallback bypass router for inspecting Survival views from dashboard
                                            navController.navigate("survival_preview")
                                        },
                                        onNavigateToFounderReset = {
                                            // Handled inside resets
                                        }
                                    )
                                }

                                composable("protocol") {
                                    ProtocolScreen(
                                        viewModel = viewModel,
                                        onNavigateToDashboard = {
                                            navController.popBackStack("dashboard", inclusive = false)
                                        }
                                    )
                                }

                                composable("survival_preview") {
                                    SurvivalScreen(
                                        viewModel = viewModel,
                                        onNavigateToDashboard = {
                                            navController.popBackStack("dashboard", inclusive = false)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkHardwarePermissions()
    }
}
