package com.signage.yomie.CMSPlayer.network.translation

data class TranslationResponse(
    val Status: String,
    val Type: String,
    val ApiStatus: Boolean,
    val Translations: Translations
)