package com.farukayata.yemektarifi.data.remote

import com.farukayata.yemektarifi.BuildConfig
import com.farukayata.yemektarifi.data.remote.model.VisionRequest
import com.farukayata.yemektarifi.data.remote.model.VisionResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface VisionApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String = BuildConfig.VISION_API_KEY,
        @Body request: VisionRequest
    ): VisionResponse
}
