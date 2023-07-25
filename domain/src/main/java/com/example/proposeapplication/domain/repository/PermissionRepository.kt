package com.example.proposeapplication.domain.repository


interface PermissionRepository {
    fun checkAllPermissions():Boolean
}