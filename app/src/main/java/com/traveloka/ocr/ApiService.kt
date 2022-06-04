package com.traveloka.ocr

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("id_cards")
    fun getKtp(
        @Header("Authorization") header: String
    ) : Call<KtpResponse>
}