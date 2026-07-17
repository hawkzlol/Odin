package com.odtheking.odin.utils

private val PORT_VERSION = Regex("""^v?(\d+)\.(\d+)\.(\d+)(?:\+mc26\.2\.port\.(\d+))?$""")

private data class PortVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val portRevision: Int,
) : Comparable<PortVersion> {
    override fun compareTo(other: PortVersion): Int =
        compareValuesBy(this, other, PortVersion::major, PortVersion::minor, PortVersion::patch, PortVersion::portRevision)
}

private fun parsePortVersion(value: String, requirePortRevision: Boolean): PortVersion? {
    val match = PORT_VERSION.matchEntire(value.trim()) ?: return null
    val values = match.groupValues
    if (requirePortRevision && values[4].isEmpty()) return null
    return PortVersion(
        major = values[1].toIntOrNull() ?: return null,
        minor = values[2].toIntOrNull() ?: return null,
        patch = values[3].toIntOrNull() ?: return null,
        portRevision = values[4].takeIf(String::isNotEmpty)?.toIntOrNull() ?: 0,
    )
}

internal fun isPortReleaseNewer(current: String, candidate: String?): Boolean {
    if (candidate == null) return false
    val currentVersion = parsePortVersion(current, requirePortRevision = false) ?: return false
    val candidateVersion = parsePortVersion(candidate, requirePortRevision = true) ?: return false
    return candidateVersion > currentVersion
}

internal const val PORT_RELEASE_PAGE = "https://github.com/hawkzlol/Odin/releases/latest"
internal const val PORT_RELEASE_API = "https://api.github.com/repos/hawkzlol/Odin/releases/latest"
