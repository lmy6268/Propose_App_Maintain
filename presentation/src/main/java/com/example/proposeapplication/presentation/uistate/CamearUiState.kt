package com.example.proposeapplication.presentation.uistate

sealed class CamearUiState {
    object Ready : CamearUiState()
    object Loading : CamearUiState()
    data class Success(val data: Any?): CamearUiState()
    data class Error(val exception: Throwable) : CamearUiState()
}
