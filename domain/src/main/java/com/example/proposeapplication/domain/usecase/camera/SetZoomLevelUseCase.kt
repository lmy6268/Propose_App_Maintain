package com.example.proposeapplication.domain.usecase.camera

import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class SetZoomLevelUseCase @Inject constructor(private val repository: CameraRepository) {
    operator fun invoke(zoomLevel: Float) = repository.setZoomRatio(zoomLevel)
}