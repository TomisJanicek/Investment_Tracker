package cz.tomasjanicek.investment_tracker.data.local.dao

import androidx.room.*
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetPriceEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface PortfolioDao {

    // --- ASSETS (TICKERS) ---

    @Query("SELECT * FROM assets ORDER BY ticker ASC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE ticker = :ticker")
    suspend fun deleteAsset(ticker: String)

    @Query("SELECT * FROM assets WHERE ticker = :ticker")
    suspend fun getAssetByTicker(ticker: String): AssetEntity?

    // --- TRANSACTIONS ---

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM transactions WHERE ticker = :ticker ORDER BY timestamp DESC")
    fun getTransactionsForTicker(ticker: String): Flow<List<TransactionEntity>>


    // --- ASSET PRICES ---

    @Query("SELECT * FROM asset_prices ORDER BY timestamp DESC")
    fun getAllAssetPrices(): Flow<List<AssetPriceEntity>>

    @Query("SELECT * FROM asset_prices WHERE ticker = :ticker ORDER BY timestamp DESC")
    fun getPriceHistoryForTicker(ticker: String): Flow<List<AssetPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetPrice(assetPrice: AssetPriceEntity)

    @Query("DELETE FROM asset_prices WHERE ticker = :ticker AND timestamp = :timestamp")
    suspend fun deleteAssetPrice(ticker: String, timestamp: LocalDateTime)

    @Query("UPDATE asset_prices SET price = :newPrice, timestamp = :newTimestamp WHERE ticker = :ticker AND timestamp = :oldTimestamp")
    suspend fun updateAssetPrice(ticker: String, oldTimestamp: LocalDateTime, newPrice: java.math.BigDecimal, newTimestamp: LocalDateTime)
}
