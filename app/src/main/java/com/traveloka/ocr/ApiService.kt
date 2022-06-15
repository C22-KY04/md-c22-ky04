package com.traveloka.ocr

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("id_cards")
    fun getIdCardList(
        @Header("Authorization") header: String,
        @Query("name") name: String
    ) : Call<KtpResponse>

    @Multipart
    @POST("ocr")
    fun processingOcr(
        @Header("Authorization") header: String,
        @Part file: MultipartBody.Part
    ) : Call<OcrUploadResponse>
}