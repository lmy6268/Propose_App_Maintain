package com.example.proposeapplication

import androidx.lifecycle.ViewModel
import com.example.proposeapplication.domain.usecase.PermissionCheckUseCase
import com.example.proposeapplication.domain.usecase.PermissionRequestUseCase
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