package com.example.proposeapplication

import android.content.Context
import com.example.proposeapplication.data.CameraRepositoryImpl
import com.example.proposeapplication.data.ImageRepositoryImpl
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import com.example.proposeapplication.domain.usecase.ai.RecommendCompInfoUseCase
import com.example.proposeapplication.domain.usecase.ai.RecommendPoseUseCase
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.camera.GetLatestImageUseCase
import com.example.proposeapplication.domain.usecase.camera.SetZoomLevelUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowFixedScreenUseCase

import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Hilt 적용시 각 의존성을 주입하는 모듈
object AppModule {

    @InstallIn(SingletonComponent::class)
    @Module
    internal object RepoModule {

        @Singleton
        @Provides
        fun provideCameraRepository(@ApplicationContext context: Context): CameraRepository =
            CameraRepositoryImpl(context)

        @Singleton
        @Provides
        fun provideImageRepository(@ApplicationContext context: Context): ImageRepository =
            ImageRepositoryImpl(context)
    }

    @InstallIn(ViewModelComponent::class)
    @Module
    internal object UseCasesModule {

        @ViewModelScoped
        @Provides
        fun provideShowPreviewUseCase(
            cameraRepository: CameraRepository,
            imageRepository: ImageRepository
        ): ShowPreviewUseCase =
            ShowPreviewUseCase(cameraRepository,imageRepository)

        @ViewModelScoped
        @Provides
        fun provideSetZoomLevelUseCase(
            cameraRepository: CameraRepository
        ): SetZoomLevelUseCase = SetZoomLevelUseCase(cameraRepository)

        @ViewModelScoped
        @Provides
        fun provideShowFixedScreenUseCase(
            imageRepository: ImageRepository,
            cameraRepository: CameraRepository
        ): ShowFixedScreenUseCase = ShowFixedScreenUseCase(imageRepository, cameraRepository)


        @ViewModelScoped
        @Provides
        fun provideCaptureImageUseCase(
            cameraRepository: CameraRepository,
            imageRepository: ImageRepository
        ): CaptureImageUseCase =
            CaptureImageUseCase(
                cameraRepository,
                imageRepository
            )

        @ViewModelScoped
        @Provides
        fun provideGetLatestImageUseCase(imageRepository: ImageRepository): GetLatestImageUseCase =
            GetLatestImageUseCase(imageRepository)

        @ViewModelScoped
        @Provides
        fun provideRecommendCompInfoUseCase(imageRepository: ImageRepository): RecommendCompInfoUseCase =
            RecommendCompInfoUseCase(imageRepository)

        @ViewModelScoped
        @Provides
        fun provideRecommendPoseUseCase(imageRepository: ImageRepository): RecommendPoseUseCase =
            RecommendPoseUseCase(imageRepository)


    }

}


