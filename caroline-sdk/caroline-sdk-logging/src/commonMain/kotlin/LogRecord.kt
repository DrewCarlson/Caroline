package cloud.caroline.logging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing a single log line.
 */
@Serializable
public data class LogRecord(
    @SerialName("_id")
    val id: String,
    /** The [CarolineLogLevel] of this record. */
    val level: Int,
    /** The unix timestamp in milliseconds. */
    val timestamp: Long,
    /** The string contents of this record. */
    val message: String,
    /** Additional attributes associated with this record. */
    val attributes: Map<String, String>,
)
