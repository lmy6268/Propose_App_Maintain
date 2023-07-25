package com.example.proposeapplication.domain.usecase.permission

import com.example.proposeapplication.domain.repository.PermissionRepository
import javax.inject.Inject

class PermissionCheckUseCase @Inject constructor(private val repository: PermissionRepository) {
    operator fun invoke() =
        repository.checkAllPermissions()

}