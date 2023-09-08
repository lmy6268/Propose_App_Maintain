package com.example.proposeapplication.domain.usecase.ai

import com.example.proposeapplication.domain.repository.ImageRepository
import com.example.proposeapplication.utils.DownloadInfo
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CheckForDownloadModelUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(downloadStateFlow: MutableStateFlow<DownloadInfo>) =
        imageRepository.testS3()
}