@file:JvmName("BundledServer")

package cloud.caroline

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ConditionalHeaders)
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(AutoHeadResponse)
    install(CachingHeaders)

    /*install(TegralOpenApiKtor) {
        title = "Caroline"
        // TODO: typeProperty = "__type"
        "JWT" securityScheme {
            type = SecurityScheme.Type.HTTP
            inLocation = SecurityScheme.In.HEADER
            name = "Authorization"
            scheme = "bearer"
            bearerFormat = "JWT"
        }
        "Session" securityScheme {
            type = SecurityScheme.Type.APIKEY
            inLocation = SecurityScheme.In.HEADER
            name = UserSession.KEY
        }
    }
    install(TegralSwaggerUiKtor)*/
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
        //openApiEndpoint("/openapi")
        //swaggerUiEndpoint("/swagger", "/openapi")
    }
}
