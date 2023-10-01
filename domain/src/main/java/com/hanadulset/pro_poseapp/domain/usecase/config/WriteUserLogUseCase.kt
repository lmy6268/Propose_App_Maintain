package com.hanadulset.pro_poseapp.domain.usecase.config


import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import javax.inject.Inject

//이벤트를 DB에  저장하는 유스케이스
class WriteUserLogUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(eventLog: EventLog) = userRepository.writeEventLog(eventLog = eventLog)

}