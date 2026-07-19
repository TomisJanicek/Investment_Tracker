package cz.tomasjanicek.investment_tracker.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Reprezentuje jeden cenový bod v historii aktiva.
 */
data class PricePointDomainModel(
    val price: BigDecimal,
    val timestamp: LocalDateTime
)