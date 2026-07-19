package cz.tomasjanicek.investment_tracker.presentation.manage_prices

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

data class ManagePricesUiState(
    val prices: List<AssetPriceUiModel> = emptyList(),
    val expandedTicker: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    
    // BottomSheet stav
    val isBottomSheetVisible: Boolean = false,
    val bottomSheetMode: BottomSheetMode = BottomSheetMode.ADD,
    val targetPricePoint: PriceHistoryUiModel? = null,
    
    // Pole ve formuláři (v BottomSheetu)
    val priceInput: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now()
)

enum class BottomSheetMode {
    ADD, EDIT, DELETE
}

data class AssetPriceUiModel(
    val ticker: String,
    val currentPrice: BigDecimal,
    val history: List<PriceHistoryUiModel> = emptyList()
)

data class PriceHistoryUiModel(
    val price: BigDecimal,
    val dateTimeFormatted: String,
    val timestamp: java.time.LocalDateTime
)