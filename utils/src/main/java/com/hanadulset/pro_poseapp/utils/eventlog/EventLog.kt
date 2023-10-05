package com.hanadulset.pro_poseapp.utils.eventlog

import kotlinx.serialization.Serializable
import java.sql.Timestamp

//Serializable 어노테이션을 통해 쉽게 json으로 직렬화가 가능하다.
@Serializable
data class EventLog(
    val eventId: Int, //
    val poseID: Int, //포즈
    val prevRecommendPoses: List<Int>?, //이전에 선택한 추천 포즈 데이터
    val timestamp: String?, //이벤트 발생 시기
    val backgroundId: Int?, //백그라운드 ID
    val backgroundHog: String?, //백그라운드 HOG
    val estimationValue: List<List<Int>> = listOf(listOf())
) {
    companion object {

        const val EVENT_CAPTURE = 0 //촬영 시

        //촬영 안할 때
        const val EVENT_RE_POSE = 1 // 포즈 추천의 슬라이드를 넘겼을 때
        const val EVENT_POSE_OFF = 2 //포즈 추천을 껐을 때


        const val EVENT_APP_PEACEFUL_CLOSE = 3
        const val EVENT_APP_CRASHED = 4
    }
}