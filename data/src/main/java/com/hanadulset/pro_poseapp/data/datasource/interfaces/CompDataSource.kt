package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap

interface CompDataSource {
    suspend fun recommendCompData(backgroundBitmap:Bitmap): Pair<String, Int>?
}