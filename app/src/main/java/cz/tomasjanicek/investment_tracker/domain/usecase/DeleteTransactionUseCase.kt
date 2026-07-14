package cz.tomasjanicek.investment_tracker.domain.usecase

import cz.tomasjanicek.investment_tracker.domain.repository.PortfolioRepository
import javax.inject.Inject

/**
 * Use Case pro odstranění existující transakce z historie portfolia.
 * * Zajišťuje, aby byly před smazáním dat provedeny nezbytné validace.
 * V reálné bankovní aplikaci slouží tato vrstva také jako místo pro zápis
 * do auditního logu (audit trail) pro účely dohledatelnosti operací.
 */
class DeleteTransactionUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * Odstraní transakci z úložiště na základě jejího unikátního identifikátoru.
     *
     * @param transactionId Unikátní ID transakce. Musí být větší než 0.
     * @throws IllegalArgumentException Pokud je předáno neplatné ID (<= 0).
     */
    suspend operator fun invoke(transactionId: Long) {
        // Validace vstupu: Ujistíme se, že pracujeme s reálným identifikátorem
        // předtím, než požadavek pošleme do datové vrstvy.
        require(transactionId > 0) { "Neplatné ID transakce: $transactionId" }

        // Smazání záznamu z repozitáře
        repository.deleteTransaction(transactionId)

        // TODO: V budoucí verzi implementovat volání logovací služby (AuditService),
        // která zaznamená smazání transakce pro interní potřeby bankovního systému.
    }
}