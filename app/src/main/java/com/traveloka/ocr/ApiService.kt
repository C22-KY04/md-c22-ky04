package com.traveloka.ocr

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("id_cards")
    fun getKtp(
        @Header("Authorization") header: String
    ) : Call<KtpResponse>

    @GET("id_cards")
    fun searchKtp(
        @Header("Authorization") header: String,
        @Query("name") name: String
    ) : Call<KtpResponse>
}