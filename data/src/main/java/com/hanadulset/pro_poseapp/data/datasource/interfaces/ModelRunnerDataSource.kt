package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import org.pytorch.Module

/**모델을  실행하는 데이터소스*/
interface ModelRunnerDataSource {
    /** 모델을 실행하기 위한 Workflow
     * 1. 서버로부터 모델을 다운로드 받는다. -> 이건 앱 초기 실행시 다운로드 화면에서 진행한다.
     * 2. 다운로드 받은 모델을 앱 전용 폴더에 저장한다.
     *
     *     https://stackoverflow.com/questions/44587187/android-how-to-write-a-file-to-internal-storage
     * 3. 각 모델을 실행한다.
     * */


    //파일명을 이용하여, AI 모델을 로드한다.
    fun loadModel(moduleAssetName: String): Module

    //미리 실행해둠
    suspend fun preRun(): Boolean

    fun runVapNet(bitmap: Bitmap): Pair<Float, Float>?

}