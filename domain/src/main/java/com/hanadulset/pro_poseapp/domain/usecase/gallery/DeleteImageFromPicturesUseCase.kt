package com.hanadulset.pro_poseapp.domain.usecase.gallery

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class DeleteImageFromPicturesUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(uri: Uri) = imageRepository.deleteCapturedImage(uri)
}