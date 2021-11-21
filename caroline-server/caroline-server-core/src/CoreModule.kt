@file:JvmName("CoreModule")

package tools.caroline

import com.mongodb.ConnectionString
import drewcarlson.ktor.permissions.PermissionAuthorization
import tools.caroline.admin.api.ApiKeyCredentials
import tools.caroline.core.models.Permission
import tools.caroline.data.ProjectUserSession
import tools.caroline.data.RestrictedSession
import tools.caroline.data.UserSession
import tools.caroline.internal.carolineProperty
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.session
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.sessions.Sessions
import io.ktor.sessions.header
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
            challenge { context.respond(HttpStatusCode.Unauthorized) }
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
