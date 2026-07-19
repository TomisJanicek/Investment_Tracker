package cz.tomasjanicek.investment_tracker.domain.repository

import cz.tomasjanicek.investment_tracker.domain.model.AssetDefinitionDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.AssetDetailDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.AssetDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.PricePointDomainModel
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

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
     * Poskytuje reaktivní stream mapy aktuálních cen (ticker -> cena).
     */
    fun getPrices(): Flow<Map<String, BigDecimal>>

    /**
     * Spouští asynchronní aktualizaci tržních cen z externího zdroje (REST API).
     */
    suspend fun refreshCurrentPrices()

    /**
     * Manuálně přidá nový cenový bod do historie konkrétního aktiva.
     */
    suspend fun addAssetPrice(ticker: String, price: BigDecimal, timestamp: LocalDateTime)

    /**
     * Vrací kompletní historii cen pro daný ticker.
     */
    fun getPriceHistory(ticker: String): Flow<List<PricePointDomainModel>>

    /**
     * Upraví existující cenový bod v historii.
     */
    suspend fun editAssetPrice(
        ticker: String,
        oldTimestamp: LocalDateTime,
        newPrice: BigDecimal,
        newTimestamp: LocalDateTime
    )

    /**
     * Trvale odstraní konkrétní cenový bod z historie.
     */
    suspend fun deleteAssetPrice(ticker: String, timestamp: LocalDateTime)


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
     * @param timestamp Čas provedení transakce.
     */
    suspend fun addTransaction(
        ticker: String, 
        type: String, 
        quantity: BigDecimal, 
        price: BigDecimal,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    /**
     * Trvale odstraní transakci z auditní stopy podle jejího unikátního identifikátoru.
     *
     * @param transactionId Primární klíč transakce v databázi.
     */
    suspend fun deleteTransaction(transactionId: Long)

    // --- ASSETS (TICKERS) ---

    /**
     * Vrací stream všech definovaných aktiv.
     */
    fun getAllDefinedAssets(): Flow<List<AssetDefinitionDomainModel>>

    /**
     * Vytvoří nebo upraví definici aktiva.
     */
    suspend fun upsertAssetDefinition(ticker: String, name: String)

    /**
     * Odstraní definici aktiva.
     */
    suspend fun deleteAssetDefinition(ticker: String)
}
