package cz.tomasjanicek.investment_tracker.presentation.detail

/**
 * Definuje uživatelské akce pro obrazovku detailu aktiva.
 * MVI: Představuje vstupní bod pro veškerou interakci uživatele s tímto modulem.
 */
sealed interface DetailAction {
    data object OnBackClicked : DetailAction
    data object OnAddTransactionClicked : DetailAction
    data class OnDeleteTransaction(val id: Long) : DetailAction
}