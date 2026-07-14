package cz.tomasjanicek.investment_tracker.domain.model

import java.math.BigDecimal

/**
 * Komplexní doménový model pro detail jednoho konkrétního aktiva.
 * Kromě aktuálního stavu pozice obsahuje i kompletní auditní stopu transakcí.
 */
data class AssetDetailDomainModel(
    val ticker: String,
    val name: String,
    val currentPrice: BigDecimal,
    val totalQuantity: BigDecimal,
    val transactions: List<TransactionDomainModel>
)