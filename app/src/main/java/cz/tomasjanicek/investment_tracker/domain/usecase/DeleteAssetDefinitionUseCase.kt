package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import javax.inject.Inject

class DeleteAssetDefinitionUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(ticker: String) {
        repository.deleteAssetDefinition(ticker)
    }
}
