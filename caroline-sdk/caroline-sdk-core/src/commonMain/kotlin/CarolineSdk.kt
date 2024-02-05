@file:Suppress("FunctionName")

package cloud.caroline.core

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

public interface CarolineSdk {

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
 * Configure an instance of [CarolineSdk] to target a self-hosted Caroline deployment.
 *
 * ```kotlin
 * CarolineSdk {
 *     serverUrl = "https://yourdomain"
 *     projectId = "..."
 *     apiKey = "..."
 * }
 * ```
 */
public fun CarolineSdk(configure: CarolineSdkBuilder.() -> Unit): CarolineSdk {
    return CarolineSdkBuilder().apply(configure).build()
}

/**
 * Configure an instance of [CarolineSdk] to target https://caroline.cloud.
 * The only required configuration is your [CarolineSdk.projectId] and [CarolineSdk.apiKey]
 * provided when creating your project or under on the Security settings page.
 *
 * ```kotlin
 * CarolineCloudSDK {
 *     projectId = "..."
 *     apiKey = "..."
 * }
 * ```
 */
public fun CarolineCloudSdk(configure: CarolineSdkBuilder.() -> Unit): CarolineSdk {
    return CarolineSdkBuilder()
        .apply(configure)
        .apply {
            serverUrl = "https://caroline.cloud"
            serviceUrls.clear()
            serviceUrls.putAll(
                CarolineSdk.Type.entries.associateWith { type ->
                    "${type.name.lowercase()}.[serverUrl]"
                },
            )
        }
        .build()
}
