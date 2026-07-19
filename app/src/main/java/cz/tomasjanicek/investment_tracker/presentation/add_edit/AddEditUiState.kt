package cz.tomasjanicek.investment_tracker.presentation.add_edit

import cz.tomasjanicek.investment_tracker.domain.model.AssetDefinitionDomainModel
import java.time.LocalDate
import java.time.LocalTime

/**
 * Reprezentuje stav obrazovky pro přidání nebo editaci aktiva/transakce.
 */
data class AddEditUiState(
    val ticker: String = "",
    val type: String = "BUY",
    val quantity: String = "",
    val price: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val isSaving: Boolean = false,
    val isTickerEnabled: Boolean = true,
    val errorMessage: String? = null,
    
    // Nové pro dropdowny
    val definedAssets: List<AssetDefinitionDomainModel> = emptyList(),
    val isDropdownExpanded: Boolean = false,
    val isTypeDropdownExpanded: Boolean = false,
    val isSuccess: Boolean = false
) {
    val canSave: Boolean = ticker.isNotBlank() && quantity.isNotBlank() && price.isNotBlank() && !isSaving && !isSuccess
}