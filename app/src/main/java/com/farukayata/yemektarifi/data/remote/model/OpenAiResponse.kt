package com.farukayata.yemektarifi.data.remote.model

data class OpenAiResponse(
    val choices: List<Choice>
) {
    data class Choice(val message: Message)
    data class Message(val content: String)
}