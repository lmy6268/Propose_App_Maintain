package com.example.camera.domain

import com.example.camera.domain.repository.CameraTakeRepository


interface CameraCaptureUseCase{
    operator fun  invoke(

    )
}
//촬영하는 부분만 담당하는 유즈케이스 - 이미지 촬영에만 이용된다.
class CameraCaptureUseCaseImpl constructor(
   private val repository: CameraTakeRepository
):CameraCaptureUseCase {
    override fun invoke() {
       repository.getCapturedImage()
    }
}