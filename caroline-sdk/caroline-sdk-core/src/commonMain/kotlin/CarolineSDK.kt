package cloud.caroline.core

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

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
 * Configure an instance of [CarolineSDK] to target https://caroline.cloud.
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
            serverUrl = "https://caroline.cloud"
            serviceUrls.clear()
            serviceUrls.putAll(
                CarolineSDK.Type.values().associateWith { type ->
                    "${type.name.lowercase()}.[serverUrl]"
                },
            )
        }
        .build()
}
