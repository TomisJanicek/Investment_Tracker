package cz.tomasjanicek.investment_tracker.presentation.manage_assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.investment_tracker.domain.usecase.DeleteAssetDefinitionUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.GetDefinedAssetsUseCase
import cz.tomasjanicek.investment_tracker.domain.usecase.UpsertAssetDefinitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageAssetsViewModel @Inject constructor(
    private val getDefinedAssetsUseCase: GetDefinedAssetsUseCase,
    private val upsertAssetDefinitionUseCase: UpsertAssetDefinitionUseCase,
    private val deleteAssetDefinitionUseCase: DeleteAssetDefinitionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageAssetsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ManageAssetsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            getDefinedAssetsUseCase().collect { assets ->
                _uiState.update { it.copy(isLoading = false, assets = assets) }
            }
        }
    }

    fun onAction(action: ManageAssetsAction) {
        when (action) {
            ManageAssetsAction.OnShowAddSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = AssetBottomSheetMode.ADD,
                    tickerInput = "",
                    nameInput = "",
                    targetAsset = null
                ) }
            }
            is ManageAssetsAction.OnShowEditSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = AssetBottomSheetMode.EDIT,
                    targetAsset = action.asset,
                    tickerInput = action.asset.ticker,
                    nameInput = action.asset.name
                ) }
            }
            is ManageAssetsAction.OnShowDeleteSheet -> {
                _uiState.update { it.copy(
                    isBottomSheetVisible = true,
                    bottomSheetMode = AssetBottomSheetMode.DELETE,
                    targetAsset = action.asset
                ) }
            }
            ManageAssetsAction.OnDismissSheet -> _uiState.update { it.copy(isBottomSheetVisible = false) }
            is ManageAssetsAction.OnTickerInputChanged -> _uiState.update { it.copy(tickerInput = action.ticker) }
            is ManageAssetsAction.OnNameInputChanged -> _uiState.update { it.copy(nameInput = action.name) }
            ManageAssetsAction.OnConfirmClicked -> handleConfirm()
            ManageAssetsAction.OnBackClicked -> {
                viewModelScope.launch { _events.send(ManageAssetsEvent.NavigateBack) }
            }
        }
    }

    private fun handleConfirm() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (state.bottomSheetMode) {
                    AssetBottomSheetMode.ADD, AssetBottomSheetMode.EDIT -> {
                        upsertAssetDefinitionUseCase(state.tickerInput, state.nameInput)
                    }
                    AssetBottomSheetMode.DELETE -> {
                        state.targetAsset?.let { deleteAssetDefinitionUseCase(it.ticker) }
                    }
                }
                _uiState.update { it.copy(isBottomSheetVisible = false, isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    sealed interface ManageAssetsEvent {
        data object NavigateBack : ManageAssetsEvent
    }
}
