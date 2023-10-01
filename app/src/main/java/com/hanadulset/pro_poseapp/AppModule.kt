package com.hanadulset.pro_poseapp

import android.content.Context
import com.hanadulset.pro_poseapp.data.repository.CameraRepositoryImpl
import com.hanadulset.pro_poseapp.data.repository.ImageRepositoryImpl
import com.hanadulset.pro_poseapp.data.repository.UserRepositoryImpl
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendCompInfoUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendPoseUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.CaptureImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.GetLatestImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.SetZoomLevelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.ShowFixedScreenUseCase

import com.hanadulset.pro_poseapp.domain.usecase.camera.ShowPreviewUseCase
import com.hanadulset.pro_poseapp.domain.usecase.config.WriteUserLogUseCase
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

        @Singleton
        @Provides
        fun provideUserRepository(@ApplicationContext context: Context): UserRepository =
            UserRepositoryImpl(context)
    }

    @InstallIn(ViewModelComponent::class)
    @Module
    internal object UseCasesModule {

        @ViewModelScoped
        @Provides
        fun provideShowPreviewUseCase(
            cameraRepository: CameraRepository,
        ): ShowPreviewUseCase =
            ShowPreviewUseCase(cameraRepository)

        @ViewModelScoped
        @Provides
        fun provideSetZoomLevelUseCase(
            cameraRepository: CameraRepository
        ): SetZoomLevelUseCase = SetZoomLevelUseCase(cameraRepository)

        @ViewModelScoped
        @Provides
        fun provideShowFixedScreenUseCase(
            imageRepository: ImageRepository,
//            cameraRepository: CameraRepository
        ): ShowFixedScreenUseCase = ShowFixedScreenUseCase(
            imageRepository
//            , cameraRepository
        )


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

        @ViewModelScoped
        @Provides
        fun provideWriteUserLog(userRepository: UserRepository): WriteUserLogUseCase =
            WriteUserLogUseCase(userRepository)


    }

}


