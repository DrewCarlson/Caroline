package cloud.caroline.logging

import cloud.caroline.internal.currentSystemMs

internal class CarolineLoggerImpl(
    private val outputLogDispatcher: LogDispatcher,
    private val cachingLogDispatcher: CachingLogDispatcher?,
    private val logSchedule: LogSchedule,
    private val messageBufferSize: Int,
) : CarolineLogger {

    private val logRecordQueue = ArrayDeque<LogRecord>(messageBufferSize)

    override fun log(level: Int, message: String, attributes: Map<String, String>) {
        val logRecord = LogRecord(
            id = "",
            level = level,
            timestamp = currentSystemMs(),
            message = message,
            attributes = attributes,
        )
        // TODO: Launch everything in below on the sdk scope
        if (cachingLogDispatcher == null) {
            when (logSchedule) {
                LogSchedule.WhenBufferFull -> {
                    if (logRecordQueue.count() == messageBufferSize) {
                        val records = logRecordQueue.toList()
                        outputLogDispatcher.dispatch(records)
                        logRecordQueue.clear()
                    }
                    logRecordQueue.add(logRecord)
                }
                is LogSchedule.Immediate -> {
                    outputLogDispatcher.dispatch(listOf(logRecord))
                }
                is LogSchedule.Interval -> {
                    // TODO: Interval scheduling
                }
            }
        } else {
            when (logSchedule) {
                LogSchedule.WhenBufferFull -> {
                    if (logRecordQueue.count() == messageBufferSize) {
                        val records = logRecordQueue.toList()
                        cachingLogDispatcher.apply {
                            if (!dispatch(records)) {
                                flush(outputLogDispatcher)
                                dispatch(records)
                            }
                        }
                        logRecordQueue.clear()
                    }
                    logRecordQueue.add(logRecord)
                }
                is LogSchedule.Immediate -> {
                    val records = listOf(logRecord)
                    outputLogDispatcher.dispatch(records)
                }
                is LogSchedule.Interval -> {
                    // TODO: Interval scheduling
                }
            }
        }
    }

    override fun logDebug(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.DEBUG, message, attributes)
    }

    override fun logInfo(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.INFO, message, attributes)
    }

    override fun logWarn(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.WARN, message, attributes)
    }

    override fun logError(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.ERROR, message, attributes)
    }

    override fun logFatal(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.FATAL, message, attributes)
    }

    override fun logTrace(message: String, attributes: Map<String, String>) {
        log(CarolineLogLevel.TRACE, message, attributes)
    }

    override fun flush(deliver: Boolean) {
        if (deliver) {
            val records = logRecordQueue.toList()
            logRecordQueue.clear()

            cachingLogDispatcher?.flush(outputLogDispatcher)
            outputLogDispatcher.dispatch(records)
        } else {
            logRecordQueue.clear()
            cachingLogDispatcher?.flush(null)
        }
    }
}
