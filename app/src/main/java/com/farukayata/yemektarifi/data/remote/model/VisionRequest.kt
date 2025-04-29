package com.farukayata.yemektarifi.data.remote.model

import com.google.gson.annotations.SerializedName

data class VisionRequest(
    val requests: List<Request>
) {
    data class Request(
        val image: Image,
        val features: List<Feature>,
        val imageContext: ImageContext? = null
    )

    data class Image(
        val content: String
    )

    data class Feature(
        val type: String = "LABEL_DETECTION",
        val maxResults: Int = 20
    )
    data class ImageContext(
        val languageHints: List<String> = listOf("en")
    )

    data class Response(
        @SerializedName("localizedObjectAnnotations")
        val localizedObjects: List<LocalizedObjectAnnotation>? = null
    )

    data class LocalizedObjectAnnotation(
        val name: String,
        val score: Float
    )
}
