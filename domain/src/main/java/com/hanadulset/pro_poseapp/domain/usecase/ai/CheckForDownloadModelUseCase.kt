package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

//모델 다운로드가 필요한지 여부를 확인하는 유스케이스
class CheckForDownloadModelUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(downloadStateFlow: MutableStateFlow<DownloadInfo>) =
        imageRepository.testS3()
}