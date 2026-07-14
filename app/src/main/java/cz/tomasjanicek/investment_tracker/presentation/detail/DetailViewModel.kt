package cz.tomasjanicek.investment_tracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.investment_tracker.domain.usecase.AddTransactionUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.DeleteTransactionUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.GetAssetDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel spravující logiku a stav detailu aktiva.
 * * Využívá [SavedStateHandle] pro injekci navigačních argumentů.
 * * Reaktivně propojuje data z doménové vrstvy s lokálním UI stavem dialogů.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getAssetDetailsUseCase: GetAssetDetailsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Identifikátor aktiva je předán z navigace
    private val ticker: String = checkNotNull(savedStateHandle["ticker"]) {
        "Ticker nebyl předán do detailu!"
    }

    private val _uiState = MutableStateFlow(DetailUiState(ticker = ticker))

    /**
     * Sjednocený proud dat pro UI.
     * [combine] zajišťuje, že jakákoliv změna v databázi (přidaná transakce)
     * způsobí automatickou aktualizaci všech vypočítaných polí v UI stavu.
     */
    val uiState: StateFlow<DetailUiState> = combine(
        _uiState,
        getAssetDetailsUseCase(ticker)
    ) { localState, domainDetails ->
        localState.copy(
            name = domainDetails.name,
            currentPrice = domainDetails.currentPrice,
            totalQuantity = domainDetails.totalQuantity,
            totalValue = domainDetails.currentPrice.multiply(domainDetails.totalQuantity),
            transactions = domainDetails.transactions.map { tx ->
                TransactionUiModel(
                    id = tx.id,
                    type = tx.type,
                    quantity = tx.quantity,
                    pricePerShare = tx.pricePerShare,
                    totalPrice = tx.quantity.multiply(tx.pricePerShare),
                    dateFormatted = tx.dateFormatted
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailUiState(ticker = ticker, name = "Načítání...")
    )

    fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.OnAddTransactionClicked -> _uiState.update { it.copy(isShowingAddDialog = true) }
            DetailAction.OnDismissDialog -> _uiState.update { it.copy(isShowingAddDialog = false) }
            is DetailAction.OnSubmitTransaction -> executeAddTransaction(action.type, action.quantity, action.price)
            is DetailAction.OnDeleteTransaction -> executeDeleteTransaction(action.id)
            else -> Unit
        }
    }

    private fun executeAddTransaction(type: String, quantityStr: String, priceStr: String) {
        viewModelScope.launch {
            try {
                // Sanitizace vstupů: Nahrazení desetiných čárek tečkou pro kompatibilitu s BigDecimal
                val cleanQty = quantityStr.replace(',', '.').trim()
                val cleanPrice = priceStr.replace(',', '.').trim()

                val quantity = BigDecimal(cleanQty)
                val price = BigDecimal(cleanPrice)

                // Validační logika na straně ViewModelu před voláním UseCase
                if (quantity > BigDecimal.ZERO && price > BigDecimal.ZERO) {
                    addTransactionUseCase(ticker, type, quantity, price)
                    _uiState.update { it.copy(isShowingAddDialog = false) }
                }
            } catch (e: NumberFormatException) {
                // V reálné produkci zde proběhne logování chyby formátu
            }
        }
    }

    private fun executeDeleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            deleteTransactionUseCase(transactionId)
        }
    }
}