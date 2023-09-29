package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.PreLoadModelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.CheckForDownloadModelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.DownloadModelUseCase
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrepareServiceViewModel @Inject constructor(
    private val checkDownloadModelUseCase: CheckForDownloadModelUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val preLoadModelUseCase: PreLoadModelUseCase,

    ) : ViewModel() {
    private val _downloadInfoState = MutableStateFlow(DownloadInfo())
    val downloadInfoState = _downloadInfoState.asStateFlow()
    private val _totalLoadedState = MutableStateFlow(false)
    private val _modelLoadedState = MutableStateFlow(false)
    val totalLoadedState = _totalLoadedState.asStateFlow()
    val downloadState = downloadModelUseCase.getFlow()

    fun checkForDownload() {
        viewModelScope.launch {
            _downloadInfoState.value = checkDownloadModelUseCase(_downloadInfoState.value)
        }
    }

    fun requestToDownload() {
        viewModelScope.launch {
            downloadModelUseCase.startToDownload()
        }
    }

    private fun preLoadModel() {
        _modelLoadedState.value = false
        viewModelScope.launch {
            _modelLoadedState.value = preLoadModelUseCase()
            checkLoadAllPreRunMethod()
        }

    }

    private fun checkLoadAllPreRunMethod() {
        if (_modelLoadedState.value) _totalLoadedState.value = true
    }

    fun preLoadMethods() {
        preLoadModel()
    }


}