@file:JvmName("BundledServer")

package drewcarlson.caroline

import com.mongodb.ConnectionString
import drewcalson.caroline.admin.api.ApiKeyCredentials
import drewcarlson.caroline.core.models.Permission
import drewcarlson.caroline.data.ProjectUserSession
import drewcarlson.caroline.data.RestrictedSession
import drewcarlson.caroline.data.UserSession
import drewcarlson.caroline.internal.carolineProperty
import drewcarlson.ktor.permissions.PermissionAuthorization
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.session
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CORS
import io.ktor.features.CachingHeaders
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ConditionalHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.PartialContent
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.content.CachingOptions
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.serialization.json
import io.ktor.sessions.Sessions
import io.ktor.sessions.header
import kotlinx.serialization.json.Json
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.event.Level
import java.util.Base64
import kotlin.random.Random

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private const val SESSION_KEY_BYTES = 32

val json = Json {
    isLenient = true
    prettyPrint = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = false
    classDiscriminator = "__type"
    allowStructuredMapKeys = true
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")

    val kmongo = KMongo.createClient(ConnectionString(mongoUrl))
    val mongodb = kmongo.getDatabase(databaseName).coroutine
    val apiKeyDb = mongodb.getCollection<ApiKeyCredentials>()

    val jwtIssuer = carolineProperty("jwtIssuer")
    val jwtRealm = carolineProperty("jwtRealm")
    val jwtSecret = carolineProperty("jwtSecret")
    JwtManager.configure(jwtIssuer, jwtRealm, jwtSecret)

    // TODO: Move this to server-core
    install(Authentication) {
        jwt(PROVIDER_API_JWT) {
            realm = jwtRealm
            verifier(JwtManager.verifier())
            validate { credential ->
                val audience = credential.payload.audience.firstOrNull()
                    ?: return@validate null
                // TODO: Just check for api key existence now.
                //   API keys, permissions, and invalidation should
                //   be refreshed elsewhere.
                apiKeyDb.findOneById(audience) ?: return@validate null
                ProjectUserSession(
                    permissions = credential.payload
                        .getClaim("permissions")
                        .asList(String::class.java)
                        .map(Permission::valueOf)
                        .toSet(),
                    payload = credential.payload
                )
            }
        }
        session<UserSession>(PROVIDER_ADMIN_SESSION) {
            challenge { context.respond(Unauthorized) }
            validate { it }
        }
    }

    install(Sessions) {
        header<UserSession>(UserSession.KEY, MongoSessionStorage(mongodb)) {
            val base64 = Base64.getEncoder()
            identity { base64.encodeToString(Random.nextBytes(SESSION_KEY_BYTES)) }
        }
    }

    install(PermissionAuthorization) {
        global(Permission.Global)
        extract { (it as RestrictedSession).permissions }
    }

    install(ContentNegotiation) {
        json(json)
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(AutoHeadResponse)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(ForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy

    install(CachingHeaders) {
        options { outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> {
                    val max = CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60)
                    CachingOptions(max)
                }
                else -> null
            }
        }
    }

    install(PartialContent) {
        // Maximum number of ranges that will be accepted from a HTTP request.
        // If the HTTP request specifies more ranges, they will all be merged into a single range.
        maxRangeCount = 10
    }
}
