package cz.tomasjanicek.investment_tracker.presentation.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "Moje Portfolio",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { onAction(DashboardAction.OnManageAssetsClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Správa aktiv"
                        )
                    }
                    IconButton(onClick = { onAction(DashboardAction.OnManagePricesClicked) }) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Spravovat ceny"
                        )
                    }
                    IconButton(onClick = { onAction(DashboardAction.RefreshPrices) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Aktualizovat ceny"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(DashboardAction.OnAddAssetClicked) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Přidat aktivum",
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (state.isLoading && state.assets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 88.dp) // Místo pro FAB
            ) {
                // 1. M3 Expressive Hero Card pro celkovou hodnotu
                item {
                    PortfolioHeroCard(
                        totalValue = state.totalValue,
                        profitLoss = state.totalProfitLoss,
                        percentage = state.percentageChange
                    )
                }

                item {
                    Text(
                        text = "Moje aktiva",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // 2. Seznam aktiv
                items(state.assets, key = { it.ticker }) { asset ->
                    AssetExpressiveItem(
                        asset = asset,
                        onClick = { onAction(DashboardAction.OnAssetClicked(asset.ticker)) }
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioHeroCard(
    totalValue: BigDecimal,
    profitLoss: BigDecimal,
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val isPositive = percentage >= 0
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val accentColor = MaterialTheme.colorScheme.secondary // Muted Gold

    // M3 Expressive: Výrazné zaoblení (32.dp) a dynamická změna velikosti
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Celková hodnota",
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Expressive Typography - Zlatá pro hlavní číslo (Premium look)
            Text(
                text = formatCurrency(totalValue),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = contentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sign = if (isPositive) "+" else ""
                    val profitColor = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    
                    Text(
                        text = "$sign${formatCurrency(profitLoss)} ($sign${String.format("%.2f", percentage)} %)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isPositive) profitColor else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AssetExpressiveItem(
    asset: AssetUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Použití Periwinkle Blue (surfaceVariant) pro odlišení karet
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = asset.ticker,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${asset.totalQuantity} ks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(asset.totalValue),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val profitColor = if (asset.isProfit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                val sign = if (asset.isProfit) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", asset.profitLossPercentage)} %",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = profitColor
                )
            }
        }
    }
}

// Pomocná formátovací funkce (v reálu by byla v core/util modulu)
private fun formatCurrency(value: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("cs", "CZ"))
    return format.format(value)
}