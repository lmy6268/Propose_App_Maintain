package com.example.proposeapplication

import android.content.Context
import com.example.proposeapplication.data.CameraRepositoryImpl
import com.example.proposeapplication.data.PermissionRepositoryImpl
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.PermissionRepository
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.permission.PermissionCheckUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


object AppModule {

    @InstallIn(SingletonComponent::class)
    @Module
    internal object RepoModule {

        @Singleton
        @Provides
        fun providePermissionRepository(@ApplicationContext appContext: Context): PermissionRepository =
            PermissionRepositoryImpl(appContext) //레포지토리 의존성 주입을 위함.\

        @Singleton
        @Provides
        fun provideCameraRepository(@ApplicationContext context: Context): CameraRepository =
            CameraRepositoryImpl(context)

    }

    @InstallIn(ViewModelComponent::class)
    @Module
    internal object UseCasesModule {
        @ViewModelScoped
        @Provides
        fun provideCheckUseCase(permissionRepository: PermissionRepository): PermissionCheckUseCase =
            PermissionCheckUseCase(permissionRepository)

        @ViewModelScoped
        @Provides
        fun provideShowPreviewUseCase(cameraRepository: CameraRepository): ShowPreviewUseCase =
            ShowPreviewUseCase(cameraRepository)

        @ViewModelScoped
        @Provides
        fun provideCaptureImageUseCase(cameraRepository: CameraRepository): CaptureImageUseCase =
            CaptureImageUseCase(cameraRepository)
    }
}


