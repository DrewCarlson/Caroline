package drewcarlson.caroline.core

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

public interface CarolineSDK {

    public val serverUrl: String

    public val projectId: String

    public val apiKey: String

    public val httpClient: HttpClient

    public val scope: CoroutineScope
}

public class CarolineSDKBuilder {

    public var serverUrl: String = ""

    public var projectId: String = ""

    public var apiKey: String = ""

    public var dispatcher: CoroutineDispatcher = Dispatchers.Default

    public lateinit var httpClient: HttpClient

    internal fun build(): CarolineSDK {
        check(serverUrl.isNotBlank()) {
            "CarolineSDK `serverUrl` must be configured"
        }

        check(projectId.isNotBlank()) {
            "CarolineSDK `projectId` must be configured"
        }

        check(apiKey.isNotBlank()) {
            "CarolineSDK `apiKey` must be configured"
        }

        if (!::httpClient.isInitialized) {
            httpClient = HttpClient()
        }

        return object : CarolineSDK {
            override val serverUrl: String = this@CarolineSDKBuilder.serverUrl
            override val projectId: String = this@CarolineSDKBuilder.projectId
            override val apiKey: String = this@CarolineSDKBuilder.apiKey
            override val httpClient: HttpClient = this@CarolineSDKBuilder.httpClient
            override val scope: CoroutineScope = CoroutineScope(dispatcher + SupervisorJob())
        }
    }
}

public fun CarolineSDK(configure: CarolineSDKBuilder.() -> Unit): CarolineSDK {
    return CarolineSDKBuilder().apply(configure).build()
}
