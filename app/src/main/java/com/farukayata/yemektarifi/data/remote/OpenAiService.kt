package com.farukayata.yemektarifi.data.remote

import com.farukayata.yemektarifi.data.remote.model.OpenAiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface OpenAiService {
    @Headers("Content-Type: application/json")
    //APInin beklediği Content-Type ı manuel ayarladık
    @POST("v1/chat/completions")
    suspend fun getImageAnalysis(
        @Body requestBody: RequestBody
    ): OpenAiResponse
}


//https://api.openai.com/ kısmı zaten var yukarısı end point olarak kalmalı

/*
interface OpenAiService {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer YOUR_OPENAI_API_KEY"
    )
    @POST("chat/completions")
    suspend fun getImageAnalysis(
        @Body requestBody: RequestBody
    ): OpenAiResponse
}
*/

