package cz.tomasjanicek.investment_tracker.presentation.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.investment_tracker.ui.common.ExpressiveDatePickerField
import cz.tomasjanicek.investment_tracker.ui.common.ExpressiveTimePickerField
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    state: AddEditUiState,
    events: Flow<AddEditViewModel.AddEditEvent>,
    onAction: (AddEditAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                AddEditViewModel.AddEditEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (state.isTickerEnabled) "Přidat aktivum" else "Nová transakce",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(AddEditAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ticker Dropdown
                ExposedDropdownMenuBox(
                    expanded = state.isDropdownExpanded,
                    onExpandedChange = { onAction(AddEditAction.OnDropdownExpandedChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.ticker,
                        onValueChange = { onAction(AddEditAction.OnTickerChanged(it)) },
                        label = { Text("Ticker (např. AAPL, BTC)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                        enabled = state.isTickerEnabled,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            if (state.isTickerEnabled) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded)
                            }
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary, // Muted Gold
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    if (state.isTickerEnabled && state.definedAssets.isNotEmpty()) {
                        val filteredOptions = state.definedAssets.filter {
                            it.ticker.contains(state.ticker, ignoreCase = true)
                        }
                        if (filteredOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = state.isDropdownExpanded,
                                onDismissRequest = { onAction(AddEditAction.OnDropdownExpandedChanged(false)) },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                filteredOptions.forEach { asset ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = asset.ticker, 
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary // Midnight Navy
                                                )
                                                Text(
                                                    text = asset.name, 
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            onAction(AddEditAction.OnTickerChanged(asset.ticker))
                                            onAction(AddEditAction.OnDropdownExpandedChanged(false))
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }

                // Typ transakce (Dropdown místo SegmentedButton)
                ExposedDropdownMenuBox(
                    expanded = state.isTypeDropdownExpanded,
                    onExpandedChange = { onAction(AddEditAction.OnTypeDropdownExpandedChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val typeLabel = if (state.type == "BUY") "Nákup" else "Prodej"
                    
                    OutlinedTextField(
                        value = typeLabel,
                        onValueChange = {},
                        label = { Text("Typ transakce") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isTypeDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = state.isTypeDropdownExpanded,
                        onDismissRequest = { onAction(AddEditAction.OnTypeDropdownExpandedChanged(false)) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Nákup", 
                                    color = MaterialTheme.colorScheme.tertiary, // Profit Green
                                    fontWeight = FontWeight.Bold 
                                ) 
                            },
                            onClick = {
                                onAction(AddEditAction.OnTypeChanged("BUY"))
                                onAction(AddEditAction.OnTypeDropdownExpandedChanged(false))
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Prodej", 
                                    color = MaterialTheme.colorScheme.error, // Loss Red
                                    fontWeight = FontWeight.Bold 
                                ) 
                            },
                            onClick = {
                                onAction(AddEditAction.OnTypeChanged("SELL"))
                                onAction(AddEditAction.OnTypeDropdownExpandedChanged(false))
                            }
                        )
                    }
                }

                // Množství
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = { onAction(AddEditAction.OnQuantityChanged(it)) },
                    label = { Text("Počet kusů") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                // Cena
                OutlinedTextField(
                    value = state.price,
                    onValueChange = { onAction(AddEditAction.OnPriceChanged(it)) },
                    label = { Text("Cena za kus (CZK)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.secondary
                    )
                )

                // Datum a Čas
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpressiveDatePickerField(
                        label = "Datum transakce",
                        selectedDate = state.date,
                        onDateSelected = { onAction(AddEditAction.OnDateChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveTimePickerField(
                        label = "Čas",
                        selectedTime = state.time,
                        onTimeSelected = { onAction(AddEditAction.OnTimeChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onAction(AddEditAction.OnSaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.canSave,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Uložit transakci", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // SUCCESS ANIMATION OVERLAY
            if (state.isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    DotLottieAnimation(
                        source = DotLottieSource.Asset("coin.lottie"),
                        autoplay = true,
                        loop = true,
                        modifier = Modifier.size(250.dp)
                    )
                }
            }
        }
    }
}
