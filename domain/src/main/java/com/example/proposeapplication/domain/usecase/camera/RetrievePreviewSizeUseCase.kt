package com.example.proposeapplication.domain.usecase.camera

import android.app.Activity
import android.content.Context
import android.view.Display
import com.example.proposeapplication.domain.repository.CameraRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject


class RetrievePreviewSizeUseCase @Inject constructor(
    private val repository: CameraRepository
) {
    operator fun invoke(context: Context,display: Display) =
        repository.getPreviewSize(context,display)
}