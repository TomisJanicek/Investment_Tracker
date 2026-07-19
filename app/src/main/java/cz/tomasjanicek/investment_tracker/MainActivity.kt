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
import cz.tomasjanicek.investment_tracker.presentation.add_edit.AddEditAction
import cz.tomasjanicek.investment_tracker.presentation.add_edit.AddEditScreen
import cz.tomasjanicek.investment_tracker.presentation.add_edit.AddEditViewModel
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardAction
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardScreen
import cz.tomasjanicek.investment_tracker.presentation.dashboard.DashboardViewModel
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailAction
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailScreen
import cz.tomasjanicek.investment_tracker.presentation.detail.DetailViewModel
import cz.tomasjanicek.investment_tracker.presentation.manage_assets.ManageAssetsAction
import cz.tomasjanicek.investment_tracker.presentation.manage_assets.ManageAssetsScreen
import cz.tomasjanicek.investment_tracker.presentation.manage_assets.ManageAssetsViewModel
import cz.tomasjanicek.investment_tracker.presentation.manage_prices.ManagePricesScreen
import cz.tomasjanicek.investment_tracker.presentation.manage_prices.ManagePricesViewModel
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
                        // Otevření obrazovky pro přidání nového aktiva
                        is DashboardAction.OnAddAssetClicked -> {
                            navController.navigate("add_edit")
                        }
                        // Správa cen
                        is DashboardAction.OnManagePricesClicked -> {
                            navController.navigate("manage_prices")
                        }
                        // Správa aktiv (Ticketů)
                        is DashboardAction.OnManageAssetsClicked -> {
                            navController.navigate("manage_assets")
                        }
                        // Ostatní akce (Refresh) předáme rovnou do ViewModelu
                        else -> viewModel.onAction(action)
                    }
                }
            )
        }

        // 2. OBRAZOVKA: SPRÁVA CEN
        composable("manage_prices") {
            val viewModel: ManagePricesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            ManagePricesScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. OBRAZOVKA: SPRÁVA AKTIV (TICKETŮ)
        composable("manage_assets") {
            val viewModel: ManageAssetsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            ManageAssetsScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. OBRAZOVKA: ADD/EDIT AKTIVA
        composable(
            route = "add_edit?ticker={ticker}",
            arguments = listOf(
                navArgument("ticker") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: AddEditViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            AddEditScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. OBRAZOVKA: DETAIL AKTIVA (přijímá parametr ticker)
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
                        // Přechod na obrazovku přidání transakce pro toto aktivum
                        is DetailAction.OnAddTransactionClicked -> {
                            navController.navigate("add_edit?ticker=${state.ticker}")
                        }
                        else -> viewModel.onAction(action)
                    }
                }
            )
        }
    }
}