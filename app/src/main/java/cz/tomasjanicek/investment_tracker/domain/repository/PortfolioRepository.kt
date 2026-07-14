package cz.tomasjanicek.investment_tracker.domain.repository

import cz.tomasjanicek.investment_tracker.domain.model.AssetDetailDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.AssetDomainModel
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * Centrální kontrakt doménové vrstvy pro přístup k datům portfolia.
 * * Využívá principu Dependency Inversion (DIP): doménová vrstva pouze definuje,
 * JAKÁ data a operace ke svému fungování potřebuje, ale neřeší ODKUD pocházejí
 * (zda z lokální Room databáze, REST API nebo mezipaměti). Tuto zodpovědnost
 * deleguje na vrstvu Data.
 */
interface PortfolioRepository {

    // --- AGREGACE A PŘEHLED PORTFOLIA (DASHBOARD) ---

    /**
     * Poskytuje kontinuální reaktivní stream aktivních investičních pozic.
     * * Použití [Flow] zaručuje, že při jakékoliv změně v podkladové databázi
     * (např. po uložení nové transakce nebo aktualizaci tržních kurzů) je doménové
     * i prezentační vrstvě automaticky vyemitován nový stav bez nutnosti manuálního dotazování.
     */
    fun getAllAssets(): Flow<List<AssetDomainModel>>

    /**
     * Spouští asynchronní aktualizaci tržních cen z externího zdroje (REST API).
     * * Metoda nevrací žádná data přímo do UI – plní pouze roli spouštěče pro synchronizaci
     * s lokální databází (Pattern: Single Source of Truth / Offline-First). Nové kurzy
     * se do aplikace propíšou automaticky prostřednictvím aktivních [Flow] streamů.
     */
    suspend fun refreshCurrentPrices()


    // --- DETAIL AKTIVA A AUDITNÍ STOPA TRANSAKCÍ ---

    /**
     * Vrací reaktivní detail konkrétního aktiva včetně kompletní historie jeho transakcí.
     *
     * @param ticker Unikátní burzovní symbol aktiva (např. "AAPL", "BTC").
     */
    fun getAssetDetails(ticker: String): Flow<AssetDetailDomainModel>

    /**
     * Zaznamená novou obchodní operaci (nákup nebo prodej) do perzistentního úložiště.
     * * Zápis je prováděn jako [suspend] funkce mimo hlavní vlákno (I/O operace).
     * Úspěšné uložení transakce automaticky vyvolá přepočet agregovaných pozic v [getAllAssets].
     *
     * @param ticker Burzovní symbol aktiva.
     * @param type Typ transakce ("BUY" pro nákup, "SELL" pro redukci pozice).
     * @param quantity Počet kusů (přísně kladné číslo v [BigDecimal]).
     * @param price Cena za jednu jednotku v měně portfolia.
     */
    suspend fun addTransaction(ticker: String, type: String, quantity: BigDecimal, price: BigDecimal)

    /**
     * Trvale odstraní transakci z auditní stopy podle jejího unikátního identifikátoru.
     *
     * @param transactionId Primární klíč transakce v databázi.
     */
    suspend fun deleteTransaction(transactionId: Long)
}