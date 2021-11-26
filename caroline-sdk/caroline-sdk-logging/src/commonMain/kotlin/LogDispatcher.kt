package cloud.caroline.logging

/**
 * [LogDispatcher]s deliver a list of [LogRecord]s to a
 * specific target, e.g. disk or remote service.
 */
public interface LogDispatcher {

    /**
     * Dispatch the [records] to the desired target.
     *
     * @return If the dispatcher was able to process the [records].
     */
    public fun dispatch(records: List<LogRecord>): Boolean

    /**
     * Dispose of any resources and ignore future calls to [dispatch].
     */
    public fun dispose()
}
