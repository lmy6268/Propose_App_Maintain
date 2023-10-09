package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.PreLoadModelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.CheckForDownloadModelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.DownloadModelUseCase
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.DownloadResponse
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
    private val _downloadResponseState = MutableStateFlow<DownloadResponse?>(null)
    val downloadInfoState = _downloadResponseState.asStateFlow()
    private val _totalLoadedState = MutableStateFlow(false)
    private val _modelLoadedState = MutableStateFlow(false)
    val totalLoadedState = _totalLoadedState.asStateFlow()
    private val _checkDownloadState = MutableStateFlow<CheckResponse?>(null)
    val checkDownloadState = _checkDownloadState.asStateFlow() //체크의 결과값을 가지고 있는 변수

    fun preLoadModel() {
        _modelLoadedState.value = false
        viewModelScope.launch {
            _modelLoadedState.value = preLoadModelUseCase()
            checkLoadAllPreRunMethod()
        }

    }


    //다운로드 할 데이터 체크하는 함수
    fun requestForCheckDownload() {
        viewModelScope.launch {
            //여기에 다운로드가 필요한지 여부를 파악하는 로직을 담는다.
            _checkDownloadState.value = checkDownloadModelUseCase()
        }
    }


    //다운로드를 요청하는 함수
    fun requestForDownload() {
        viewModelScope.launch {
            //다운로드를 요청하고,정상적으로 다운로드가 진행되는 경우,
            _downloadResponseState.value = downloadModelUseCase()
        }
    }

    fun clearStates() {
        _downloadResponseState.value = null
        _checkDownloadState.value = null
    }


    private fun checkLoadAllPreRunMethod() {
        if (_modelLoadedState.value) _totalLoadedState.value = true
    }




}