package com.example.proposeapplication.presentation.uistate

sealed class CameraUiState {
    object Ready : CameraUiState()
    object Loading : CameraUiState()
    data class Success(val data: Any?): CameraUiState()
    data class Error(val exception: Throwable) : CameraUiState()
}
