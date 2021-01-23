package drewcarlson.caroline.logging

/**
 * [LogSchedule] defines the normally desired signal that causes
 * log messages to be delivered.
 */
public sealed class LogSchedule {
    /**
     * Deliver each new log message to the [LogDispatcher]
     * for immediate dispatch.
     *
     * By default, [realtime] is true meaning a single websocket
     * connection will be used to deliver log messages.
     *
     * If [realtime] is false and the [LogDispatcher] is network
     * enabled, each line will result in a single HTTP request.
     */
    public data class Immediate(
        val realtime: Boolean = true,
    ) : LogSchedule()

    /**
     * Deliver all messages after the given [milliseconds] interval.
     */
    public data class Interval(
        val milliseconds: Long,
        val rescheduleIfBufferFull: Boolean,
    ) : LogSchedule()

    /**
     * Only delivers all messages when the buffer is full.
     */
    public object WhenBufferFull : LogSchedule()
}
