package com.odtheking.odin.utils.network.hypixelapi

const val DEFAULT_HYPIXEL_API_URL = "https://hypixel.odtheking.com/"

/** Upgrade the former default without overriding a user's custom endpoint. */
internal fun resolveHypixelApiUrl(configured: String): String =
    if (configured.trimEnd('/') == "https://api.odtheking.com/hypixel") DEFAULT_HYPIXEL_API_URL else configured
