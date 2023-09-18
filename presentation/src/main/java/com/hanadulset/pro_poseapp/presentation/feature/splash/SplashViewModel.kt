package com.hanadulset.pro_poseapp.presentation.feature.splash

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.PreLoadModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preLoadModelUseCase: PreLoadModelUseCase,

) : ViewModel() {

    private val _totalLoadedState = MutableStateFlow(false)
    private val _modelLoadedState = MutableStateFlow(false)
    val modelLoadedState = _modelLoadedState.asStateFlow()
    val totalLoadedState = _totalLoadedState.asStateFlow()

    fun preLoadModel() {
        _modelLoadedState.value = false
        viewModelScope.launch {
            _modelLoadedState.value = preLoadModelUseCase()
        }
        checkLoadAllPreRunMethod()
    }

    private fun checkLoadAllPreRunMethod() {
        if (_modelLoadedState.value) _totalLoadedState.value = true
    }

    fun preLoadMethods(){
        preLoadModel()

    }



}