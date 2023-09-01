package com.signage.yomie.utils.ErrorLog

data class Response(
    val data: String
)

data class ErrorLogResponse(
    val request: String,
    val success: Int,
    val response: Response
)
