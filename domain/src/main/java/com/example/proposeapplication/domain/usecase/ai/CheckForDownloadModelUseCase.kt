package com.example.proposeapplication.domain.usecase.ai

import com.example.proposeapplication.domain.repository.ImageRepository
import com.example.proposeapplication.utils.DownloadInfo
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

//모델 다운로드가 필요한지 여부를 확인하는 유스케이스
class CheckForDownloadModelUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(downloadStateFlow: MutableStateFlow<DownloadInfo>) =
        imageRepository.testS3()
}