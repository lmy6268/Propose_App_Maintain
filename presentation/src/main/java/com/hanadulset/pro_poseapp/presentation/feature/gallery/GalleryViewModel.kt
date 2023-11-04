package com.hanadulset.pro_poseapp.presentation.feature.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.gallery.DeleteImageFromPicturesUseCase
import com.hanadulset.pro_poseapp.domain.usecase.gallery.GetImagesFromPicturesUseCase
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    //UseCases
    //이미지 목록 불러오는 유스케이스
    private val getImagesFromPicturesUseCase: GetImagesFromPicturesUseCase,
    private val deleteImageFromPicturesUseCase: DeleteImageFromPicturesUseCase
    //특정 이미지 삭제하는 유스케이스
) : ViewModel() {
    private val _capturedImageState = MutableStateFlow<List<ImageResult>?>(null)
    val capturedImageState = _capturedImageState.asStateFlow() //UI 단에서 사용할 때
    private val _deleteCompleteState = MutableStateFlow<Boolean?>(null)
    val deleteCompleteState = _deleteCompleteState.asStateFlow()


    //이미지목록을 불러온다.
    fun loadImages() {
        _capturedImageState.value = null
        viewModelScope.launch {
            _capturedImageState.value = getImagesFromPicturesUseCase() //이미지 목록을 가져옴 .
        }
    }

    //이미지 삭제
    fun deleteImage(index: Int, isOnDialog: Boolean) {
        _deleteCompleteState.value = false
        viewModelScope.launch {
            if (isOnDialog || deleteImageFromPicturesUseCase(uri = _capturedImageState.value!![index].dataUri!!)) {
                _capturedImageState.update {
                    it!!.toMutableList().apply {
                        removeAt(index)
                    }.toList()
                }
                _deleteCompleteState.value = true
            }
        }
    }
}