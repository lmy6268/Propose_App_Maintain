package com.hanadulset.pro_poseapp

import androidx.lifecycle.ViewModel
import com.hanadulset.pro_poseapp.domain.usecase.PermissionCheckUseCase
import com.hanadulset.pro_poseapp.domain.usecase.PermissionRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //UseCases
    private val requestUseCase: PermissionRequestUseCase,
    private val checkUseCase: PermissionCheckUseCase
) : ViewModel() {
    fun requestPermission(){
        requestUseCase()
    }
    fun checkPermission(){
        checkUseCase()
    }

}