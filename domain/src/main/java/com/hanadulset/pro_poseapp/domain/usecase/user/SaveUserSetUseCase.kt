package com.hanadulset.pro_poseapp.domain.usecase.user

import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.UserSet
import javax.inject.Inject

class SaveUserSetUseCase
@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userSet: UserSet) = userRepository.saveUserSet(userSet)
}