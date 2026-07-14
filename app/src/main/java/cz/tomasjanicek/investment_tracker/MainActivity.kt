package cz.tomasjanicek.investment_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardAction
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardScreen
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardViewModel
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailAction
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailScreen
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailViewModel
import cz.tomasjanicek.investment_tracker.ui.theme.Investment_TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Investment_TrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InvestmentAppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun InvestmentAppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // NavHost definuje, že startovní obrazovkou je "dashboard"
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        // 1. OBRAZOVKA: DASHBOARD
        composable("dashboard") {
            // Hilt nám automaticky dodá správnou instanci ViewModelu
            val viewModel: DashboardViewModel = hiltViewModel()

            // Bezpečné sbírání reaktivního stavu v lifecycle-aware režimu
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            DashboardScreen(
                state = state,
                onAction = { action ->
                    when (action) {
                        // Když uživatel klikne na aktivum, přesměrujeme ho do detailu
                        is DashboardAction.OnAssetClicked -> {
                            navController.navigate("detail/${action.ticker}")
                        }
                        // Ostatní akce (Refresh, Add) předáme rovnou do ViewModelu
                        else -> viewModel.onAction(action)
                    }
                }
            )
        }

        // 2. OBRAZOVKA: DETAIL AKTIVA (přijímá parametr ticker)
        composable(
            route = "detail/{ticker}",
            arguments = listOf(
                navArgument("ticker") { type = NavType.StringType }
            )
        ) {
            val viewModel: DetailViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            DetailScreen(
                state = state,
                onAction = { action ->
                    when (action) {
                        // Zpracování tlačítka Zpět v top baru detailu
                        is DetailAction.OnBackClicked -> {
                            navController.popBackStack()
                        }
                        else -> viewModel.onAction(action)
                    }
                }
            )
        }
    }
}