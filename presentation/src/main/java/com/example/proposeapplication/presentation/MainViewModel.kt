package com.example.proposeapplication.presentation

import android.view.Display
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.permission.PermissionCheckUseCase
import com.example.proposeapplication.domain.usecase.camera.RetrievePreviewSizeUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //UseCases
    private val checkUseCase: PermissionCheckUseCase,
    private val showPreviewUseCase: ShowPreviewUseCase,
    private val retrievePreviewSizeUseCase: RetrievePreviewSizeUseCase,
) : ViewModel() {
    private val _pUiState = MutableStateFlow<PermissionUiState>(PermissionUiState.Ready)
    private val _cUiState = MutableStateFlow<CamearUiState>(CamearUiState.Ready)
    val pUiState = _pUiState.asStateFlow()
    val cUiState = _cUiState.asStateFlow()

    fun checkPermission() {
        _pUiState.update {
            PermissionUiState.Success(checkUseCase())
        }
    }

    fun showPreview(surface: Surface) {
        viewModelScope.launch {
            _cUiState.update { CamearUiState.Loading }
            showPreviewUseCase(surface)
            _cUiState.update { CamearUiState.Success }
        }
    }

    fun getPreviewSize(display: Display) =
        retrievePreviewSizeUseCase(display)


}