@file:JvmName("CoreModule")

package cloud.caroline

import com.mongodb.ConnectionString
import org.drewcarlson.ktor.permissions.PermissionAuthorization
import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.core.models.Permission
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.data.RestrictedSession
import cloud.caroline.data.UserSession
import cloud.caroline.internal.carolineProperty
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.Base64
import kotlin.random.Random

private const val SESSION_KEY_BYTES = 32

public val json: Json = Json {
    isLenient = true
    prettyPrint = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = false
    classDiscriminator = "__type"
    allowStructuredMapKeys = true
}

@Suppress("unused")
public fun Application.coreModule() {
    val mongoUrl = carolineProperty("mongoUrl")
    val databaseName = carolineProperty("databaseName")
    val apiPath = carolineProperty("apiBasePath", CAROLINE_API_PATH)

    val kmongo = KMongo.createClient(ConnectionString(mongoUrl))
    val mongodb = kmongo.getDatabase(databaseName).coroutine
    val apiKeyDb = mongodb.getCollection<ApiKeyCredentials>()

    val jwtIssuer = carolineProperty("jwtIssuer")
    val jwtRealm = carolineProperty("jwtRealm")
    val jwtSecret = carolineProperty("jwtSecret")
    JwtManager.configure(jwtIssuer, jwtRealm, jwtSecret)

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
                val creds = apiKeyDb.findOneById(audience) ?: return@validate null
                ProjectUserSession(
                    projectId = creds.projectId,
                    apiKey = audience,
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
            challenge { call.respond(HttpStatusCode.Unauthorized) }
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

    routing {
        route(apiPath) {
            addCoreRoutes(mongodb)
        }
    }
}
