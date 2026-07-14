package cz.tomasjanicek.investment_tracker.presentation.detail

/**
 * Definuje uživatelské akce pro obrazovku detailu aktiva.
 * MVI: Představuje vstupní bod pro veškerou interakci uživatele s tímto modulem.
 */
sealed interface DetailAction {
    data object OnBackClicked : DetailAction
    data object OnAddTransactionClicked : DetailAction
    data object OnDismissDialog : DetailAction

    /**
     * Akce pro odeslání transakce.
     * Vstupy jsou jako [String], aby byla umožněna flexibilní validace
     * a ošetření formátů (desetinná čárka vs. tečka) před převodem na [BigDecimal].
     */
    data class OnSubmitTransaction(
        val type: String,
        val quantity: String,
        val price: String
    ) : DetailAction

    data class OnDeleteTransaction(val id: Long) : DetailAction
}