package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateAssetPriceUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(ticker: String, price: BigDecimal, timestamp: LocalDateTime = LocalDateTime.now()) {
        require(ticker.isNotBlank()) { "Ticker nesmí být prázdný." }
        require(price >= BigDecimal.ZERO) { "Cena nesmí být záporná." }
        
        repository.addAssetPrice(ticker.uppercase().trim(), price, timestamp)
    }
}