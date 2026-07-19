package cz.tomasjanicek.investment_tracker.presentation.dashboard

sealed interface DashboardAction {
    /**
     * Definuje veškeré uživatelské záměry (intentions) na obrazovce Dashboard.
     * MVI pattern: UI pouze odesílá tyto akce a pasivně reaguje na stav.
     */
    data object RefreshPrices : DashboardAction
    data class OnAssetClicked(val ticker: String) : DashboardAction
    data class OnDeleteAsset(val ticker: String) : DashboardAction
    data object OnAddAssetClicked : DashboardAction
    data object OnManagePricesClicked : DashboardAction
    data object OnManageAssetsClicked : DashboardAction
}