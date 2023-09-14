package com.hanadulset.pro_poseapp.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.data.datasource.CameraDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {

    private val cameraDataSource by lazy {
        CameraDataSourceImpl(applicationContext)
    }
    private val imageProcessDataSourceImpl by lazy {
        ImageProcessDataSourceImpl()
    }


    override fun initCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,

        ) = cameraDataSource.initCamera(
        lifecycleOwner, surfaceProvider, aspectRatio, previewRotation, analyzer
    )

    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun takePhoto(isFixedRequest: Boolean) =
        cameraDataSource.takePhoto(isFixedRequest)
            .let { data ->
                if (data is ImageProxy)
                    data.use {
                        imageProcessDataSourceImpl.imageToBitmap(
                            data.image!!,
                            data.imageInfo.rotationDegrees
                        )
                    }
                else data
            }


    override fun setZoomRatio(zoomLevel: Float) =
        cameraDataSource.setZoomLevel(zoomLevel)


//   suspend fun compositionData(bitmap: Bitmap): String {
//        try {
//            val URL = "https://regularly-enabled-perch.ngrok-free.app/guide/"
////             "http://172.16.101.36:8000/guide/"
//            val retrofit: Retrofit = Retrofit.Builder()
//                .baseUrl(URL).addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(
//                    GsonConverterFactory.create()
//                ).build()
//            val retrofitService: RetrofitService = retrofit.create(RetrofitService::class.java)
//            val filesDir = applicationContext.filesDir
//            val file = File(filesDir, "targetImage.jpg")
//
//            file.outputStream().use { outputStream ->
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            }
//
//            val body = MultipartBody.Part.createFormData(
//                "image", "target.jpg",
//                RequestBody.create(MediaType.parse("multipart/form-data; image/*"), file)
//            )
//            val res = retrofitService.sendImage(body)
//            if (res.isSuccessful && res.body() != null) {
//                val data = JSONObject(res.body()!!).getString("direction")
//                Log.d("loadedData", data)
//                return data
//            } else {
//                Log.e("NetworkError", "Request was not successful or response body is null")
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("NetworkError", "Exception: ${e.message}")
//        }
//
//        return ""
//    }
}
