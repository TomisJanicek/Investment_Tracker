package cz.tomasjanicek.investment_tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase, // Voláme UseCase, ne Repository!
    private val refreshPricesUseCase: RefreshPricesUseCase
) : ViewModel() {

    // Jediný zdroj pravdy pro UI
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadPortfolio()
    }

    // TADY JE TO MVI KOUZLO: Z UI voláš POUZE tuto jedinou funkci
    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.RefreshPrices -> refreshPrices()
            is DashboardAction.DeleteAsset -> deleteAsset(action.ticker)
            is DashboardAction.NavigateToDetail -> {
                // Zde můžeš odeslat jednorázový event pro navigaci
            }
        }
    }

    private fun refreshPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            refreshPricesUseCase() // Provádí se byznys logika ve vrstvě Domain
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    // ...
}