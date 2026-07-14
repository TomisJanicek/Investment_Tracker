package cz.tomasjanicek.investment_tracker.domain.model

import java.math.BigDecimal

/**
 * Hlavní agregační kořen (Aggregate Root) pro přehled portfolia na Dashboardu.
 * Nese celkové vypočítané součty za všechna aktiva.
 * * Všechny peněžní hodnoty využívají [BigDecimal], aby se zabránilo nepřesnostem
 * v zaokrouhlování, které jsou typické pro typy Float a Double.
 */
data class PortfolioDomainModel(
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val totalProfitLoss: BigDecimal = BigDecimal.ZERO,
    val percentageChange: Double = 0.0,
    val assets: List<AssetDomainModel> = emptyList()
)