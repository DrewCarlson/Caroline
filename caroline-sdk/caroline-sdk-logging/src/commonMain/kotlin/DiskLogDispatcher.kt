package cloud.caroline.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class DiskLogDispatcher(
    private val path: String,
    override val cacheSize: Long,
) : CachingLogDispatcher {

    private val scope = CoroutineScope(Default + SupervisorJob())

    override fun dispatch(records: List<LogRecord>): Boolean {
        TODO("Not yet implemented")
    }

    override fun flush(dispatcher: LogDispatcher?) {
    }

    override fun dispose() {
        scope.cancel()
    }
}
