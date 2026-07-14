package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.model.AssetDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.PortfolioDomainModel
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Use Case pro agregaci portfolia a výpočet jeho klíčových metrik.
 * * Zajišťuje transformaci surových dat z repozitáře na ucelený doménový model
 * obsahující vypočítané finanční ukazatele, které jsou připraveny k zobrazení v UI.
 */
class GetPortfolioUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * Poskytuje reaktivní stream ([Flow]) vypočítaného stavu portfolia.
     * * Jakákoliv změna v datech (aktualizace kurzů, nová transakce) iniciuje přepočet
     * v [calculatePortfolio], čímž je zaručena konzistence dat v reálném čase.
     */
    operator fun invoke(): Flow<PortfolioDomainModel> {
        return repository.getAllAssets().map { assets ->
            calculatePortfolio(assets)
        }
    }

    /**
     * Čistá byznys logika pro výpočet finančních ukazatelů portfolia.
     * * Metoda je navržena jako "pure function" bez vedlejších efektů,
     * což umožňuje snadnou testovatelnost pomocí Unit Testů bez nutnosti mockovat repozitář.
     *
     * @param assets Seznam aktiv aktuálně držených v portfoliu.
     * @return Agregovaný model [PortfolioDomainModel] se součty a seřazenými aktivy.
     */
    fun calculatePortfolio(assets: List<AssetDomainModel>): PortfolioDomainModel {
        if (assets.isEmpty()) {
            return PortfolioDomainModel()
        }

        var totalPortfolioValue = BigDecimal.ZERO
        var totalPortfolioInvested = BigDecimal.ZERO

        // Iterativní výpočet celkové hodnoty a investované částky (cost basis)
        for (asset in assets) {
            val assetCurrentValue = asset.currentPrice.multiply(asset.totalQuantity)

            totalPortfolioValue = totalPortfolioValue.add(assetCurrentValue)
            totalPortfolioInvested = totalPortfolioInvested.add(asset.totalInvested)
        }

        // Celkový nerealizovaný zisk nebo ztráta
        val totalProfitLoss = totalPortfolioValue.subtract(totalPortfolioInvested)

        // Výpočet procentuální výkonnosti portfolia.
        // Použita přesnost na 4 desetinná místa pro zachování finanční validity.
        val percentageChange: Double = if (totalPortfolioInvested > BigDecimal.ZERO) {
            val ratio = totalProfitLoss.divide(
                totalPortfolioInvested,
                4,
                RoundingMode.HALF_UP
            )
            ratio.multiply(BigDecimal(100)).toDouble()
        } else {
            0.0
        }

        // Aktiva jsou řazena dle jejich tržní hodnoty (největší podíl v portfoliu nahoře),
        // aby uživatel viděl nejdůležitější složky portfolia jako první.
        val sortedAssets = assets.sortedByDescending {
            it.currentPrice.multiply(it.totalQuantity)
        }

        return PortfolioDomainModel(
            totalValue = totalPortfolioValue,
            totalProfitLoss = totalProfitLoss,
            percentageChange = percentageChange,
            assets = sortedAssets
        )
    }
}