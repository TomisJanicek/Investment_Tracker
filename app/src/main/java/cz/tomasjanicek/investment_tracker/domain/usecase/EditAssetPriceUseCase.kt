package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

class EditAssetPriceUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(
        ticker: String,
        oldTimestamp: LocalDateTime,
        newPrice: BigDecimal,
        newTimestamp: LocalDateTime
    ) {
        require(ticker.isNotBlank()) { "Ticker nesmí být prázdný." }
        require(newPrice >= BigDecimal.ZERO) { "Cena nesmí být záporná." }
        
        repository.editAssetPrice(ticker.uppercase().trim(), oldTimestamp, newPrice, newTimestamp)
    }
}
