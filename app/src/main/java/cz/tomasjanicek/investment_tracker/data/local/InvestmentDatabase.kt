package cz.tomasjanicek.investment_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.tomasjanicek.investment_tracker.data.local.dao.PortfolioDao
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.AssetPriceEntity
import cz.tomasjanicek.investment_tracker.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, AssetPriceEntity::class, AssetEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InvestmentDatabase : RoomDatabase() {
    abstract val dao: PortfolioDao
}
