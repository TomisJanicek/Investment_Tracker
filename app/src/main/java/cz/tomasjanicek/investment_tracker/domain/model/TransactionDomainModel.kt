package cz.tomasjanicek.investment_tracker.domain.model

import java.math.BigDecimal

/**
 * Neměnná (immutable) reprezentace jedné historické transakce (nákup nebo prodej).
 */
data class TransactionDomainModel(
    val id: Long,
    /**
     * Typ operace na trhu. Možné hodnoty jsou primárně "BUY" (nákup) nebo "SELL" (prodej).
     */
    val type: String,
    val quantity: BigDecimal,
    val pricePerShare: BigDecimal,
    /**
     * Textová reprezentace data transakce připravená pro zobrazení v UI.
     * Formátování probíhá na úrovni repozitáře/mapperu, aby UI vrstva zůstala maximálně jednoduchá.
     */
    val dateFormatted: String
)