package cz.tomasjanicek.investment_tracker.presentation.manage_assets

import cz.tomasjanicek.investment_tracker.domain.model.AssetDefinitionDomainModel

sealed interface ManageAssetsAction {
    data object OnShowAddSheet : ManageAssetsAction
    data class OnShowEditSheet(val asset: AssetDefinitionDomainModel) : ManageAssetsAction
    data class OnShowDeleteSheet(val asset: AssetDefinitionDomainModel) : ManageAssetsAction
    data object OnDismissSheet : ManageAssetsAction
    
    data class OnTickerInputChanged(val ticker: String) : ManageAssetsAction
    data class OnNameInputChanged(val name: String) : ManageAssetsAction
    
    data object OnConfirmClicked : ManageAssetsAction
    data object OnBackClicked : ManageAssetsAction
}
