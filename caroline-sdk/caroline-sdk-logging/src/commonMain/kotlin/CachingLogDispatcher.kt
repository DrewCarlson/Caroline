package cloud.caroline.logging

private const val DEFAULT_CACHE_SIZE = 5_242_880L // 5MB

/**
 * [CachingLogDispatcher]s act as a persistent cache to reduce
 * dispatches to a more expensive [LogDispatcher].
 */
public interface CachingLogDispatcher : LogDispatcher {

    public companion object {
        /**
         * Create a new [CachingLogDispatcher] that writes data to
         * disk at [filePath] until the [cacheSize] in bytes is reached.
         */
        public fun create(
            filePath: String,
            cacheSize: Long = DEFAULT_CACHE_SIZE
        ): CachingLogDispatcher {
            return DiskLogDispatcher(filePath, cacheSize)
        }
    }

    /**
     * The maximum size (generally bytes) stored by this
     * [CachingLogDispatcher] before a call to [flush] is
     * required.
     */
    public val cacheSize: Long

    /**
     * Flush stored logs from the cache, optionally directing
     * them into [dispatcher].
     */
    public fun flush(dispatcher: LogDispatcher?)
}
