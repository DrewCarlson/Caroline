package drewcarlson.caroline.logging

private const val DEFAULT_CACHE_SIZE = 5_242_880L // 5MB

/**
 * [CachingLogDispatcher]s act as a persistent cache to reduce
 * dispatches to a more expensive [LogDispatcher].
 */
public interface CachingLogDispatcher : LogDispatcher {

    public companion object {
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

    public fun flush(dispatcher: LogDispatcher?)
}
