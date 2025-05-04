package com.example.tubitak.network

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response

interface ApiService {
    @Multipart
    @POST("/predict")
    suspend fun predictDisease(
        @Part image: MultipartBody.Part
    ): Response<PredictionResult>
}
