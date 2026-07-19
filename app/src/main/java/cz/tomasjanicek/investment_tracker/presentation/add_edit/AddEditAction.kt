package cz.tomasjanicek.investment_tracker.presentation.add_edit

import java.time.LocalDate
import java.time.LocalTime

sealed interface AddEditAction {
    data class OnTickerChanged(val ticker: String) : AddEditAction
    data class OnTypeChanged(val type: String) : AddEditAction
    data class OnQuantityChanged(val quantity: String) : AddEditAction
    data class OnPriceChanged(val price: String) : AddEditAction
    data class OnDateChanged(val date: LocalDate) : AddEditAction
    data class OnTimeChanged(val time: LocalTime) : AddEditAction
    data class OnDropdownExpandedChanged(val expanded: Boolean) : AddEditAction
    data class OnTypeDropdownExpandedChanged(val expanded: Boolean) : AddEditAction
    data object OnSaveClicked : AddEditAction
    data object OnBackClicked : AddEditAction
}
