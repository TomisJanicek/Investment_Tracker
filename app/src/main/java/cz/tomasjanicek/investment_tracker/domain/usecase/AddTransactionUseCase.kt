package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import java.math.BigDecimal
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
     * * Využívá defenzivního programování ([require]) – v případě porušení byznys
     * pravidel je okamžitě vyhozena výjimka [IllegalArgumentException], kterou prezentační
     * vrstva odchytí a transformuje do srozumitelné chybové hlášky pro uživatele.
     *
     * @param ticker Burzovní symbol aktiva (např. "aapl", "BTC").
     * @param type Typ transakce ("BUY" pro nákup, "SELL" pro prodej).
     * @param quantity Počet obchodovaných kusů.
     * @param price Cena za jednu jednotku v CZK.
     * @throws IllegalArgumentException Pokud jakýkoliv ze vstupních parametrů porušuje doménová pravidla.
     */
    suspend operator fun invoke(
        ticker: String,
        type: String,
        quantity: BigDecimal,
        price: BigDecimal
    ) {
        // 1. Striktní doménová validace vstupních parametrů
        require(ticker.isNotBlank()) { "Ticker nesmí být prázdný." }
        require(type == "BUY" || type == "SELL") { "Neplatný typ transakce: $type" }
        require(quantity > BigDecimal.ZERO) { "Počet kusů musí být větší než 0." }
        require(price > BigDecimal.ZERO) { "Cena musí být větší než 0." }

        // TODO: Pro typ "SELL" implementovat ověření aktuální otevřené pozice z repozitáře,
        //  aby se zamezilo prodeji nakrátko (short selling) nad rámec reálně vlastněných kusů.

        // 2. Sanitizace formátu (odstranění bílých znaků a unifikace na velká písmena)
        // a následné předání validovaného modelu do repozitáře.
        repository.addTransaction(
            ticker = ticker.uppercase().trim(),
            type = type,
            quantity = quantity,
            price = price
        )
    }
}