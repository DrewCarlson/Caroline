package drewcarlson.caroline.core

import drewcarlson.caroline.internal.carolineJson
import io.ktor.client.HttpClient
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val AUTHORIZATION = "Authorization"

internal class CarolineSDKImpl(
    override val serverUrl: String,
    override val projectId: String,
    override val apiKey: String,
    httpClient: HttpClient,
    dispatcher: CoroutineDispatcher
) : CarolineSDK {

    private val tokenLock = Mutex()
    private val tokenFlow = MutableStateFlow<String?>(null)

    override val scope = CoroutineScope(dispatcher + SupervisorJob())

    override val httpClient: HttpClient = httpClient.config {
        Json {
            serializer = KotlinxSerializer(carolineJson)
        }

        install("CarolineProjectJWT") {
            requestPipeline.intercept(HttpRequestPipeline.Before) {
                if (!context.headers.contains(AUTHORIZATION)) {
                    context.header(AUTHORIZATION, "Bearer ${token()}")
                }
            }
            responsePipeline.intercept(HttpResponsePipeline.Receive) {
                if (context.response.status == Unauthorized) {
                    val sentWithToken = context.request.headers[AUTHORIZATION]
                        ?.substringAfter("Bearer ")
                        ?.isNotBlank() == true
                    if (sentWithToken) {
                        tokenFlow.value = null
                    }
                }
            }
        }
    }

    private suspend fun token(): String {
        return tokenFlow.value ?: if (tokenLock.isLocked) {
            tokenFlow.drop(1).filterNotNull().first()
        } else {
            tokenFlow.value = null
            tokenLock.withLock {
                tokenFlow.value ?: createAndUpdateToken()
            }
        }
    }

    private suspend fun createAndUpdateToken(): String =
        httpClient.get<String>("$serverUrl/core/token") {
            header(AUTHORIZATION, apiKey)
        }.also { newToken ->
            tokenFlow.value = newToken
        }
}
