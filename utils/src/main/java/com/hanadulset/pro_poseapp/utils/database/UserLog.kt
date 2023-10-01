package com.hanadulset.pro_poseapp.utils.database

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class UserLog(
    @PrimaryKey
    val timeStamp:String,
    val eventId:Int,//이벤트 번호
    val poseID:Int?,//혹시 포즈를 켜고 찍은 경우, 현재 선택된 포즈 아이디
    val prevRecommendPoses:String?, //포즈를 켜고 찍은 기록이 있다면, 이전에 선택했던 항목에 대한 정보들
    val backgroundId:Int?, //포즈 추천을 받은 경우, 해당 배경에 대한 클러스터 아이디를 반환
    val backgroundHog:String? //해당 배경에 대한 Hog값을 저장 (FloatArray -> String)
)
