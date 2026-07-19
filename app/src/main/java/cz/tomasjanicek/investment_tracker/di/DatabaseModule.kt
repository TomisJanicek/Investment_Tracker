package cz.tomasjanicek.investment_tracker.di

import android.content.Context
import androidx.room.Room
import cz.tomasjanicek.investment_tracker.data.local.InvestmentDatabase
import cz.tomasjanicek.investment_tracker.data.local.dao.PortfolioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InvestmentDatabase {
        return Room.databaseBuilder(
            context,
            InvestmentDatabase::class.java,
            "investment_tracker.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun providePortfolioDao(database: InvestmentDatabase): PortfolioDao {
        return database.dao
    }
}
