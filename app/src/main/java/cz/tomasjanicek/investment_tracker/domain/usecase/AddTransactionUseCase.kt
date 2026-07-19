package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use Case zodpovědný za validaci a zápis nové obchodní transakce do portfolia.
 * * Slouží jako doménová bariéra – zajišťuje, že do datové vrstvy se dostanou
 * pouze konzistentní a formátově čistá data. Zabraňuje vzniku neplatných stavů v databázi.
 */
class AddTransactionUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * Spouští asynchronní zápis transakce s předchozí striktní validací vstupů.
     * Automaticky aktualizuje historii cen aktiva podle zadané nákupní/prodejní ceny.
     *
     * @param ticker Burzovní symbol aktiva (např. "aapl", "BTC").
     * @param type Typ transakce ("BUY" pro nákup, "SELL" pro prodej).
     * @param quantity Počet obchodovaných kusů.
     * @param price Cena za jednu jednotku v CZK.
     * @param timestamp Čas provedení transakce.
     * @throws IllegalArgumentException Pokud jakýkoliv ze vstupních parametrů porušuje doménová pravidla.
     */
    suspend operator fun invoke(
        ticker: String,
        type: String,
        quantity: BigDecimal,
        price: BigDecimal,
        timestamp: LocalDateTime = LocalDateTime.now()
    ) {
        // 1. Striktní doménová validace vstupních parametrů
        require(ticker.isNotBlank()) { "Ticker nesmí být prázdný." }
        require(type == "BUY" || type == "SELL") { "Neplatný typ transakce: $type" }
        require(quantity > BigDecimal.ZERO) { "Počet kusů musí být větší než 0." }
        require(price > BigDecimal.ZERO) { "Cena musí být větší než 0." }

        val cleanTicker = ticker.uppercase().trim()

        // 2. Uložení transakce
        repository.addTransaction(
            ticker = cleanTicker,
            type = type,
            quantity = quantity,
            price = price
        )

        // 3. Automatický sync do historie cen aktiva
        repository.addAssetPrice(
            ticker = cleanTicker,
            price = price,
            timestamp = timestamp
        )
    }
}
