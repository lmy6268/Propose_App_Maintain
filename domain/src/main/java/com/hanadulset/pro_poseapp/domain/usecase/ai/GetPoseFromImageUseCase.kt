package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class GetPoseFromImageUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    operator fun invoke(uri: Uri?) = imageRepository.getPoseFromImage(uri)
}