@file:JvmName("BundledServer")

package cloud.caroline

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ConditionalHeaders)
    //install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(AutoHeadResponse)
    install(CachingHeaders)

    install(CallLogging) {
        level = Level.INFO
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }

    install(CORS) {
        methods.addAll(HttpMethod.DefaultMethods)
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowHeaders { true }
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    routing {
        // TODO: Set proper version
        val apiInfo = OpenApiInfo(
            title = "Caroline",
            version = "1.0",
            description = "Privacy respecting backend services with multiplatform Kotlin SDKs.",
        )
        openAPI("api") {
            info = apiInfo
        }
        swaggerUI("api") {
            info = apiInfo
        }
    }
}
