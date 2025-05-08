package com.farukayata.yemektarifi.data.remote

import com.farukayata.yemektarifi.data.remote.model.OpenAiImageResponse
import com.farukayata.yemektarifi.data.remote.model.OpenAiResponse
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

    /*
    //dally için
    @POST("v1/images/generations")
    suspend fun generateImage(@Body request: RequestBody): OpenAiRespons
     */

    @POST("v1/images/generations")
    suspend fun generateImage(@Body request: RequestBody): OpenAiImageResponse


}


//https://api.openai.com/ kısmı zaten var yukarısı end point olarak kalmalı

