package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.model.PricePointDomainModel
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPriceHistoryUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    operator fun invoke(ticker: String): Flow<List<PricePointDomainModel>> {
        return repository.getPriceHistory(ticker)
    }
}