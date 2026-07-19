package cz.tomasjanicek.investment_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ticker: String,
    val name: String,
    val type: String, // BUY, SELL
    val quantity: BigDecimal,
    val pricePerShare: BigDecimal,
    val timestamp: LocalDateTime
)
