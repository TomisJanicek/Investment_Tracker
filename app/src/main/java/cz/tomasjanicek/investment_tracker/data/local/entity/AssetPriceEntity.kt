package cz.tomasjanicek.investment_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "asset_prices")
data class AssetPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ticker: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
)
