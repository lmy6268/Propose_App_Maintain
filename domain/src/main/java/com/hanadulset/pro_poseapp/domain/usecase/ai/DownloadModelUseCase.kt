package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

// 모델을 다운로드 하는 유스케이스
class DownloadModelUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke() = repository.downloadResources()

}