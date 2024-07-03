package com.hanadulset.pro_poseapp.domain.usecase.user

import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserSuccessToUseUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() = userRepository.saveUserSuccessToTermOfUse()
}