package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import javax.inject.Inject

class CheckUserSuccessToUseUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() = userRepository.checkUserSuccessToTermOfUse()
}