package com.example.proposeapplication.data

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.proposeapplication.domain.repository.PermissionRepository
import com.example.proposeapplication.utils.PermissionDialog.Companion.PERMISSIONS_REQUIRED
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PermissionRepositoryImpl @Inject constructor(val appContext: Context) : PermissionRepository {




    override fun checkAllPermissions(): Boolean =
         PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                appContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
}