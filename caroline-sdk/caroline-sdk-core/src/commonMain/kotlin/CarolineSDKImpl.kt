package cloud.caroline.core

import cloud.caroline.internal.carolineJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.serialization.kotlinx.json.*
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
    private val serviceUrlMap: Map<CarolineSDK.Type, String>,
    httpClient: HttpClient,
    dispatcher: CoroutineDispatcher,
) : CarolineSDK {

    private val tokenLock = Mutex()
    private val tokenFlow = MutableStateFlow<String?>(null)

    override val scope = CoroutineScope(dispatcher + SupervisorJob())

    override val httpClient: HttpClient = httpClient.config {
        install(ContentNegotiation) {
            json(carolineJson)
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

    override fun urlFor(type: CarolineSDK.Type): String {
        return serviceUrlMap[type] ?: serverUrl
    }

    private suspend fun token(): String? {
        return tokenFlow.value ?: if (tokenLock.isLocked) {
            tokenFlow.drop(1).filterNotNull().first()
        } else {
            tokenFlow.value = null
            tokenLock.withLock {
                tokenFlow.value ?: createAndUpdateToken()
            }
        }
    }

    private suspend fun createAndUpdateToken(): String? {
        val response = httpClient.get("$serverUrl/core/token") {
            header("X-Caroline-Api-Key", apiKey)
        }
        return if (response.status == OK) {
            response.bodyAsText().also { newToken ->
                tokenFlow.value = newToken
            }
        } else {
            null
        }
    }
}
