package com.example.proposeapplication.domain.usecase.camera

import android.view.Display
import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class RetrievePreviewSizeUseCase @Inject constructor(private val repository: CameraRepository) {
    operator fun invoke(display: Display) =
        repository.getPreviewSize(display)
}