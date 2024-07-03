package com.hanadulset.pro_poseapp.utils.model.camera

sealed class ProPoseCameraState<out T> {
    data object Loading : ProPoseCameraState<Nothing>()
    data class Success<out T>(val data: T) : ProPoseCameraState<T>()

    data class Error(
        val exception: Exception?
    ) : ProPoseCameraState<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(exception: Exception?) = Error(exception)
        fun loading() = Loading
    }

}





