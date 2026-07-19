package cz.tomasjanicek.investment_tracker.presentation.manage_prices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.investment_tracker.domain.usecase.DeleteAssetPriceUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.EditAssetPriceUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.GetAssetPricesUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.GetPriceHistoryUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.UpdateAssetPriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManagePricesViewModel @Inject constructor(
    private val getAssetPricesUseCase: GetAssetPricesUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val updateAssetPriceUseCase: UpdateAssetPriceUseCase,
    private val editAssetPriceUseCase: EditAssetPriceUseCase,
    private val deleteAssetPriceUseCase: DeleteAssetPriceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagePricesUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ManagePricesEvent>()
    val events = _events.receiveAsFlow()

    private val expandedTicker = MutableStateFlow<String?>(null)

    init {
        val pricesFlow = getAssetPricesUseCase()
        
        val historyFlow = expandedTicker.flatMapLatest { ticker ->
            if (ticker != null) {
                getPriceHistoryUseCase(ticker)
            } else {
                flowOf(emptyList())
            }
        }

        viewModelScope.launch {
            combine(pricesFlow, historyFlow, expandedTicker, _uiState) { domainPrices, history, expanded, state ->
                state.copy(
                    isLoading = false,
                    expandedTicker = expanded,
                    prices = domainPrices.map { (ticker, price) ->
                        AssetPriceUiModel(
                            ticker = ticker,
                            currentPrice = price,
                            history = if (ticker == expanded) {
                                history.map { 
                                    PriceHistoryUiModel(
                                        it.price, 
                                        it.timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                                        it.timestamp
                                    ) 
                                }
                            } else emptyList()
                        )
                    }.sortedBy { it.ticker }
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onAction(action: ManagePricesAction) {
        when (action) {
            is ManagePricesAction.OnToggleExpand -> {
                expandedTicker.value = if (expandedTicker.value == action.ticker) null else action.ticker
            }
            ManagePricesAction.OnShowAddSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = BottomSheetMode.ADD,
                    priceInput = "",
                    selectedDate = LocalDate.now(),
                    selectedTime = LocalTime.now(),
                    targetPricePoint = null
                ) }
            }
            is ManagePricesAction.OnShowEditSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = BottomSheetMode.EDIT,
                    targetPricePoint = action.pricePoint,
                    priceInput = action.pricePoint.price.toString(),
                    selectedDate = action.pricePoint.timestamp.toLocalDate(),
                    selectedTime = action.pricePoint.timestamp.toLocalTime()
                ) }
            }
            is ManagePricesAction.OnShowDeleteSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = BottomSheetMode.DELETE,
                    targetPricePoint = action.pricePoint
                ) }
            }
            ManagePricesAction.OnDismissSheet -> _uiState.update { it.copy(isBottomSheetVisible = false) }
            is ManagePricesAction.OnPriceInputChanged -> _uiState.update { it.copy(priceInput = action.price) }
            is ManagePricesAction.OnDateChanged -> _uiState.update { it.copy(selectedDate = action.date) }
            is ManagePricesAction.OnTimeChanged -> _uiState.update { it.copy(selectedTime = action.time) }
            ManagePricesAction.OnConfirmClicked -> handleConfirm()
            ManagePricesAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(ManagePricesEvent.NavigateBack) }
            }
        }
    }

    private fun handleConfirm() {
        val ticker = expandedTicker.value ?: return
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (state.bottomSheetMode) {
                    BottomSheetMode.ADD -> {
                        val cleanPrice = state.priceInput.replace(',', '.').trim()
                        val timestamp = LocalDateTime.of(state.selectedDate, state.selectedTime)
                        updateAssetPriceUseCase(ticker, BigDecimal(cleanPrice), timestamp)
                    }
                    BottomSheetMode.EDIT -> {
                        val oldTimestamp = state.targetPricePoint?.timestamp ?: return@launch
                        val cleanPrice = state.priceInput.replace(',', '.').trim()
                        val newTimestamp = LocalDateTime.of(state.selectedDate, state.selectedTime)
                        editAssetPriceUseCase(ticker, oldTimestamp, BigDecimal(cleanPrice), newTimestamp)
                    }
                    BottomSheetMode.DELETE -> {
                        val timestamp = state.targetPricePoint?.timestamp ?: return@launch
                        deleteAssetPriceUseCase(ticker, timestamp)
                    }
                }
                _uiState.update { it.copy(isBottomSheetVisible = false, isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    sealed interface ManagePricesEvent {
        data object NavigateBack : ManagePricesEvent
    }
}
