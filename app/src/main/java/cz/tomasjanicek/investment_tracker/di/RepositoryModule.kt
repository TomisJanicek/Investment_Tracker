package cz.tomasjanicek.investment_tracker.di

import cz.tomasjanicek.investment_tracker.data.repository.PortfolioRepositoryImpl
import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger/Hilt modul zajišťující propojení doménových rozhraní s jejich datovými implementacemi.
 * Využívá principu Dependency Inversion – doménová vrstva tak zůstává zcela nezávislá na datové vrstvě.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Spáruje abstrakci [PortfolioRepository] s konkrétní implementací [PortfolioRepositoryImpl].
     * * Anotace @Singleton je kritická pro udržení konzistence dat (Single Source of Truth) –
     * zaručuje, že všechny ViewModely napříč aplikací pracují se stejnou instancí repozitáře
     * a sdílejí jednotný reaktivní stream transakcí.
     */
    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(
        portfolioRepositoryImpl: PortfolioRepositoryImpl
    ): PortfolioRepository
}