package drewcarlson.caroline.logging

import drewcarlson.caroline.core.CarolineSDK
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class HttpLogDispatcher(
    private val sdk: CarolineSDK
) : LogDispatcher {
    private val scope = CoroutineScope(sdk.scope.coroutineContext + SupervisorJob())

    override fun dispatch(records: List<LogRecord>): Boolean {
        if (!scope.isActive) return false

        scope.launch {
            sdk.httpClient.post("${sdk.serverUrl}/api/logging/record") {
                contentType(ContentType.Application.Json)
                body = records
            }
        }
        return true
    }

    override fun dispose() {
        scope.cancel()
    }
}
