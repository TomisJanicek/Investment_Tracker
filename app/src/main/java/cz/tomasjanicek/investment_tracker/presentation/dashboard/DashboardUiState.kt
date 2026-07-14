package cz.tomasjanicek.investment_tracker.presentation.dashboard

import java.math.BigDecimal

/**
 * Reprezentuje neměnný (immutable) stav obrazovky Dashboard.
 * UI vrstva používá tento model jako jediný zdroj pravdy pro vykreslení komponent.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val totalProfitLoss: BigDecimal = BigDecimal.ZERO,
    val percentageChange: Double = 0.0,
    val assets: List<AssetUiModel> = emptyList(),
    val errorMessage: String? = null
)

/**
 * UI model pro jedno aktivum, upravený pro potřeby zobrazení v seznamu.
 */
data class AssetUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: BigDecimal,
    val totalQuantity: BigDecimal,
    val totalValue: BigDecimal,
    val profitLossPercentage: Double,
    val isProfit: Boolean = profitLossPercentage >= 0.0
)