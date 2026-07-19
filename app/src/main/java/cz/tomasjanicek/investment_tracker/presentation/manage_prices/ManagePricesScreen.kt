package cz.tomasjanicek.investment_tracker.presentation.manage_prices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.investment_tracker.ui.common.ExpressiveDatePickerField
import cz.tomasjanicek.investment_tracker.ui.common.ExpressiveTimePickerField
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePricesScreen(
    state: ManagePricesUiState,
    events: Flow<ManagePricesViewModel.ManagePricesEvent>,
    onAction: (ManagePricesAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                ManagePricesViewModel.ManagePricesEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onAction(ManagePricesAction.OnDismissSheet) },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            BottomSheetContent(state = state, onAction = onAction)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Správa cen aktiv", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onAction(ManagePricesAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.prices, key = { it.ticker }) { item ->
                    PriceHistoryExpandableCard(
                        item = item,
                        isExpanded = state.expandedTicker == item.ticker,
                        onToggle = { onAction(ManagePricesAction.OnToggleExpand(item.ticker)) },
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
fun PriceHistoryExpandableCard(
    item: AssetPriceUiModel,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAction: (ManagePricesAction) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) // Jemná zlatá při rozbalení
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Periwinkle Blue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggle() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.ticker,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Aktuálně: ${formatCurrency(item.currentPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Méně" else "Více",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historie cen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        FilledTonalButton(
                            onClick = { onAction(ManagePricesAction.OnShowAddSheet) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Přidat cenu")
                        }
                    }

                    if (item.history.isEmpty()) {
                        Text(
                            text = "Žádná historie záznamů",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        item.history.forEach { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = historyItem.dateTimeFormatted,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = formatCurrency(historyItem.price),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onAction(ManagePricesAction.OnShowEditSheet(historyItem)) }) {
                                        Icon(
                                            Icons.Default.Edit, 
                                            contentDescription = "Upravit", 
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(onClick = { onAction(ManagePricesAction.OnShowDeleteSheet(historyItem)) }) {
                                        Icon(
                                            Icons.Default.Delete, 
                                            contentDescription = "Smazat", 
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetContent(
    state: ManagePricesUiState,
    onAction: (ManagePricesAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp), // Padding od spodku displeje
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val title = when (state.bottomSheetMode) {
            BottomSheetMode.ADD -> "Přidat cenový bod"
            BottomSheetMode.EDIT -> "Upravit cenový bod"
            BottomSheetMode.DELETE -> "Smazat cenový bod"
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (state.bottomSheetMode == BottomSheetMode.DELETE) {
            Text(
                text = "Opravdu chcete smazat tento záznam?",
                style = MaterialTheme.typography.bodyLarge
            )
            state.targetPricePoint?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Cena: ${formatCurrency(it.price)}", fontWeight = FontWeight.Bold)
                        Text(text = "Datum: ${it.dateTimeFormatted}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // Formulář pro ADD a EDIT
            OutlinedTextField(
                value = state.priceInput,
                onValueChange = { onAction(ManagePricesAction.OnPriceInputChanged(it)) },
                label = { Text("Cena (CZK)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExpressiveDatePickerField(
                    label = "Datum",
                    selectedDate = state.selectedDate,
                    onDateSelected = { onAction(ManagePricesAction.OnDateChanged(it)) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveTimePickerField(
                    label = "Čas",
                    selectedTime = state.selectedTime,
                    onTimeSelected = { onAction(ManagePricesAction.OnTimeChanged(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = { onAction(ManagePricesAction.OnDismissSheet) },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Zrušit")
            }
            
            val buttonColor = when (state.bottomSheetMode) {
                BottomSheetMode.DELETE -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.secondary
            }
            
            val contentColor = when (state.bottomSheetMode) {
                BottomSheetMode.DELETE -> MaterialTheme.colorScheme.onError
                else -> MaterialTheme.colorScheme.onSecondary
            }
                
            Button(
                onClick = { onAction(ManagePricesAction.OnConfirmClicked) },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = contentColor
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving && (state.bottomSheetMode == BottomSheetMode.DELETE || state.priceInput.isNotBlank())
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor)
                } else {
                    val label = if (state.bottomSheetMode == BottomSheetMode.DELETE) "Smazat" else "Uložit"
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun formatCurrency(value: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("cs", "CZ"))
    return format.format(value)
}
