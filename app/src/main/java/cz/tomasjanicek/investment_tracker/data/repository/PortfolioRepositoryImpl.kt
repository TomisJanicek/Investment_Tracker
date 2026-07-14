package cz.tomasjanicek.investment_tracker.data.repository

import cz.tomasjanicek.investment_tracker.domain.model.AssetDetailDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.AssetDomainModel
import cz.tomasjanicek.investment_tracker.domain.model.TransactionDomainModel
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    // TODO: Zde bude injektováno Room DAO (private val dao: PortfolioDao) a Retrofit API klient
) : PortfolioRepository {

    /**
     * Dočasné in-memory úložiště transakcí sloužící jako Single Source of Truth (SSOT).
     * Díky použití StateFlow je zachována plná reaktivita - jakákoliv změna stavu
     * automaticky vyemituje nová data do všech aktivních odběratelů v doménové vrstvě.
     */
    private val transactionsFlow = MutableStateFlow(
        listOf(
            TransactionInternal("AAPL", "Apple Inc.", 1L, "BUY", BigDecimal("10"), BigDecimal("4200.50"), "10. 07. 2026"),
            TransactionInternal("AAPL", "Apple Inc.", 2L, "BUY", BigDecimal("5"), BigDecimal("4350.00"), "12. 07. 2026"),
            TransactionInternal("BTC", "Bitcoin", 3L, "BUY", BigDecimal("0.25"), BigDecimal("1500000.00"), "01. 06. 2026"),
            TransactionInternal("SXR8", "S&P 500 ETF (iShares)", 4L, "BUY", BigDecimal("4"), BigDecimal("12500.00"), "14. 07. 2026")
        )
    )

    /**
     * Cache aktuálních tržních cen (v CZK).
     * Slouží k výpočtu průběžné hodnoty aktiv a celkového zisku/ztráty portfolia.
     */
    private val currentPrices = mapOf(
        "AAPL" to BigDecimal("4500.00"),
        "BTC" to BigDecimal("1420000.00"),
        "SXR8" to BigDecimal("12850.00")
    )

    override fun getAllAssets(): Flow<List<AssetDomainModel>> {
        return transactionsFlow.map { transactions ->
            // Agregace transakcí podle tickeru pro výpočet aktuální otevřené pozice
            transactions.groupBy { it.ticker }.map { (ticker, txList) ->
                val name = txList.firstOrNull()?.name ?: ticker
                val price = currentPrices[ticker] ?: BigDecimal.ZERO

                var totalQty = BigDecimal.ZERO
                var totalInvested = BigDecimal.ZERO

                for (tx in txList) {
                    if (tx.type == "BUY") {
                        totalQty = totalQty.add(tx.quantity)
                        totalInvested = totalInvested.add(tx.quantity.multiply(tx.pricePerShare))
                    } else if (tx.type == "SELL") {
                        totalQty = totalQty.subtract(tx.quantity)

                        // Při redukci pozice (PRODEJ) ponížeme celkovou investovanou částku
                        // o původní nákupní hodnotu prodávaných kusů (cost basis reduction)
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
            }.filter { it.totalQuantity > BigDecimal.ZERO } // Do přehledu vracíme pouze aktivní pozice
        }
    }

    override fun getAssetDetails(ticker: String): Flow<AssetDetailDomainModel> {
        return transactionsFlow.map { transactions ->
            val assetTxs = transactions.filter { it.ticker.equals(ticker, ignoreCase = true) }
            val name = assetTxs.firstOrNull()?.name ?: ticker
            val price = currentPrices[ticker] ?: BigDecimal.ZERO

            var totalQty = BigDecimal.ZERO
            val domainTxs = assetTxs.map { tx ->
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
                    dateFormatted = tx.date
                )
            }

            AssetDetailDomainModel(
                ticker = ticker.uppercase(),
                name = name,
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
        price: BigDecimal
    ) {
        val newId = (transactionsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L

        // Mapování názvů pro známé tickery (bude nahrazeno vyhledáváním z API)
        val name = when (ticker.uppercase()) {
            "AAPL" -> "Apple Inc."
            "BTC" -> "Bitcoin"
            "SXR8" -> "S&P 500 ETF (iShares)"
            else -> ticker.uppercase()
        }

        val newTx = TransactionInternal(
            ticker = ticker.uppercase(),
            name = name,
            id = newId,
            type = type,
            quantity = quantity,
            pricePerShare = price,
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd. MM. yyyy"))
        )

        // Reaktivní aktualizace stavu vyvolá automatické překreslení závislých UI komponent
        transactionsFlow.update { currentList -> currentList + newTx }
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactionsFlow.update { currentList ->
            currentList.filterNot { it.id == transactionId }
        }
    }

    override suspend fun refreshCurrentPrices() {
        // Zde bude implementováno asynchronní stahování tržních dat z externího API
    }

    /**
     * Interní datová entita reprezentující záznam transakce v databázi.
     */
    private data class TransactionInternal(
        val ticker: String,
        val name: String,
        val id: Long,
        val type: String,
        val quantity: BigDecimal,
        val pricePerShare: BigDecimal,
        val date: String
    )
}