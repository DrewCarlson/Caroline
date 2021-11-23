package tools.caroline.crash

import kotlinx.serialization.Serializable

@Serializable
public data class CrashReport(
    val stacktrace: String,
    val properties: Map<String, String>,
)