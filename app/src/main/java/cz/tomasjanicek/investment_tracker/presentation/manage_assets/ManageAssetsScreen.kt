package cz.tomasjanicek.investment_tracker.presentation.manage_assets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAssetsScreen(
    state: ManageAssetsUiState,
    events: Flow<ManageAssetsViewModel.ManageAssetsEvent>,
    onAction: (ManageAssetsAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                ManageAssetsViewModel.ManageAssetsEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onAction(ManageAssetsAction.OnDismissSheet) },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AssetBottomSheetContent(state = state, onAction = onAction)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Správa aktiv (Ticketů)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onAction(ManageAssetsAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(ManageAssetsAction.OnShowAddSheet) },
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.assets, key = { it.ticker }) { asset ->
                    AssetDefinitionItem(
                        asset = asset,
                        onEdit = { onAction(ManageAssetsAction.OnShowEditSheet(asset)) },
                        onDelete = { onAction(ManageAssetsAction.OnShowDeleteSheet(asset)) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssetDefinitionItem(
    asset: cz.tomasjanicek.investment_tracker.domain.model.AssetDefinitionDomainModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.ticker,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun AssetBottomSheetContent(
    state: ManageAssetsUiState,
    onAction: (ManageAssetsAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val title = when (state.bottomSheetMode) {
            AssetBottomSheetMode.ADD -> "Nové aktivum"
            AssetBottomSheetMode.EDIT -> "Upravit aktivum"
            AssetBottomSheetMode.DELETE -> "Smazat aktivum"
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (state.bottomSheetMode == AssetBottomSheetMode.DELETE) {
            Text(
                text = "Opravdu chcete smazat aktivum ${state.targetAsset?.ticker}? Smazání definice neovlivní stávající transakce v databázi, ale aktivum zmizí z výběru.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            OutlinedTextField(
                value = state.tickerInput,
                onValueChange = { onAction(ManageAssetsAction.OnTickerInputChanged(it)) },
                label = { Text("Ticker (např. AAPL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                enabled = state.bottomSheetMode == AssetBottomSheetMode.ADD, // Ticker nelze u existujícího měnit
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            OutlinedTextField(
                value = state.nameInput,
                onValueChange = { onAction(ManageAssetsAction.OnNameInputChanged(it)) },
                label = { Text("Název aktiva (např. Apple Inc.)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = { onAction(ManageAssetsAction.OnDismissSheet) },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Zrušit")
            }

            val buttonColor = if (state.bottomSheetMode == AssetBottomSheetMode.DELETE) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.secondary

            val contentColor = if (state.bottomSheetMode == AssetBottomSheetMode.DELETE)
                MaterialTheme.colorScheme.onError
            else
                MaterialTheme.colorScheme.onSecondary

            Button(
                onClick = { onAction(ManageAssetsAction.OnConfirmClicked) },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = contentColor
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving && (state.bottomSheetMode == AssetBottomSheetMode.DELETE || (state.tickerInput.isNotBlank() && state.nameInput.isNotBlank()))
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor)
                } else {
                    val label = if (state.bottomSheetMode == AssetBottomSheetMode.DELETE) "Smazat" else "Uložit"
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
