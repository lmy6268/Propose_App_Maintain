package com.example.proposeapplication.presentation.uistate

sealed class PermissionUiState {
    object Ready : PermissionUiState()
    data class Success(val value: Boolean) : PermissionUiState()
    data class Error(val exception: Throwable) : PermissionUiState()
}
