package com.farukayata.yemektarifi.data.remote.model

data class OpenAiImageResponse(
    val created: Long,
    val data: List<ImageData>
)

data class ImageData(
    val url: String
)
