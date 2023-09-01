package com.signage.yomie.CMSPlayer.network

class RequestParameter {
    data class CheckDevice(var DeviceID: String, var InstallationId: String)
    data class AddDevice(
        var DeviceID: String,
        var InstallationId: String,
        var PlayerID: String,
        var ClaimID: String
    )

    data class ErrorLog(
        var error: String,
        var PlayerID: String,
        var ClaimID: String,
        var type: String
    )
}