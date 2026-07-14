package cz.tomasjanicek.investment_tracker.presentation.detail

import java.math.BigDecimal

/**
 * Reprezentuje aktuální stav obrazovky detailu aktiva.
 * Model je navržen tak, aby obsahoval jak data z doménové vrstvy,
 * tak UI-specifický stav (např. viditelnost dialogového okna).
 */
data class DetailUiState(
    val ticker: String = "",
    val name: String = "",
    val currentPrice: BigDecimal = BigDecimal.ZERO,
    val totalQuantity: BigDecimal = BigDecimal.ZERO,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val transactions: List<TransactionUiModel> = emptyList(),
    val isShowingAddDialog: Boolean = false
)

/**
 * UI model jedné transakce, připravený k okamžitému zobrazení v seznamu.
 */
data class TransactionUiModel(
    val id: Long,
    val type: String,
    val quantity: BigDecimal,
    val pricePerShare: BigDecimal,
    val totalPrice: BigDecimal,
    val dateFormatted: String
)