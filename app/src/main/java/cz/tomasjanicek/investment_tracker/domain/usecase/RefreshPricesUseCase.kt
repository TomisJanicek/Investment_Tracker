package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import javax.inject.Inject

/**
 * Use Case zajišťující synchronizaci tržních dat z externího zdroje do lokální databáze.
 * * Dodržuje pattern "Offline-First" – primárním zdrojem dat pro UI je vždy lokální
 * databáze, zatímco tento Use Case provádí pouze "background refresh", aby data
 * udržel aktuální.
 */
class RefreshPricesUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * Vyvolá aktualizaci cenových hladin na pozadí.
     * * * Operace probíhá jako [suspend] funkce, což zaručuje, že nedojde k blokování
     * hlavního UI vlákna.
     * * V případě výpadku konektivity je výjimka zachycena a může být dále
     * propagována do prezentační vrstvy pro zobrazení notifikace uživateli.
     *
     * @throws Exception Pokud dojde k selhání síťové komunikace nebo zápisu do úložiště.
     */
    suspend operator fun invoke() {
        try {
            // Delegování synchronizace na datovou vrstvu, která řeší komunikaci s API (Retrofit)
            // a následnou perzistenci (Room).
            repository.refreshCurrentPrices()
        } catch (e: Exception) {
            // Logika pro ošetření chybového stavu při aktualizaci:
            // Díky "Offline-First" přístupu aplikace zůstává plně funkční i při selhání
            // tohoto Use Casu, protože UI čte data z lokální mezipaměti.
            throw e
        }
    }
}