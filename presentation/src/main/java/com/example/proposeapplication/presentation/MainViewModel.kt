package com.example.proposeapplication.presentation

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.permission.PermissionCheckUseCase
import com.example.proposeapplication.domain.usecase.camera.RetrievePreviewSizeUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import com.example.proposeapplication.presentation.uistate.CamearUiState
import com.example.proposeapplication.presentation.uistate.PermissionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class MainViewModel @Inject constructor(
    //UseCases
    private val checkUseCase: PermissionCheckUseCase,
    private val showPreviewUseCase: ShowPreviewUseCase,
    private val retrievePreviewSizeUseCase: RetrievePreviewSizeUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
) : ViewModel() {

    private val _pUiState = MutableStateFlow<PermissionUiState>(PermissionUiState.Ready)
    private val _cUiState = MutableStateFlow<CamearUiState>(CamearUiState.Ready)
    val pUiState = _pUiState.asStateFlow()
    val cUiState = _cUiState.asStateFlow()


    suspend fun checkPermission() {
        _pUiState.emit(PermissionUiState.Success(checkUseCase()))
    }

    fun showPreview(surface: Surface) {
        viewModelScope.launch {
            showPreviewUseCase(surface)
        }
    }

    fun takePhoto(orientationData: Int) {
        viewModelScope.launch {
            _cUiState.emit(CamearUiState.Loading)
            Log.d(
                "elapse to Take", "${
                    measureTimeMillis {
                        val data = captureImageUseCase(orientationData)
                        Log.d(
                            "${MainViewModel::class.simpleName}",
                            "${data.height} * ${data.width}"
                        )
                        _cUiState.emit(CamearUiState.Success(data))
                    }.toString()
                }ms"
            )
        }
    }

    fun getPreviewSize(context: Context, display: Display) =
        retrievePreviewSizeUseCase(context, display)


}