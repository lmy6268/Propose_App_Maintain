package com.example.proposeapplication.presentation

sealed class CamearUiState {
    object Ready : CamearUiState()
    object Loading : CamearUiState()
    object Success : CamearUiState()
    data class Error(val exception: Throwable) : CamearUiState()
}
