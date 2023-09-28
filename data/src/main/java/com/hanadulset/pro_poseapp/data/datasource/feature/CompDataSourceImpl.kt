package com.hanadulset.pro_poseapp.data.datasource.feature

import android.content.Context
import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.data.datasource.ModelRunnerImpl
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CompDataSourceImpl(private val modelRunner: ModelRunnerImpl) :
    CompDataSource {
    override suspend fun recommendCompData(backgroundBitmap: Bitmap): Pair<String, Int>? =
        suspendCoroutine {
            CoroutineScope(Dispatchers.IO).launch {
                it.resume(modelRunner.runVapNet(backgroundBitmap))
            }
        }

}