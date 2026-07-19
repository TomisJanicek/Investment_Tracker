package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import java.time.LocalDateTime
import javax.inject.Inject

class DeleteAssetPriceUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(ticker: String, timestamp: LocalDateTime) {
        repository.deleteAssetPrice(ticker, timestamp)
    }
}