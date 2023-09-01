//package com.example.proposeapplication.data.guidence
//
//import okhttp3.MultipartBody
//import okhttp3.ResponseBody
//import retrofit2.Response
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//
//interface RetrofitService {
//    @Multipart
//    @POST("/guide")
//    suspend fun sendImage(@Part imageFile: MultipartBody.Part): Response<String>
//}