package cz.tomasjanicek.investment_tracker.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.investment_tracker.domain.usecase.AddTransactionUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.GetDefinedAssetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getDefinedAssetsUseCase: GetDefinedAssetsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddEditEvent>()
    val events = _events.receiveAsFlow()

    init {
        val ticker: String? = savedStateHandle["ticker"]
        if (ticker != null) {
            _uiState.update { it.copy(ticker = ticker, isTickerEnabled = false) }
        }

        // Načtení definovaných aktiv pro dropdown
        viewModelScope.launch {
            getDefinedAssetsUseCase().collect { assets ->
                _uiState.update { it.copy(definedAssets = assets) }
            }
        }
    }

    fun onAction(action: AddEditAction) {
        when (action) {
            is AddEditAction.OnTickerChanged -> _uiState.update { it.copy(ticker = action.ticker) }
            is AddEditAction.OnTypeChanged -> _uiState.update { it.copy(type = action.type) }
            is AddEditAction.OnQuantityChanged -> _uiState.update { it.copy(quantity = action.quantity) }
            is AddEditAction.OnPriceChanged -> _uiState.update { it.copy(price = action.price) }
            is AddEditAction.OnDateChanged -> _uiState.update { it.copy(date = action.date) }
            is AddEditAction.OnTimeChanged -> _uiState.update { it.copy(time = action.time) }
            is AddEditAction.OnDropdownExpandedChanged -> _uiState.update { it.copy(isDropdownExpanded = action.expanded) }
            is AddEditAction.OnTypeDropdownExpandedChanged -> _uiState.update { it.copy(isTypeDropdownExpanded = action.expanded) }
            AddEditAction.OnSaveClicked -> saveTransaction()
            AddEditAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(AddEditEvent.NavigateBack) }
            }
        }
    }

    private fun saveTransaction() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val cleanQty = state.quantity.replace(',', '.').trim()
                val cleanPrice = state.price.replace(',', '.').trim()
                
                val timestamp = LocalDateTime.of(state.date, state.time)

                addTransactionUseCase(
                    ticker = state.ticker,
                    type = state.type,
                    quantity = BigDecimal(cleanQty),
                    price = BigDecimal(cleanPrice),
                    timestamp = timestamp
                )
                
                // Úspěšné uložení -> zobrazení animace a pak návrat
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                delay(2000) // Čas na animaci
                _events.send(AddEditEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Chyba při ukládání: ${e.message}") }
            }
        }
    }

    sealed interface AddEditEvent {
        data object NavigateBack : AddEditEvent
    }
}