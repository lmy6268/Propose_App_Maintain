package com.example.proposeapplication.presentation

import android.content.Context
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.permission.PermissionCheckUseCase
import com.example.proposeapplication.domain.usecase.camera.RetrievePreviewSizeUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowFixedScreenUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import com.example.proposeapplication.presentation.uistate.CameraUiState
import com.example.proposeapplication.presentation.uistate.PermissionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
) : ViewModel() {

    private val _pUiState = MutableStateFlow<PermissionUiState>(PermissionUiState.Ready)
    private val _fixedScreenUiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    private val _captureUiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)

    val pUiState = _pUiState.asStateFlow()
    val fixedScreenUiState = _fixedScreenUiState.asStateFlow()
    val captureUiState = _captureUiState.asStateFlow()



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
            _captureUiState.emit(CameraUiState.Loading)
            Log.d(
                "elapse to Take", "${
                    measureTimeMillis {
                        val data = captureImageUseCase(orientationData)
                        Log.d(
                            "${MainViewModel::class.simpleName}",
                            "${data.height} * ${data.width}"
                        )
                        _captureUiState.emit(CameraUiState.Success(data))
                    }.toString()
                }ms"
            )
        }
    }


    fun getPreviewSize(context: Context, display: Display) =
        retrievePreviewSizeUseCase(context, display)

    suspend fun getFixedScreen(viewFinder: SurfaceView) {
        _fixedScreenUiState.emit(CameraUiState.Loading)
        Log.d(
            "elapse to Take", "${
                measureTimeMillis {
                    val data = showFixedScreenUseCase(viewFinder)
                    _fixedScreenUiState.emit(CameraUiState.Success(data))
                }
            }ms"
        )
    }

}