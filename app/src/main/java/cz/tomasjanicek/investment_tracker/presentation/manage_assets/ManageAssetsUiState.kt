package cz.tomasjanicek.investment_tracker.presentation.manage_assets

import cz.tomasjanicek.investment_tracker.domain.model.AssetDefinitionDomainModel

data class ManageAssetsUiState(
    val assets: List<AssetDefinitionDomainModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    
    // BottomSheet stav
    val isBottomSheetVisible: Boolean = false,
    val bottomSheetMode: AssetBottomSheetMode = AssetBottomSheetMode.ADD,
    val targetAsset: AssetDefinitionDomainModel? = null,
    
    // Formulářová pole
    val tickerInput: String = "",
    val nameInput: String = ""
)

enum class AssetBottomSheetMode {
    ADD, EDIT, DELETE
}
