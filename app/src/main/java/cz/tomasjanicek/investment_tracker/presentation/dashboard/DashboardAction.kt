package cz.tomasjanicek.investment_tracker.presentation.dashboard

sealed interface DashboardAction {
    object RefreshPrices : DashboardAction
    data class DeleteAsset(val ticker: String) : DashboardAction
    data class NavigateToDetail(val ticker: String) : DashboardAction
}