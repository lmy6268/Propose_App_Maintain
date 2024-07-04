package com.hanadulset.pro_poseapp.domain.usecase.user

import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings
import javax.inject.Inject

class SaveUserSetUseCase
@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(proPoseAppSettings: ProPoseAppSettings) = userRepository.saveUserSet(proPoseAppSettings)
}