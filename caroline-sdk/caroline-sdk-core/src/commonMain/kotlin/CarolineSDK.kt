package tools.caroline.core

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public interface CarolineSDK {

    public val serverUrl: String

    public val projectId: String

    public val apiKey: String

    public val httpClient: HttpClient

    public val scope: CoroutineScope

    public fun urlFor(type: Type): String

    public enum class Type {
        ADMIN,
        ANALYTICS,
        AUTH,
        CONFIG,
        CORE,
        CRASH,
        FUNCTIONS,
        LOGGING,
        STORE,
    }
}

/**
 * Configure an instance of [CarolineSDK] to target a self-hosted Caroline deployment.
 *
 * ```kotlin
 * CarolineSDK {
 *     serverUrl = "https://yourdomain"
 *     projectId = "..."
 *     apiKey = "..."
 * }
 * ```
 */
public fun CarolineSDK(configure: CarolineSDKBuilder.() -> Unit): CarolineSDK {
    return CarolineSDKBuilder().apply(configure).build()
}

/**
 * Configure an instance of [CarolineSDK] to target https://caroline.tools.
 * The only required configuration is your [CarolineSDK.projectId] and [CarolineSDK.apiKey]
 * provided when creating your project or under on the Security settings page.
 *
 * ```kotlin
 * CarolineCloudSDK {
 *     projectId = "..."
 *     apiKey = "..."
 * }
 * ```
 */
public fun CarolineCloudSDK(configure: CarolineSDKBuilder.() -> Unit): CarolineSDK {
    return CarolineSDKBuilder()
        .apply(configure)
        .apply {
            serverUrl = "https://caroline.tools"
            serviceUrls.clear()
            serviceUrls.putAll(cloudUrlFormats)
        }
        .build()
}

/**
 * Default service URL configurations for cloud target
 */
private val cloudUrlFormats = buildMap<CarolineSDK.Type, String>(
    capacity = CarolineSDK.Type.values().size
) {
    put(CarolineSDK.Type.ADMIN, "admin.[serverUrl]")
    put(CarolineSDK.Type.ANALYTICS, "analytics.[serverUrl]")
    put(CarolineSDK.Type.AUTH, "auth.[serverUrl]")
    put(CarolineSDK.Type.CONFIG, "config.[serverUrl]")
    put(CarolineSDK.Type.CORE, "core.[serverUrl]")
    put(CarolineSDK.Type.CRASH, "crash.[serverUrl]")
    put(CarolineSDK.Type.FUNCTIONS, "functions.[serverUrl]")
    put(CarolineSDK.Type.LOGGING, "logging.[serverUrl]")
    put(CarolineSDK.Type.STORE, "store.[serverUrl]")
}