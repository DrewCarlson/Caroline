package drewcarlson.caroline.logging

import drewcarlson.caroline.core.CarolineSDK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private const val DEFAULT_MESSAGE_BUFFER_SIZE = 150

/**
 * [CarolineLogger] provides a familiar logging interface that can
 * be configured to manage a buffer of messages in various ways.
 * This buffer can be flushed into a [LogDispatcher] to store the
 * logs in the desired location.
 */
public interface CarolineLogger {

    public companion object {
        /**
         * Create a new [CarolineLogger] targeting [sdk] with the
         * given [logSchedule].
         */
        public fun create(
            sdk: CarolineSDK,
            logSchedule: LogSchedule,
            outputLogDispatcher: LogDispatcher = HttpLogDispatcher(sdk),
            cachingLogDispatcher: CachingLogDispatcher? = null,
            messageBufferSize: Int = DEFAULT_MESSAGE_BUFFER_SIZE
        ): CarolineLogger {
            return CarolineLoggerImpl(
                cachingLogDispatcher = cachingLogDispatcher,
                outputLogDispatcher = outputLogDispatcher,
                logSchedule = logSchedule,
                messageBufferSize = messageBufferSize
            )
        }
    }

    /**
     * Process the [message] with [level] depending on
     * the selected delivery strategy.
     *
     * Arbitrary data can be included in [attributes].
     */
    public fun log(level: Int, message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.DEBUG] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logDebug(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.INFO] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logInfo(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.WARN] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logWarn(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.ERROR] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logError(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.FATAL] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logFatal(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Log [message] with the [CarolineLogLevel.TRACE] level.
     *
     * An optional [attributes] map can be provided to include
     * arbitrary data with the message.
     */
    public fun logTrace(message: String, attributes: Map<String, String> = emptyMap())

    /**
     * Flush the current log buffer.
     *
     * If [deliver] is true the messages will be consumed by
     * [LogDispatcher], otherwise stored messages are discarded.
     */
    public fun flush(deliver: Boolean = true)
}
