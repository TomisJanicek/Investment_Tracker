package cz.tomasjanicek.investment_tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.investment_tracker.domain.usecase.GetPortfolioUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.RefreshPricesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * ViewModel spravující stav a logiku obrazovky Dashboard.
 * Implementuje MVI pattern – transformuje byznys data z [GetPortfolioUseCase]
 * na [DashboardUiState] připravený pro vykreslení v UI.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val refreshPricesUseCase: RefreshPricesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))

    /**
     * Kombinovaný reaktivní stream dat.
     * Využívá [combine] k propojení interního stavu (loading, error)
     * s reálnými daty z doménové vrstvy.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        _uiState,
        getPortfolioUseCase()
    ) { localState, portfolioDomain ->

        localState.copy(
            isLoading = false,
            totalValue = portfolioDomain.totalValue,
            totalProfitLoss = portfolioDomain.totalProfitLoss,
            percentageChange = portfolioDomain.percentageChange,
            assets = portfolioDomain.assets.map { assetDomain ->
                AssetUiModel(
                    ticker = assetDomain.ticker,
                    name = assetDomain.name,
                    currentPrice = assetDomain.currentPrice,
                    totalQuantity = assetDomain.totalQuantity,
                    totalValue = assetDomain.currentPrice.multiply(assetDomain.totalQuantity),
                    profitLossPercentage = calculateAssetPercentage(
                        currentVal = assetDomain.currentPrice.multiply(assetDomain.totalQuantity),
                        investedVal = assetDomain.totalInvested
                    )
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        refreshPrices()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.RefreshPrices -> refreshPrices()
            else -> Unit // Další akce budou implementovány dle potřeby (např. navigace)
        }
    }

    private fun refreshPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                refreshPricesUseCase()
            } catch (e: Exception) {
                // Propagace chyby do UI stavu pro zobrazení uživatelské notifikace
                _uiState.update {
                    it.copy(errorMessage = "Aktualizace cen selhala. Data mohou být neaktuální.")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateAssetPercentage(currentVal: BigDecimal, investedVal: BigDecimal): Double {
        if (investedVal <= BigDecimal.ZERO) return 0.0
        return currentVal.subtract(investedVal)
            .divide(investedVal, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toDouble()
    }
}