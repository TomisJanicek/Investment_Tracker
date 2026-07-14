package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.model.AssetDetailDomainModel
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use Case pro získání detailních informací o konkrétním investičním aktivu.
 * * Zajišťuje, že uživatelské rozhraní dostane vždy aktuální a správně seřazená data.
 */
class GetAssetDetailsUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * Vrací [Flow] s detaily aktiva.
     * * @param ticker Unikátní burzovní symbol (např. "AAPL").
     * @return Reaktivní stream [AssetDetailDomainModel].
     * * * Logika řazení transakcí je zapouzdřena zde v doménové vrstvě, aby bylo
     * UI vrstvě zaručeno, že dostane data vždy v požadovaném pořadí (od nejnovějších),
     * nezávisle na pořadí, ve kterém je vrací úložiště.
     */
    operator fun invoke(ticker: String): Flow<AssetDetailDomainModel> {
        return repository.getAssetDetails(ticker).map { domainModel ->
            // Uplatnění byznys pravidla pro prezentaci historie transakcí:
            // Nejnovější záznamy musí být vždy na začátku seznamu pro lepší čitelnost.
            val sortedTransactions = domainModel.transactions.sortedByDescending { it.id }

            // Vytvoření nové instance modelu s transformovanou historií transakcí
            domainModel.copy(
                transactions = sortedTransactions
            )
        }
    }
}