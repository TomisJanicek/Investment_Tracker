package cz.tomasjanicek.investment_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey
    val ticker: String,
    val name: String,
    val category: String = "Stock" // Defaultní kategorie
)
