package com.hanadulset.pro_poseapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ModelRunnerImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.CompDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.feature.PoseDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageRepositoryImpl(private val context: Context) : ImageRepository {
    private val modelRunnerImpl by lazy {
        ModelRunnerImpl(context)
    }

    private val poseDataSourceImpl by lazy {
        PoseDataSourceImpl(context, modelRunnerImpl)
    }

    private val imageProcessDataSource by lazy {
        ImageProcessDataSourceImpl()
    }

    private val fileHandleDataSource by lazy {
        FileHandleDataSourceImpl(context)
    }

    private val compDataSource by lazy {
        CompDataSourceImpl(modelRunnerImpl)
    }

    private val modelDownloadInfoFlow = MutableStateFlow(DownloadInfo())

//
//    //이미지를 저장하고 썸네일 만들기
//    override suspend fun saveImageToGallery(bitmap: Bitmap): Bitmap =
//        suspendCoroutine { cont ->
//            CoroutineScope(Dispatchers.IO).launch {
//                fileHandleDataSource.saveImageToGallery(bitmap)
//                val image =
//                    imageProcessDataSource.resizeBitmap(bitmap = bitmap, isScaledResize = true)
//                cont.resume(image)
//            }
//        }


    override suspend fun getRecommendCompInfo(image: Image, rotation: Int): Pair<String, Int>? {
        val targetBitmap =
            imageProcessDataSource.imageToBitmap(image, rotation)
//            AppCompatResources.getDrawable(
//                context,
//                com.hanadulset.pro_poseapp.utils.R.drawable.sample
//            )!!.toBitmap()

        return compDataSource.recommendCompData(targetBitmap)
    }

    override suspend fun getRecommendPose(
        image: Image, rotation: Int
    ): Pair<DoubleArray, List<PoseData>> =
        poseDataSourceImpl.recommendPose(
            imageProcessDataSource.imageToBitmap(image, rotation)
        )


    override fun getFixedScreen(backgroundBitmap: Bitmap): Bitmap =
        imageProcessDataSource.getFixedImage(bitmap = backgroundBitmap).apply {
//            modelRunnerImpl.runVapNet(backgroundBitmap)
        }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun getFixedScreen(imageProxy: ImageProxy): Bitmap =
        imageProcessDataSource.getFixedImage(
            imageProcessDataSource.imageToBitmap(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees
            )
        )


    override fun getLatestImage(): Uri {
        return fileHandleDataSource.getLatestImage()!!
    }

    override suspend fun downloadAiModel() {
        fileHandleDataSource.downloadModel(modelDownloadInfoFlow)
    }

    override fun getDownloadInfoFlow(): StateFlow<DownloadInfo> =
        modelDownloadInfoFlow.asStateFlow()

    override suspend fun checkForDownloadModel(downloadInfo: DownloadInfo) =
        fileHandleDataSource.checkForDownloadModel(downloadInfo)


    override suspend fun testS3(): String {
//        fileHandleDataSource.sendFeedBackData(
//            feedBackData = FeedBackData(
//                deviceID = "123Test",
//                eventLogs = ArrayList(
//                    listOf(
//                        EventLog(
//                            eventId = EventLog.EVENT_CAPTURE,
//                            poseID = 1,
//                            prevRecommendPoses = ArrayList(1),
//                            backgroundHog = ArrayList(listOf(1f)).toString(),
//                            backgroundId = 1,
//                            timestamp = System.currentTimeMillis().toString()
//                        )
//                    )
//                )
//            )
//        )
        return ""
//        return fileHandleDataSource.testS3()
    }


    override suspend fun preRunModel(): Boolean {
        return modelRunnerImpl.preRun()
    }

    override fun getPoseFromImage(uri: Uri?): Bitmap? =
        if (uri != null) {
            val backgroundBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri);
            }
            val softwareBitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, false)
            getFixedScreen(
                softwareBitmap
            )
        } else null
}