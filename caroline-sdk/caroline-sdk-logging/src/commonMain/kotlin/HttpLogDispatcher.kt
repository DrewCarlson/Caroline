package cloud.caroline.logging

import cloud.caroline.core.CarolineSDK
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class HttpLogDispatcher(
    private val sdk: CarolineSDK,
) : LogDispatcher {
    private val scope = CoroutineScope(sdk.scope.coroutineContext + SupervisorJob())
    private val serviceUrl = sdk.urlFor(CarolineSDK.Type.LOGGING)

    override fun dispatch(records: List<LogRecord>): Boolean {
        if (!scope.isActive) return false

        scope.launch {
            sdk.httpClient.post("$serviceUrl/api/logging/record") {
                contentType(ContentType.Application.Json)
                setBody(records)
            }
        }
        return true
    }

    override fun dispose() {
        scope.cancel()
    }
}
