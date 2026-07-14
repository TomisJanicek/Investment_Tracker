package cz.tomasjanicek.investment_tracker.domain.model

import java.math.BigDecimal

/**
 * Doménová reprezentace jedné otevřené pozice v portfoliu (např. AAPL, BTC).
 */
data class AssetDomainModel(
    val ticker: String,
    val name: String,
    val currentPrice: BigDecimal,
    val totalQuantity: BigDecimal,
    /**
     * Celková částka (v CZK), která byla do aktiva reálně investována.
     * Počítá se agregací z historie nákupů a je ponížena při částečných prodejích (cost basis).
     */
    val totalInvested: BigDecimal
)