package cz.tomasjanicek.investment_tracker.data.repository

import cz.tomasjanicek.investment_tracker.data.local.dao.PortfolioDao
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetPriceEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.TransactionEntity
import cz.tomasjanicek.investment_tracker.domain.model.*
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val dao: PortfolioDao
) : PortfolioRepository {

    override fun getPrices(): Flow<Map<String, BigDecimal>> {
        return dao.getAllAssetPrices().map { allPrices ->
            allPrices.groupBy { it.ticker }.mapValues { (_, prices) ->
                prices.maxByOrNull { it.timestamp }?.price ?: BigDecimal.ZERO
            }
        }
    }

    override fun getPriceHistory(ticker: String): Flow<List<PricePointDomainModel>> {
        return dao.getPriceHistoryForTicker(ticker.uppercase()).map { entities ->
            entities.map { PricePointDomainModel(it.price, it.timestamp) }
        }
    }

    override fun getAllAssets(): Flow<List<AssetDomainModel>> {
        return combine(
            dao.getAllTransactions(), 
            getPrices(),
            dao.getAllAssets()
        ) { transactions, prices, assets ->
            val assetMap = assets.associateBy { it.ticker }
            
            transactions.groupBy { it.ticker }.map { (ticker, txList) ->
                val name = assetMap[ticker]?.name ?: txList.firstOrNull()?.name ?: ticker
                val price = prices[ticker] ?: BigDecimal.ZERO

                var totalQty = BigDecimal.ZERO
                var totalInvested = BigDecimal.ZERO

                for (tx in txList) {
                    if (tx.type == "BUY") {
                        totalQty = totalQty.add(tx.quantity)
                        totalInvested = totalInvested.add(tx.quantity.multiply(tx.pricePerShare))
                    } else if (tx.type == "SELL") {
                        totalQty = totalQty.subtract(tx.quantity)
                        val costBasisOfSold = tx.quantity.multiply(tx.pricePerShare)
                        totalInvested = totalInvested.subtract(costBasisOfSold).max(BigDecimal.ZERO)
                    }
                }

                AssetDomainModel(
                    ticker = ticker,
                    name = name,
                    currentPrice = price,
                    totalQuantity = totalQty,
                    totalInvested = totalInvested
                )
            }.filter { it.totalQuantity > BigDecimal.ZERO }
        }
    }

    override fun getAssetDetails(ticker: String): Flow<AssetDetailDomainModel> {
        val tickerUpper = ticker.uppercase()
        return combine(
            dao.getTransactionsForTicker(tickerUpper), 
            getPrices(),
            dao.getAllAssets()
        ) { transactions, prices, assets ->
            val assetName = assets.find { it.ticker == tickerUpper }?.name ?: transactions.firstOrNull()?.name ?: tickerUpper
            val price = prices[tickerUpper] ?: BigDecimal.ZERO

            var totalQty = BigDecimal.ZERO
            val domainTxs = transactions.map { tx ->
                if (tx.type == "BUY") {
                    totalQty = totalQty.add(tx.quantity)
                } else if (tx.type == "SELL") {
                    totalQty = totalQty.subtract(tx.quantity)
                }

                TransactionDomainModel(
                    id = tx.id,
                    type = tx.type,
                    quantity = tx.quantity,
                    pricePerShare = tx.pricePerShare,
                    dateFormatted = tx.timestamp.format(DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm"))
                )
            }

            AssetDetailDomainModel(
                ticker = tickerUpper,
                name = assetName,
                currentPrice = price,
                totalQuantity = totalQty,
                transactions = domainTxs
            )
        }
    }

    override suspend fun addTransaction(
        ticker: String,
        type: String,
        quantity: BigDecimal,
        price: BigDecimal,
        timestamp: LocalDateTime
    ) {
        val tickerUpper = ticker.uppercase()
        
        // Zjistíme, zda už aktivum máme definované, abychom použili jeho název
        val existingAsset = dao.getAssetByTicker(tickerUpper)
        val name = existingAsset?.name ?: when (tickerUpper) {
            "AAPL" -> "Apple Inc."
            "BTC" -> "Bitcoin"
            "SXR8" -> "S&P 500 ETF (iShares)"
            else -> tickerUpper
        }

        // Pokud aktivum neexistuje, vytvoříme ho (automatické založení)
        if (existingAsset == null) {
            dao.upsertAsset(AssetEntity(ticker = tickerUpper, name = name))
        }

        dao.insertTransaction(
            TransactionEntity(
                ticker = tickerUpper,
                name = name,
                type = type,
                quantity = quantity,
                pricePerShare = price,
                timestamp = timestamp
            )
        )
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        dao.deleteTransaction(transactionId)
    }

    override suspend fun addAssetPrice(ticker: String, price: BigDecimal, timestamp: LocalDateTime) {
        dao.insertAssetPrice(
            AssetPriceEntity(
                ticker = ticker.uppercase(),
                price = price,
                timestamp = timestamp
            )
        )
    }

    override suspend fun deleteAssetPrice(ticker: String, timestamp: LocalDateTime) {
        dao.deleteAssetPrice(ticker.uppercase(), timestamp)
    }

    override suspend fun editAssetPrice(
        ticker: String,
        oldTimestamp: LocalDateTime,
        newPrice: BigDecimal,
        newTimestamp: LocalDateTime
    ) {
        dao.updateAssetPrice(ticker.uppercase(), oldTimestamp, newPrice, newTimestamp)
    }

    override fun getAllDefinedAssets(): Flow<List<AssetDefinitionDomainModel>> {
        return dao.getAllAssets().map { entities ->
            entities.map { AssetDefinitionDomainModel(it.ticker, it.name) }
        }
    }

    override suspend fun upsertAssetDefinition(ticker: String, name: String) {
        dao.upsertAsset(AssetEntity(ticker.uppercase(), name))
    }

    override suspend fun deleteAssetDefinition(ticker: String) {
        dao.deleteAsset(ticker.uppercase())
    }

    override suspend fun refreshCurrentPrices() {
        // Future API implementation
    }
}
