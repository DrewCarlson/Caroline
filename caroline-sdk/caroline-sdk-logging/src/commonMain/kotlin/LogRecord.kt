package drewcarlson.caroline.logging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LogRecord(
    @SerialName("_id")
    val id: String,
    val level: Int,
    val timestamp: Long,
    val message: String,
    val attributes: Map<String, String>,
)
