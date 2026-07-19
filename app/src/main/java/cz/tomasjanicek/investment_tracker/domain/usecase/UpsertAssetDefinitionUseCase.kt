package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import javax.inject.Inject

class UpsertAssetDefinitionUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    suspend operator fun invoke(ticker: String, name: String) {
        require(ticker.isNotBlank()) { "Ticker nesmí být prázdný." }
        require(name.isNotBlank()) { "Název nesmí být prázdný." }
        repository.upsertAssetDefinition(ticker, name)
    }
}
