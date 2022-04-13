package cloud.caroline.core

import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


public class CarolineSDKBuilder internal constructor() {

    /**
     * The primary URL of the Caroline server to target.
     * For an all-in-one instance only this URL is required as all services
     * are exposed from the same domain.
     *
     * @see serviceUrls for advanced deployments with independently hosted services.
     */
    public var serverUrl: String = ""

    /**
     * The required project id generated by the Caroline installation you're targeting.
     */
    public var projectId: String = ""

    /**
     * The required api key generated by the Caroline installation you're targeting.
     */
    public var apiKey: String = ""

    /**
     * An optional [CoroutineDispatcher] to be used by SDK components.
     */
    public var dispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * An optional [HttpClient] used as the base client by SDK components.
     */
    public lateinit var httpClient: HttpClient

    /**
     * Advanced deployments of Caroline that put each service behind a distinct URL
     * must provide the subdomain or base path for each service.
     *
     * URL formats should either be `logging.\[serverUrl]` for subdomains or
     * `\[serverUrl]/logging` for a base path.
     *
     * @see CarolineCloudSDK to configure url formats automatically when targeting caroline.cloud
     */
    public val serviceUrls: MutableMap<CarolineSDK.Type, String> = mutableMapOf()

    internal fun build(): CarolineSDK {
        check(serverUrl.isNotBlank()) {
            "CarolineSDK `serverUrl` must be configured"
        }

        check(serverUrl.startsWith("https://") || serverUrl.startsWith("http://")) {
            "CarolineSdk `serverUrl` must start with 'https://' or 'http://' for advanced deployments"
        }

        check(projectId.isNotBlank()) {
            "CarolineSDK `projectId` must be configured"
        }

        check(apiKey.isNotBlank()) {
            "CarolineSDK `apiKey` must be configured"
        }

        serviceUrls.forEach { (type, format) ->
            check(format.contains("[serverUrl]")) {
                "CarolineSDK `serviceUrls` must contain '[serverUrl]' to be formatted with the configured value\n" +
                        "   CarolineSDK { serviceUrls[${type.name}] = \"${type.name.lowercase()}.[serverUrl]\" }"
            }
        }
        val formattedServiceUrls = serviceUrls.mapValues { (_, format) ->
            format.replace("[serverUrl]", serverUrl)
        }

        if (!::httpClient.isInitialized) {
            httpClient = HttpClient()
        }

        return CarolineSDKImpl(serverUrl, projectId, apiKey, formattedServiceUrls, httpClient, dispatcher)
    }
}