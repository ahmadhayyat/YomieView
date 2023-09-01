package com.signage.yomie.commons

interface SocketInterface {
    fun onDataCaptured(data: String, type: String)
}