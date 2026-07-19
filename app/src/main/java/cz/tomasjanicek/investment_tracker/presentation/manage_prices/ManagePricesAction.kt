package cz.tomasjanicek.investment_tracker.presentation.manage_prices

import java.time.LocalDate
import java.time.LocalTime

sealed interface ManagePricesAction {
    data class OnToggleExpand(val ticker: String) : ManagePricesAction
    
    // BottomSheet akce
    data object OnShowAddSheet : ManagePricesAction
    data class OnShowEditSheet(val pricePoint: PriceHistoryUiModel) : ManagePricesAction
    data class OnShowDeleteSheet(val pricePoint: PriceHistoryUiModel) : ManagePricesAction
    data object OnDismissSheet : ManagePricesAction

    // Formulářové akce (v Sheetu)
    data class OnPriceInputChanged(val price: String) : ManagePricesAction
    data class OnDateChanged(val date: LocalDate) : ManagePricesAction
    data class OnTimeChanged(val time: LocalTime) : ManagePricesAction
    
    data object OnConfirmClicked : ManagePricesAction
    data object OnBackClicked : ManagePricesAction
}
