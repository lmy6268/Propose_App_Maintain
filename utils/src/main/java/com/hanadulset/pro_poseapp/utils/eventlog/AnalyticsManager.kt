package com.hanadulset.pro_poseapp.utils.eventlog

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings
import com.google.firebase.Firebase
import com.google.firebase.analytics.ParametersBuilder
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

@SuppressLint("HardwareIds")
class AnalyticsManager(contentResolver: ContentResolver) {
    private val analytics = Firebase.analytics
    private val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    private val isOnDebug = true


    private fun makeLogEvent(eventName: String, params: ParametersBuilder.() -> Unit = {}) =
        analytics.run {
            if(isOnDebug.not()) {
                setUserId(deviceID)
                logEvent(eventName) {
                    param("deviceID", deviceID)
                    param("timestamp", System.currentTimeMillis())
                    params()
                }
            }
        }

    fun saveUserAgreeToUseEvent() = makeLogEvent(
        eventName = EVENT_SUCCESS_TO_USE
    ) {
        param("user_answer", true.toString())
    }

    fun saveAppOpenEvent() = makeLogEvent(EVENT_APP_OPEN)
    fun saveAppClosedEvent() = makeLogEvent(EVENT_APP_CLOSE)

    fun saveCapturedEvent(
        captureEventData: CaptureEventData,
        estimationResult: List<Triple<Float, Float, Float>?>
    ) {
        captureEventData.run {
            val hog = backgroundHog.toString()
            val prev = prevRecommendPoses.toString()
            val poseEstimationResult = estimationResult.toString()

            makeLogEvent(EVENT_CAPTURE) {
                param("poseID", poseID.toDouble())
                param("backgroundId", backgroundId?.toDouble() ?: -1.0)
                if (prev.length > 99) {
                    prev.chunked(99).forEachIndexed { index, s ->
                        param("prevRecommendPoses_$index", s)
                    }
                } else {
                    param(
                        "prevRecommendPoses", prev
                    )
                }
                if (hog.length > 99) {
                    hog.chunked(99).forEachIndexed { index, s ->
                        param(
                            "backgroundHog_$index", s
                        )
                    }
                } else {
                    param(
                        "backgroundHog", hog
                    )
                }
                if (poseEstimationResult.length > 99) {
                    poseEstimationResult.chunked(99).forEachIndexed { index, s ->
                        param(
                            "human_pose_estimation_$index", s
                        )
                    }
                } else {
                    param("human_pose_estimation", poseEstimationResult)
                }
            }
        }

    }


    companion object {
        private const val EVENT_CAPTURE = "EVENT_CAPTURE"
        private const val EVENT_APP_CLOSE = "EVENT_APP_CLOSE"
        private const val EVENT_SUCCESS_TO_USE = "EVENT_SUCCESS_TO_USE"
        private const val EVENT_APP_OPEN = "EVENT_APP_OPEN"
    }
}