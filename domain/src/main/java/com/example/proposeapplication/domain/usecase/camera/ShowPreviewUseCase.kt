package com.example.proposeapplication.domain.usecase.camera


import android.view.Surface
import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class ShowPreviewUseCase @Inject constructor(private val repository: CameraRepository) {
    suspend operator fun invoke(surface: Surface) =
        repository.initPreview(surface)
}