package com.farukayata.yemektarifi.data.remote.model

import com.google.gson.annotations.SerializedName


data class VisionResponse(
    @SerializedName("responses")
    val responses: List<Response>
) {
    data class Response(
        @SerializedName("labelAnnotations")
        val labelAnnotations: List<LabelAnnotation>? = null,

        @SerializedName("localizedObjectAnnotations")
        val localizedObjectAnnotations: List<VisionRequest.LocalizedObjectAnnotation>? = null
    )

    data class LabelAnnotation(
        val description: String,
        val score: Float
    )
}
/*
data class VisionResponse(
    val responses: List<Response>
) {
    data class Response(
        val labelAnnotations: List<LabelAnnotation>?
    )

    data class LabelAnnotation(
        val description: String,
        val score: Float
    )
}

 */
