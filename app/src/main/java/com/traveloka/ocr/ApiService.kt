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

    @FormUrlEncoded
    @POST("id_cards")
    fun uploadIdCard(
        @Header("Authorization") header: String,
        @Field("province") province: String,
        @Field("district") district: String,
        @Field("id_number") nin: String,
        @Field("name") name: String,
        @Field("place_date_of_birth") placeAndDob: String,
        @Field("gender") gender: String,
        @Field("blood_type") bloodType: String,
        @Field("address") address: String,
        @Field("neighborhood") neighborhood: String,
        @Field("village") village: String,
        @Field("subdistrict") subDistrict: String,
        @Field("religion") religion: String,
        @Field("marital_status") maritalStatus: String,
        @Field("occupation") occupation: String,
        @Field("nationality") nationality: String,
        @Field("expiry_date") expiredDate: String,
        @Field("attachment") image: String
    ) : Call<IdCardUploadResponse>

    @Multipart
    @POST("ocr")
    fun processingOcr(
        @Header("Authorization") header: String,
        @Part file: MultipartBody.Part
    ) : Call<OcrUploadResponse>
}